package com.examinai.app.web.task;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.examinai.app.config.SecurityConfig;
import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.Task;
import com.examinai.app.domain.task.TaskAssignment;
import com.examinai.app.domain.user.User;
import com.examinai.app.domain.user.UserRepository;
import com.examinai.app.integration.ai.AiDraftAssessmentService;
import com.examinai.app.security.RoleBasedAuthenticationSuccessHandler;
import com.examinai.app.service.MentorReviewService;
import com.examinai.app.service.SourceRetrievalService;
import com.examinai.app.service.SubmissionService;
import com.examinai.app.service.TaskAssignmentService;
import com.examinai.app.service.TaskService;

@WebMvcTest(TaskSubmissionMentorController.class)
@Import({ SecurityConfig.class, RoleBasedAuthenticationSuccessHandler.class })
class TaskSubmissionMentorAiDraftWebMvcTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private TaskService taskService;

	@MockBean
	private TaskAssignmentService taskAssignmentService;

	@MockBean
	private SubmissionService submissionService;

	@MockBean
	private SourceRetrievalService sourceRetrievalService;

	@MockBean
	private MentorReviewService mentorReviewService;

	@MockBean
	private AiDraftAssessmentService aiDraftAssessmentService;

	@MockBean
	private UserRepository userRepository;

	@Test
	@WithMockUser(roles = "MENTOR")
	void mentorCanTriggerAiDraft() throws Exception {
		UUID taskId = UUID.randomUUID();
		UUID internId = UUID.randomUUID();
		UUID submissionId = UUID.randomUUID();

		Task task = org.mockito.Mockito.mock(Task.class);
		when(task.getId()).thenReturn(taskId);
		when(taskService.requireTask(taskId)).thenReturn(task);

		User intern = org.mockito.Mockito.mock(User.class);
		when(intern.getId()).thenReturn(internId);
		when(taskAssignmentService.listForTask(taskId)).thenReturn(List.of(new TaskAssignment(task, intern)));

		Submission submission = org.mockito.Mockito.mock(Submission.class);
		when(submission.getId()).thenReturn(submissionId);
		when(submissionService.findForTaskAndInternOrNull(taskId, internId)).thenReturn(submission);
		when(aiDraftAssessmentService.generateDraft(submissionId)).thenReturn("Draft text");

		mockMvc.perform(
				post("/tasks/" + taskId + "/submissions/" + internId + "/ai-draft-assessment").with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/tasks/" + taskId + "/submissions/" + internId));

		verify(aiDraftAssessmentService).generateDraft(submissionId);
	}

	@Test
	@WithMockUser(roles = "INTERN")
	void internIsForbiddenFromAiDraft() throws Exception {
		UUID taskId = UUID.randomUUID();
		UUID internId = UUID.randomUUID();
		mockMvc.perform(
				post("/tasks/" + taskId + "/submissions/" + internId + "/ai-draft-assessment").with(csrf()))
			.andExpect(status().isForbidden());
	}
}
