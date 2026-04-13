package com.examinai.app.web.intern;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.examinai.app.domain.user.UserRepository;
import com.examinai.app.service.InternFeedbackService;

@Controller
@RequestMapping("/intern/submissions")
public class InternSubmissionFeedbackController {

	private final UserRepository userRepository;

	private final InternFeedbackService internFeedbackService;

	public InternSubmissionFeedbackController(UserRepository userRepository, InternFeedbackService internFeedbackService) {
		this.userRepository = userRepository;
		this.internFeedbackService = internFeedbackService;
	}

	/**
	 * Official mentor outcome for the intern's current submission revision only; never exposes unpublished mentor rubric (Story 6.1).
	 * Cross-intern access returns 404 (Story 6.4).
	 */
	@GetMapping("/{submissionId}/feedback")
	public String feedback(@PathVariable UUID submissionId, Authentication authentication, Model model) {
		UUID internId = requireUserId(authentication);
		var bundle = internFeedbackService.loadFeedbackForIntern(submissionId, internId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		model.addAttribute("bundle", bundle);
		model.addAttribute("task", bundle.task());
		model.addAttribute("submission", bundle.submission());
		model.addAttribute("submissionLifecycle", bundle.submissionLifecycle());
		var official = bundle.officialForCurrentRevision().orElse(null);
		model.addAttribute("officialReview", official);
		if (official != null) {
			model.addAttribute("officialFeedbackCardClass", OfficialFeedbackCardSupport.cssClass(official));
		}
		return "intern/submissions/feedback";
	}

	private UUID requireUserId(Authentication authentication) {
		return userRepository.findByEmail(authentication.getName())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED))
			.getId();
	}
}
