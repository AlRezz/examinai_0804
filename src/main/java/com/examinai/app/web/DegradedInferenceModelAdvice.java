package com.examinai.app.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.examinai.app.web.review.MentorReviewQueueController;
import com.examinai.app.web.task.TaskSubmissionMentorController;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice(assignableTypes = { TaskSubmissionMentorController.class, MentorReviewQueueController.class })
public class DegradedInferenceModelAdvice {

	@ModelAttribute
	public void attachDegradedInference(HttpSession session, Model model) {
		model.addAttribute("degradedInference",
				Boolean.TRUE.equals(session.getAttribute(DegradedInferenceAttributes.SESSION_KEY)));
	}
}
