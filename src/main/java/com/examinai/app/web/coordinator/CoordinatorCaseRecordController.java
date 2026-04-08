package com.examinai.app.web.coordinator;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.examinai.app.service.CoordinatorCaseRecordService;

@Controller
@RequestMapping("/coordinator")
public class CoordinatorCaseRecordController {

	private final CoordinatorCaseRecordService coordinatorCaseRecordService;

	public CoordinatorCaseRecordController(CoordinatorCaseRecordService coordinatorCaseRecordService) {
		this.coordinatorCaseRecordService = coordinatorCaseRecordService;
	}

	@GetMapping
	public String index() {
		return "coordinator/index";
	}

	/**
	 * Read-only audit record for one submission (Story 7.1). No mutating endpoints.
	 */
	@GetMapping("/cases/{submissionId}")
	public String caseRecord(@PathVariable UUID submissionId, Model model) {
		var record = coordinatorCaseRecordService.findBySubmissionId(submissionId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
		model.addAttribute("submission", record.submission());
		model.addAttribute("gitRetrievalMessage", record.gitRetrievalMessage());
		model.addAttribute("latestAiDraft", record.latestAiDraft());
		model.addAttribute("mentorDraft", record.mentorDraft());
		model.addAttribute("publishedReviewHistory", record.publishedHistory());
		model.addAttribute("publishedForCurrentRevision", record.publishedForCurrentRevision().orElse(null));
		return "coordinator/case-record";
	}

}
