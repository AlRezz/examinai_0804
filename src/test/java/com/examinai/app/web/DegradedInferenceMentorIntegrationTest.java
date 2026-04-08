package com.examinai.app.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.examinai.app.domain.task.SubmissionStatus;
import com.examinai.app.domain.user.RoleRepository;
import com.examinai.app.domain.user.User;
import com.examinai.app.domain.user.UserRepository;
import com.examinai.app.integration.ai.AiDraftAssessmentService;
import com.examinai.app.integration.ai.InferenceUnavailableException;
import com.examinai.app.service.SubmissionService;
import com.examinai.app.service.TaskAssignmentService;
import com.examinai.app.service.TaskService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DegradedInferenceMentorIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskAssignmentService taskAssignmentService;

	@Autowired
	private SubmissionService submissionService;

	@MockBean
	private AiDraftAssessmentService aiDraftAssessmentService;

	private User intern;

	private java.util.UUID taskId;

	@BeforeEach
	void seed() {
		intern = userRepository.findByEmail("degraded-intern@examinai.local").orElseGet(() -> {
			User u = new User("degraded-intern@examinai.local", passwordEncoder.encode("DegradedIntern!9"));
			u.addRole(roleRepository.findByName("intern").orElseThrow());
			return userRepository.save(u);
		});
		userRepository.findByEmail("degraded-mentor@examinai.local").orElseGet(() -> {
			User m = new User("degraded-mentor@examinai.local", passwordEncoder.encode("DegradedMentor!9"));
			m.addRole(roleRepository.findByName("mentor").orElseThrow());
			return userRepository.save(m);
		});
		taskId = taskService.create("Degraded inference task", "Brief.", java.time.LocalDate.now().plusDays(2)).getId();
		taskAssignmentService.replaceAssignmentsForTask(taskId, List.of(intern.getId()));
		submissionService.upsertCoordinates(taskId, intern.getId(), "owner/repo", "abc1111", "README.md",
				SubmissionStatus.SUBMITTED);
	}

	@Test
	void submissionDetailShowsBannerAfterAiFailure_andMentorCanPublish() throws Exception {
		when(aiDraftAssessmentService.generateDraft(any()))
			.thenThrow(new InferenceUnavailableException("service unreachable"));

		var login = mockMvc.perform(formLogin("/login").user("degraded-mentor@examinai.local").password("DegradedMentor!9"))
			.andExpect(authenticated())
			.andReturn();
		var session = (MockHttpSession) login.getRequest().getSession();
		java.util.UUID internId = intern.getId();
		String detailUrl = "/tasks/" + taskId + "/submissions/" + internId;

		mockMvc.perform(post(detailUrl + "/ai-draft-assessment").with(csrf()).session(session))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl(detailUrl));

		String html = mockMvc.perform(get(detailUrl).session(session)).andExpect(status().isOk()).andReturn().getResponse()
			.getContentAsString();
		assertThat(html).contains("Assistive AI temporarily unavailable");

		mockMvc.perform(post(detailUrl + "/publish-review").with(csrf())
			.session(session)
			.param("qualityScore", "4")
			.param("readabilityScore", "4")
			.param("correctnessScore", "4")
			.param("narrativeFeedback", "Manual review without model."))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl(detailUrl));

		var sub = submissionService.findForTaskAndInternOrNull(taskId, internId);
		assertThat(sub.getStatus()).isEqualTo(SubmissionStatus.OUTCOME_PUBLISHED);
	}
}
