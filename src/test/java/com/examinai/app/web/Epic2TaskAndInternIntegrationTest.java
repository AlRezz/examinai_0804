package com.examinai.app.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.examinai.app.domain.task.SubmissionStatus;
import com.examinai.app.domain.user.RoleRepository;
import com.examinai.app.domain.user.User;
import com.examinai.app.domain.user.UserRepository;
import com.examinai.app.service.InternTaskService;
import com.examinai.app.service.SubmissionService;
import com.examinai.app.service.TaskAssignmentService;
import com.examinai.app.service.TaskService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class Epic2TaskAndInternIntegrationTest {

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
	private InternTaskService internTaskService;

	@Autowired
	private SubmissionService submissionService;

	private User internA;

	private User internB;

	private UUID taskId;

	@BeforeEach
	void seedUsersAndTask() {
		internA = userRepository.findByEmail("epic2-intern-a@examinai.local").orElseGet(() -> {
			User u = new User("epic2-intern-a@examinai.local", passwordEncoder.encode("Epic2InternA!9"));
			u.addRole(roleRepository.findByName("intern").orElseThrow());
			return userRepository.save(u);
		});
		internB = userRepository.findByEmail("epic2-intern-b@examinai.local").orElseGet(() -> {
			User u = new User("epic2-intern-b@examinai.local", passwordEncoder.encode("Epic2InternB!9"));
			u.addRole(roleRepository.findByName("intern").orElseThrow());
			return userRepository.save(u);
		});
		userRepository.findByEmail("epic2-mentor@examinai.local").orElseGet(() -> {
			User m = new User("epic2-mentor@examinai.local", passwordEncoder.encode("Epic2Mentor!9"));
			m.addRole(roleRepository.findByName("mentor").orElseThrow());
			return userRepository.save(m);
		});
		var task = taskService.create("Epic2 isolation task", "Do the thing.", java.time.LocalDate.now().plusWeeks(2));
		taskId = task.getId();
		taskAssignmentService.replaceAssignmentsForTask(taskId, List.of(internA.getId()));
	}

	@Test
	void internSeesOnlyAssignedTasksInServiceLayer() {
		assertThat(internTaskService.listAssignedTasksForIntern(internA.getId())).anyMatch(t -> t.getId().equals(taskId));
		assertThat(internTaskService.listAssignedTasksForIntern(internB.getId())).noneMatch(t -> t.getId().equals(taskId));
	}

	@Test
	void submissionRejectedWhenInternNotAssigned() {
		assertThatThrownBy(() -> submissionService.upsertCoordinates(taskId, internB.getId(), "https://git.example/repo.git",
				"abcdef0", null, SubmissionStatus.SUBMITTED)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Not assigned");
	}

	@Test
	void internCannotOpenTaskRoutesReservedForMentor() throws Exception {
		var login = mockMvc.perform(formLogin("/login").user("epic2-intern-a@examinai.local").password("Epic2InternA!9"))
			.andExpect(authenticated())
			.andExpect(redirectedUrl("/intern/tasks"))
			.andReturn();
		mockMvc.perform(get("/tasks").session((org.springframework.mock.web.MockHttpSession) login.getRequest().getSession()))
			.andExpect(status().isForbidden());
	}

	@Test
	void unassignedInternGetsNotFoundOnPeerTask() throws Exception {
		var login = mockMvc.perform(formLogin("/login").user("epic2-intern-b@examinai.local").password("Epic2InternB!9"))
			.andExpect(authenticated())
			.andReturn();
		var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();
		mockMvc.perform(get("/intern/tasks/" + taskId).session(session)).andExpect(status().isNotFound());
	}

	@Test
	void mentorCannotOpenInternTaskList() throws Exception {
		var login = mockMvc.perform(formLogin("/login").user("epic2-mentor@examinai.local").password("Epic2Mentor!9"))
		               .andExpect(authenticated())
		               .andReturn();
		mockMvc.perform(
				get("/intern/tasks").session((org.springframework.mock.web.MockHttpSession) login.getRequest().getSession()))
			.andExpect(status().isForbidden());
	}
}
