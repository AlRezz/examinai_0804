package com.examinai.app.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.examinai.app.domain.ai.ModelInvocationRepository;
import com.examinai.app.domain.review.PublishedReviewRepository;
import com.examinai.app.domain.task.GitRetrievalState;
import com.examinai.app.domain.task.SubmissionRepository;
import com.examinai.app.domain.task.SubmissionStatus;
import com.examinai.app.domain.user.RoleRepository;
import com.examinai.app.domain.user.User;
import com.examinai.app.domain.user.UserRepository;
import com.examinai.app.service.AiDraftPersistenceService;
import com.examinai.app.service.MentorReviewService;
import com.examinai.app.service.SubmissionService;
import com.examinai.app.service.TaskAssignmentService;
import com.examinai.app.service.TaskService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class Epic5AiDraftPersistenceIntegrationTest {

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
	private SubmissionRepository submissionRepository;

	@Autowired
	private AiDraftPersistenceService aiDraftPersistenceService;

	@Autowired
	private ModelInvocationRepository modelInvocationRepository;

	@Autowired
	private MentorReviewService mentorReviewService;

	@Autowired
	private PublishedReviewRepository publishedReviewRepository;

	private User intern;

	private java.util.UUID taskId;

	@BeforeEach
	void seed() {
		intern = userRepository.findByEmail("epic5-intern@examinai.local").orElseGet(() -> {
			User u = new User("epic5-intern@examinai.local", passwordEncoder.encode("Epic5Intern!9"));
			u.addRole(roleRepository.findByName("intern").orElseThrow());
			return userRepository.save(u);
		});
		userRepository.findByEmail("epic5-mentor@examinai.local").orElseGet(() -> {
			User m = new User("epic5-mentor@examinai.local", passwordEncoder.encode("Epic5Mentor!9"));
			m.addRole(roleRepository.findByName("mentor").orElseThrow());
			return userRepository.save(m);
		});
		taskId = taskService.create("Epic5 AI draft task", "Review rubric.", java.time.LocalDate.now().plusDays(2)).getId();
		taskAssignmentService.replaceAssignmentsForTask(taskId, List.of(intern.getId()));
		submissionService.upsertCoordinates(taskId, intern.getId(), "owner/repo", "abc1111", "README.md", SubmissionStatus.SUBMITTED);
		var sub = submissionRepository.findByTask_IdAndIntern_Id(taskId, intern.getId()).orElseThrow();
		sub.setGitRetrievalState(GitRetrievalState.OK);
		sub.setGitRetrievedText("def init():\n    pass\n");
		submissionRepository.save(sub);
	}

	@Test
	void publishReviewViaWebSucceedsWithNoAiInvocationRows() throws Exception {
		var sub = submissionRepository.findByTask_IdAndIntern_Id(taskId, intern.getId()).orElseThrow();
		assertThat(modelInvocationRepository.countBySubmission_Id(sub.getId())).isZero();

		var login = mockMvc.perform(formLogin("/login").user("epic5-mentor@examinai.local").password("Epic5Mentor!9"))
			.andExpect(authenticated())
			.andReturn();
		MockHttpSession session = (MockHttpSession) login.getRequest().getSession();
		String detailUrl = "/tasks/" + taskId + "/submissions/" + intern.getId();

		mockMvc.perform(post(detailUrl + "/publish-review").with(csrf())
			.session(session)
			.param("qualityScore", "3")
			.param("readabilityScore", "4")
			.param("correctnessScore", "5")
			.param("narrativeFeedback", "Published without any AI draft."))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl(detailUrl));

		var updated = submissionRepository.findById(sub.getId()).orElseThrow();
		assertThat(updated.getStatus()).isEqualTo(SubmissionStatus.OUTCOME_PUBLISHED);
		var published = publishedReviewRepository.findBySubmission_IdOrderByPublishedAtDesc(sub.getId());
		assertThat(published).hasSize(1);
		assertThat(published.getFirst().getNarrativeFeedback()).isEqualTo("Published without any AI draft.");
		assertThat(modelInvocationRepository.countBySubmission_Id(sub.getId())).isZero();
	}

	@Test
	void aiDraftAndInvocationPersistedSeparatelyFromPublishedReview() {
		var sub = submissionRepository.findByTask_IdAndIntern_Id(taskId, intern.getId()).orElseThrow();
		aiDraftPersistenceService.persistSuccessfulDraft(sub.getId(), "Strengths: clarity. Gaps: tests.");

		assertThat(modelInvocationRepository.countBySubmission_Id(sub.getId())).isEqualTo(1);
		assertThat(publishedReviewRepository.findBySubmission_IdOrderByPublishedAtDesc(sub.getId())).isEmpty();

		var mentorId = userRepository.findByEmail("epic5-mentor@examinai.local").orElseThrow().getId();
		mentorReviewService.publish(sub.getId(), mentorId, 4, 4, 4, "Official mentor narrative.");

		assertThat(publishedReviewRepository.findBySubmission_IdOrderByPublishedAtDesc(sub.getId())).hasSize(1);
		assertThat(modelInvocationRepository.countBySubmission_Id(sub.getId())).isEqualTo(1);
		var latest = aiDraftPersistenceService.findLatestForSubmission(sub.getId()).orElseThrow();
		assertThat(latest.assessmentText()).contains("Strengths: clarity.");
		assertThat(latest.promptHash()).hasSize(64);
	}

	@Test
	void mentorReviewPageLabelsAiDraftAsNotFinal() throws Exception {
		var sub = submissionRepository.findByTask_IdAndIntern_Id(taskId, intern.getId()).orElseThrow();
		aiDraftPersistenceService.persistSuccessfulDraft(sub.getId(), "Model draft content for UX.");

		var login = mockMvc.perform(formLogin("/login").user("epic5-mentor@examinai.local").password("Epic5Mentor!9"))
			.andExpect(authenticated())
			.andReturn();
		MockHttpSession session = (MockHttpSession) login.getRequest().getSession();
		String detailUrl = "/tasks/" + taskId + "/submissions/" + intern.getId();
		MvcResult page = mockMvc.perform(get(detailUrl).session(session)).andExpect(status().isOk()).andReturn();
		String html = page.getResponse().getContentAsString();
		assertThat(html).contains("Not final").contains("collapsed").contains("Model draft content for UX.")
			.contains("Official outcome is only");
	}
}
