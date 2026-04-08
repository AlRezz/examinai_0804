package com.examinai.app.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.examinai.app.domain.user.RoleRepository;
import com.examinai.app.domain.user.User;
import com.examinai.app.domain.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginAndSecurityIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void internAccountForAccessTests() {
		if (userRepository.findByEmail("itest-intern@examinai.local").isEmpty()) {
			User intern = new User("itest-intern@examinai.local", passwordEncoder.encode("ItestIntern!8"));
			intern.addRole(roleRepository.findByName("intern").orElseThrow());
			userRepository.save(intern);
		}
		if (userRepository.findByEmail("itest-mentor@examinai.local").isEmpty()) {
			User mentor = new User("itest-mentor@examinai.local", passwordEncoder.encode("ItestMentor!8"));
			mentor.addRole(roleRepository.findByName("mentor").orElseThrow());
			userRepository.save(mentor);
		}
	}

	@Test
	void loginSuccessRedirectsAdministratorToAdminArea() throws Exception {
		mockMvc.perform(
				formLogin("/login").user("admin@examinai.local").password("ChangeMe!Dev1"))
			.andExpect(authenticated())
			.andExpect(redirectedUrl("/admin/users"));
	}

	@Test
	void loginSuccessRedirectsInternToAssignedTasksHome() throws Exception {
		mockMvc.perform(
				formLogin("/login").user("itest-intern@examinai.local").password("ItestIntern!8"))
			.andExpect(authenticated())
			.andExpect(redirectedUrl("/intern/tasks"));
	}

	@Test
	void loginSuccessRedirectsMentorToTasks() throws Exception {
		mockMvc.perform(
				formLogin("/login").user("itest-mentor@examinai.local").password("ItestMentor!8"))
			.andExpect(authenticated())
			.andExpect(redirectedUrl("/tasks"));
	}

	@Test
	void protectedRouteRedirectsAnonymousUserToLogin() throws Exception {
		var result = mockMvc.perform(get("/home")).andExpect(status().is3xxRedirection()).andReturn();
		assertThat(result.getResponse().getRedirectedUrl()).contains("/login");
	}

	@Test
	void logoutClearsSessionAndBlocksProtectedRoute() throws Exception {
		var login = mockMvc.perform(
				formLogin("/login").user("itest-intern@examinai.local").password("ItestIntern!8"))
			.andExpect(authenticated())
			.andReturn();
		MockHttpSession session = (MockHttpSession) login.getRequest().getSession();
		mockMvc.perform(post("/logout").session(session).with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?logout"));
		var afterLogout = mockMvc.perform(get("/app/secure")).andExpect(status().is3xxRedirection()).andReturn();
		assertThat(afterLogout.getResponse().getRedirectedUrl()).contains("/login");
	}

	@Test
	void internReceivesForbiddenForAdminRoutes() throws Exception {
		var login = mockMvc.perform(
				formLogin("/login").user("itest-intern@examinai.local").password("ItestIntern!8"))
			.andExpect(authenticated())
			.andReturn();
		MockHttpSession session = (MockHttpSession) login.getRequest().getSession();
		mockMvc.perform(get("/admin/users").session(session)).andExpect(status().isForbidden());
	}

	@Test
	void mutatingAdminPostWithoutCsrfIsForbidden() throws Exception {
		var login = mockMvc.perform(
				formLogin("/login").user("admin@examinai.local").password("ChangeMe!Dev1"))
			.andExpect(authenticated())
			.andReturn();
		MockHttpSession session = (MockHttpSession) login.getRequest().getSession();
		mockMvc.perform(
				post("/admin/users/new").session(session).contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("email", "csrf-test@examinai.local")
					.param("password", "CsrfTestPw1")
					.param("roles", "intern"))
			.andExpect(status().isForbidden());
	}

	@Test
	void invalidCredentialsDoNotAuthenticate() throws Exception {
		var result = mockMvc.perform(
				formLogin("/login").user("admin@examinai.local").password("wrong-password"))
			.andExpect(status().is3xxRedirection())
			.andReturn();
		assertThat(result.getResponse().getRedirectedUrl()).contains("error");
	}
}
