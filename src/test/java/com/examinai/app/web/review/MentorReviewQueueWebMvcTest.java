package com.examinai.app.web.review;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.examinai.app.config.SecurityConfig;
import com.examinai.app.security.RoleBasedAuthenticationSuccessHandler;
import com.examinai.app.service.MentorReviewService;

import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(MentorReviewQueueController.class)
@Import({ SecurityConfig.class, RoleBasedAuthenticationSuccessHandler.class })
class MentorReviewQueueWebMvcTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private MentorReviewService mentorReviewService;

	@Test
	@WithMockUser(roles = "MENTOR")
	void mentorCanOpenQueue() throws Exception {
		when(mentorReviewService.listQueue()).thenReturn(List.of());
		mockMvc.perform(get("/review/queue")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "INTERN")
	void internGetsForbidden() throws Exception {
		mockMvc.perform(get("/review/queue")).andExpect(status().isForbidden());
	}
}
