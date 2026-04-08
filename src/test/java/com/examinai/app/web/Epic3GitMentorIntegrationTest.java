package com.examinai.app.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.examinai.app.domain.task.SubmissionStatus;
import com.examinai.app.domain.user.RoleRepository;
import com.examinai.app.domain.user.User;
import com.examinai.app.domain.user.UserRepository;
import com.examinai.app.integration.git.GitFailureKind;
import com.examinai.app.integration.git.GitProviderException;
import com.examinai.app.integration.git.GitSourceClient;
import com.examinai.app.service.SubmissionService;
import com.examinai.app.service.TaskAssignmentService;
import com.examinai.app.service.TaskService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class Epic3GitMentorIntegrationTest {

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
	private GitSourceClient gitSourceClient;

	private User intern;

	private java.util.UUID taskId;

	@BeforeEach
	void seed() {
		intern = userRepository.findByEmail("epic3-intern@examinai.local").orElseGet(() -> {
			User u = new User("epic3-intern@examinai.local", passwordEncoder.encode("Epic3Intern!9"));
			u.addRole(roleRepository.findByName("intern").orElseThrow());
			return userRepository.save(u);
		});
		userRepository.findByEmail("epic3-mentor@examinai.local").orElseGet(() -> {
			User m = new User("epic3-mentor@examinai.local", passwordEncoder.encode("Epic3Mentor!9"));
			m.addRole(roleRepository.findByName("mentor").orElseThrow());
			return userRepository.save(m);
		});
		taskId = taskService.create("Epic3 git task", "Submit code.", java.time.LocalDate.now().plusWeeks(1)).getId();
		taskAssignmentService.replaceAssignmentsForTask(taskId, List.of(intern.getId()));
		submissionService.upsertCoordinates(taskId, intern.getId(), "octocat/Hello-World", "main", "README.md", SubmissionStatus.SUBMITTED);
	}

	@Test
	void mentorCanOpenSubmissionsList() throws Exception {
		var login = mockMvc.perform(formLogin("/login").user("epic3-mentor@examinai.local").password("Epic3Mentor!9"))
			.andExpect(authenticated())
			.andReturn();
		var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();
		mockMvc.perform(get("/tasks/" + taskId + "/submissions").session(session)).andExpect(status().isOk());
	}

	@Test
	void internForbiddenFromMentorSubmissions() throws Exception {
		var login = mockMvc.perform(formLogin("/login").user("epic3-intern@examinai.local").password("Epic3Intern!9"))
			.andExpect(authenticated())
			.andReturn();
		var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();
		mockMvc.perform(get("/tasks/" + taskId + "/submissions").session(session)).andExpect(status().isForbidden());
	}

	@Test
	void refetchFailurePageOmitsTokenLikeSubstring() throws Exception {
		when(gitSourceClient.fetchNormalizedFileContent(anyString(), anyString(), any())).thenThrow(
			new GitProviderException(GitFailureKind.ACCESS_DENIED, "Access was denied by the Git host."));
		var login = mockMvc.perform(formLogin("/login").user("epic3-mentor@examinai.local").password("Epic3Mentor!9"))
			.andExpect(authenticated())
			.andReturn();
		var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();
		mockMvc.perform(
				post("/tasks/" + taskId + "/submissions/" + intern.getId() + "/fetch").with(csrf()).session(session))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/tasks/" + taskId + "/submissions/" + intern.getId()));
		MvcResult page = mockMvc.perform(get("/tasks/" + taskId + "/submissions/" + intern.getId()).session(session))
			.andExpect(status().isOk())
			.andReturn();
		String html = page.getResponse().getContentAsString();
		assertThat(html).doesNotContain("ghp_").doesNotContain("Bearer ");
	}
}
