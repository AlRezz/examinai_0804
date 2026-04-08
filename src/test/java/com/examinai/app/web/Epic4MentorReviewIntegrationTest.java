package com.examinai.app.web;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.examinai.app.domain.review.PublishedReviewRepository;
import com.examinai.app.domain.task.SubmissionStatus;
import com.examinai.app.domain.user.RoleRepository;
import com.examinai.app.domain.user.User;
import com.examinai.app.domain.user.UserRepository;
import com.examinai.app.service.SubmissionService;
import com.examinai.app.service.TaskAssignmentService;
import com.examinai.app.service.TaskService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class Epic4MentorReviewIntegrationTest {

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

	@Autowired
	private PublishedReviewRepository publishedReviewRepository;

	private User intern;

	private java.util.UUID taskId;

	@BeforeEach
	void seed() {
		intern = userRepository.findByEmail("epic4-intern@examinai.local").orElseGet(() -> {
			User u = new User("epic4-intern@examinai.local", passwordEncoder.encode("Epic4Intern!9"));
			u.addRole(roleRepository.findByName("intern").orElseThrow());
			return userRepository.save(u);
		});
		userRepository.findByEmail("epic4-mentor@examinai.local").orElseGet(() -> {
			User m = new User("epic4-mentor@examinai.local", passwordEncoder.encode("Epic4Mentor!9"));
			m.addRole(roleRepository.findByName("mentor").orElseThrow());
			return userRepository.save(m);
		});
		taskId = taskService.create("Epic4 review task", "Implement feature X.", java.time.LocalDate.now().plusDays(3)).getId();
		taskAssignmentService.replaceAssignmentsForTask(taskId, List.of(intern.getId()));
		submissionService.upsertCoordinates(taskId, intern.getId(), "owner/repo", "abc1111", "README.md", SubmissionStatus.SUBMITTED);
	}

	@Test
	void mentorCanOpenReviewQueue() throws Exception {
		var login = mockMvc.perform(formLogin("/login").user("epic4-mentor@examinai.local").password("Epic4Mentor!9"))
			.andExpect(authenticated())
			.andReturn();
		var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();
		mockMvc.perform(get("/review/queue").session(session)).andExpect(status().isOk());
	}

	@Test
	void internForbiddenFromReviewQueue() throws Exception {
		var login = mockMvc.perform(formLogin("/login").user("epic4-intern@examinai.local").password("Epic4Intern!9"))
			.andExpect(authenticated())
			.andReturn();
		var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();
		mockMvc.perform(get("/review/queue").session(session)).andExpect(status().isForbidden());
	}

	@Test
	void publishStoresProvenanceAndHistoryAcrossRevision() throws Exception {
		var login = mockMvc.perform(formLogin("/login").user("epic4-mentor@examinai.local").password("Epic4Mentor!9"))
			.andExpect(authenticated())
			.andReturn();
		var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();

		java.util.UUID internId = intern.getId();
		String detailUrl = "/tasks/" + taskId + "/submissions/" + internId;

		mockMvc.perform(post(detailUrl + "/publish-review").with(csrf())
			.session(session)
			.param("qualityScore", "4")
			.param("readabilityScore", "5")
			.param("correctnessScore", "3")
			.param("narrativeFeedback", "First pass."))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl(detailUrl));

		var sub1 = submissionService.findForTaskAndInternOrNull(taskId, internId);
		assertThat(sub1.getStatus()).isEqualTo(SubmissionStatus.OUTCOME_PUBLISHED);
		assertThat(publishedReviewRepository.findBySubmission_IdOrderByPublishedAtDesc(sub1.getId())).hasSize(1);

		submissionService.upsertCoordinates(taskId, internId, "owner/repo", "def2222", "README.md", SubmissionStatus.SUBMITTED);
		var sub2 = submissionService.findForTaskAndInternOrNull(taskId, internId);
		assertThat(sub2.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);

		mockMvc.perform(post(detailUrl + "/publish-review").with(csrf())
			.session(session)
			.param("qualityScore", "5")
			.param("readabilityScore", "5")
			.param("correctnessScore", "5")
			.param("narrativeFeedback", "Second pass after resubmit."))
			.andExpect(status().is3xxRedirection());

		var history = publishedReviewRepository.findBySubmission_IdOrderByPublishedAtDesc(sub2.getId());
		assertThat(history).hasSize(2);
		assertThat(history.getFirst().getSnapshotCommitSha()).isEqualTo("def2222");
		assertThat(history.get(1).getSnapshotCommitSha()).isEqualTo("abc1111");

		MvcResult page = mockMvc.perform(get(detailUrl).session(session)).andExpect(status().isOk()).andReturn();
		String html = page.getResponse().getContentAsString();
		assertThat(html).contains("First pass.").contains("Second pass after resubmit.");
		assertThat(html).contains("abc1111").contains("def2222");
	}
}
