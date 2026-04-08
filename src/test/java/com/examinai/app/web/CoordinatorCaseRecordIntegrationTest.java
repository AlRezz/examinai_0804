package com.examinai.app.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

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
class CoordinatorCaseRecordIntegrationTest {

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

	private User intern;

	private UUID taskId;

	@BeforeEach
	void seed() {
		userRepository.findByEmail("itest-coordinator@examinai.local").orElseGet(() -> {
			User c = new User("itest-coordinator@examinai.local", passwordEncoder.encode("ItestCoord!8"));
			c.addRole(roleRepository.findByName("coordinator").orElseThrow());
			return userRepository.save(c);
		});
		intern = userRepository.findByEmail("coord-case-intern@examinai.local").orElseGet(() -> {
			User u = new User("coord-case-intern@examinai.local", passwordEncoder.encode("CoordIntern!8"));
			u.addRole(roleRepository.findByName("intern").orElseThrow());
			return userRepository.save(u);
		});
		userRepository.findByEmail("coord-case-mentor@examinai.local").orElseGet(() -> {
			User m = new User("coord-case-mentor@examinai.local", passwordEncoder.encode("CoordMentor!8"));
			m.addRole(roleRepository.findByName("mentor").orElseThrow());
			return userRepository.save(m);
		});
		taskId = taskService.create("Coordinator case task", "Brief.", java.time.LocalDate.now().plusDays(2)).getId();
		taskAssignmentService.replaceAssignmentsForTask(taskId, List.of(intern.getId()));
		submissionService.upsertCoordinates(taskId, intern.getId(), "owner/repo-coord", "cafebabe", "README.md",
				SubmissionStatus.SUBMITTED);
	}

	@Test
	void coordinatorReceivesCaseRecordForSubmission() throws Exception {
		UUID subId = submissionService.findForTaskAndInternOrNull(taskId, intern.getId()).getId();
		var login = mockMvc.perform(formLogin("/login").user("itest-coordinator@examinai.local").password("ItestCoord!8"))
			.andExpect(authenticated())
			.andReturn();
		var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();
		var page = mockMvc.perform(get("/coordinator/cases/" + subId).session(session)).andExpect(status().isOk()).andReturn();
		String html = page.getResponse().getContentAsString();
		assertThat(html).contains("read-only").contains("owner/repo-coord").contains("cafebabe");
	}

	@Test
	void mentorForbiddenFromCoordinatorCaseRecord() throws Exception {
		UUID subId = submissionService.findForTaskAndInternOrNull(taskId, intern.getId()).getId();
		var login = mockMvc.perform(formLogin("/login").user("coord-case-mentor@examinai.local").password("CoordMentor!8"))
			.andExpect(authenticated())
			.andReturn();
		var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();
		mockMvc.perform(get("/coordinator/cases/" + subId).session(session)).andExpect(status().isForbidden());
	}

	@Test
	void internForbiddenFromCoordinatorCaseRecord() throws Exception {
		UUID subId = submissionService.findForTaskAndInternOrNull(taskId, intern.getId()).getId();
		var login = mockMvc.perform(formLogin("/login").user("coord-case-intern@examinai.local").password("CoordIntern!8"))
			.andExpect(authenticated())
			.andReturn();
		var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();
		mockMvc.perform(get("/coordinator/cases/" + subId).session(session)).andExpect(status().isForbidden());
	}

	@Test
	void coordinatorGetsNotFoundForUnknownSubmission() throws Exception {
		var login = mockMvc.perform(formLogin("/login").user("itest-coordinator@examinai.local").password("ItestCoord!8"))
			.andExpect(authenticated())
			.andReturn();
		var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();
		mockMvc.perform(get("/coordinator/cases/" + UUID.randomUUID()).session(session)).andExpect(status().isNotFound());
	}
}
