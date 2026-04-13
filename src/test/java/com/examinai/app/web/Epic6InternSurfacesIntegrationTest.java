package com.examinai.app.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

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

import com.examinai.app.domain.task.GitRetrievalState;
import com.examinai.app.domain.task.SubmissionRepository;
import com.examinai.app.domain.task.SubmissionStatus;
import com.examinai.app.domain.user.RoleRepository;
import com.examinai.app.domain.user.User;
import com.examinai.app.domain.user.UserRepository;
import com.examinai.app.service.AiDraftPersistenceService;
import com.examinai.app.service.SubmissionService;
import com.examinai.app.service.TaskAssignmentService;
import com.examinai.app.service.TaskService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class Epic6InternSurfacesIntegrationTest {

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

	private User internA;

	private User internB;

	private UUID taskIdA;

	private UUID taskIdB;

	@BeforeEach
	void seed() {
		internA = userRepository.findByEmail("epic6-a@examinai.local").orElseGet(() -> {
			User u = new User("epic6-a@examinai.local", passwordEncoder.encode("Epic6InternA!9"));
			u.addRole(roleRepository.findByName("intern").orElseThrow());
			return userRepository.save(u);
		});
		internB = userRepository.findByEmail("epic6-b@examinai.local").orElseGet(() -> {
			User u = new User("epic6-b@examinai.local", passwordEncoder.encode("Epic6InternB!9"));
			u.addRole(roleRepository.findByName("intern").orElseThrow());
			return userRepository.save(u);
		});
		userRepository.findByEmail("epic6-mentor@examinai.local").orElseGet(() -> {
			User m = new User("epic6-mentor@examinai.local", passwordEncoder.encode("Epic6Mentor!9"));
			m.addRole(roleRepository.findByName("mentor").orElseThrow());
			return userRepository.save(m);
		});
		taskIdA = taskService.create("Epic6 task A", "Do A.", java.time.LocalDate.now().plusDays(2)).getId();
		taskIdB = taskService.create("Epic6 task B", "Do B.", java.time.LocalDate.now().plusDays(2)).getId();
		taskAssignmentService.replaceAssignmentsForTask(taskIdA, List.of(internA.getId()));
		taskAssignmentService.replaceAssignmentsForTask(taskIdB, List.of(internB.getId()));
		submissionService.upsertCoordinates(taskIdA, internA.getId(), "a/repo", "aaa111", "README.md", SubmissionStatus.SUBMITTED);
		submissionService.upsertCoordinates(taskIdB, internB.getId(), "b/repo", "bbb222", "README.md", SubmissionStatus.SUBMITTED);
	}

	@Test
	void internCannotOpenAnotherInternsFeedbackBySubmissionId() throws Exception {
		var subB = submissionRepository.findByTask_IdAndIntern_Id(taskIdB, internB.getId()).orElseThrow();
		var login = mockMvc.perform(formLogin("/login").user("epic6-a@examinai.local").password("Epic6InternA!9"))
			.andReturn();
		var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();
		mockMvc.perform(get("/intern/submissions/" + subB.getId() + "/feedback").session(session)).andExpect(status().isNotFound());
	}

	@Test
	void internListShowsLifecycleBadge() throws Exception {
		var login = mockMvc.perform(formLogin("/login").user("epic6-a@examinai.local").password("Epic6InternA!9"))
			.andReturn();
		var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();
		MvcResult res = mockMvc.perform(get("/intern/tasks").session(session)).andExpect(status().isOk()).andReturn();
		assertThat(res.getResponse().getContentAsString()).contains("Awaiting mentor review");
	}

	@Test
	void mentorPublish_makesScoresVisibleToInternForCurrentRevision() throws Exception {
		var mentorLogin = mockMvc.perform(formLogin("/login").user("epic6-mentor@examinai.local").password("Epic6Mentor!9"))
			.andReturn();
		var mentorSession = (org.springframework.mock.web.MockHttpSession) mentorLogin.getRequest().getSession();
		mockMvc.perform(postPublish(taskIdA, internA.getId(), mentorSession)).andExpect(status().is3xxRedirection());

		var login = mockMvc.perform(formLogin("/login").user("epic6-a@examinai.local").password("Epic6InternA!9"))
			.andReturn();
		var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();
		var subA = submissionRepository.findByTask_IdAndIntern_Id(taskIdA, internA.getId()).orElseThrow();
		MvcResult page = mockMvc.perform(get("/intern/submissions/" + subA.getId() + "/feedback").session(session))
			.andExpect(status().isOk())
			.andReturn();
		String html = page.getResponse().getContentAsString();
		assertThat(html).contains("Official mentor feedback").contains("Mentor sentence for epic6.");
		assertThat(html).contains("Outcome published");
	}

	@Test
	void internFeedbackShowsOfficialOnly_aiAssistiveDraftNotListedOnPage() throws Exception {
		var subAUpsert = submissionRepository.findByTask_IdAndIntern_Id(taskIdA, internA.getId()).orElseThrow();
		subAUpsert.setGitRetrievalState(GitRetrievalState.OK);
		subAUpsert.setGitRetrievedText("source line");
		submissionRepository.save(subAUpsert);
		aiDraftPersistenceService.persistSuccessfulDraft(subAUpsert.getId(), "AI draft prose for epic6 label test.");

		var mentorLogin = mockMvc.perform(formLogin("/login").user("epic6-mentor@examinai.local").password("Epic6Mentor!9"))
			.andReturn();
		var mentorSession = (org.springframework.mock.web.MockHttpSession) mentorLogin.getRequest().getSession();
		mockMvc.perform(postPublish(taskIdA, internA.getId(), mentorSession)).andExpect(status().is3xxRedirection());

		var login = mockMvc.perform(formLogin("/login").user("epic6-a@examinai.local").password("Epic6InternA!9")).andReturn();
		var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();
		var subA = submissionRepository.findByTask_IdAndIntern_Id(taskIdA, internA.getId()).orElseThrow();
		MvcResult page = mockMvc.perform(get("/intern/submissions/" + subA.getId() + "/feedback").session(session))
			.andExpect(status().isOk())
			.andReturn();
		String html = page.getResponse().getContentAsString();
		assertThat(html).contains("Official mentor feedback").contains("Mentor sentence for epic6.");
		assertThat(html).doesNotContain("AI draft prose for epic6 label test.").doesNotContain("Draft — not official");
	}

	@Test
	void afterResubmit_newRevisionHasNoOfficialUntilRepublish() throws Exception {
		var mentorLogin = mockMvc.perform(formLogin("/login").user("epic6-mentor@examinai.local").password("Epic6Mentor!9"))
			.andReturn();
		var mentorSession = (org.springframework.mock.web.MockHttpSession) mentorLogin.getRequest().getSession();
		mockMvc.perform(postPublish(taskIdA, internA.getId(), mentorSession)).andExpect(status().is3xxRedirection());
		submissionService.upsertCoordinates(taskIdA, internA.getId(), "a/repo", "newsha99", "README.md", SubmissionStatus.SUBMITTED);

		var login = mockMvc.perform(formLogin("/login").user("epic6-a@examinai.local").password("Epic6InternA!9"))
			.andReturn();
		var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();
		var subA = submissionRepository.findByTask_IdAndIntern_Id(taskIdA, internA.getId()).orElseThrow();
		MvcResult page = mockMvc.perform(get("/intern/submissions/" + subA.getId() + "/feedback").session(session))
			.andExpect(status().isOk())
			.andReturn();
		assertThat(page.getResponse().getContentAsString()).contains("No official grade yet");
	}

	private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder postPublish(UUID taskId, UUID internId,
			org.springframework.mock.web.MockHttpSession mentorSession) {
		return org.springframework.test.web.servlet.request.MockMvcRequestBuilders
			.post("/tasks/" + taskId + "/submissions/" + internId + "/publish-review")
			.with(csrf())
			.session(mentorSession)
			.param("qualityScore", "4")
			.param("readabilityScore", "4")
			.param("correctnessScore", "4")
			.param("narrativeFeedback", "Mentor sentence for epic6.");
	}
}
