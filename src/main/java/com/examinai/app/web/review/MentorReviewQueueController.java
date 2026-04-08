package com.examinai.app.web.review;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionStatus;
import com.examinai.app.service.MentorReviewService;

@Controller
@RequestMapping("/review")
public class MentorReviewQueueController {

	private final MentorReviewService mentorReviewService;

	public MentorReviewQueueController(MentorReviewService mentorReviewService) {
		this.mentorReviewService = mentorReviewService;
	}

	@GetMapping("/queue")
	public String queue(Model model) {
		List<Submission> submissions = mentorReviewService.listQueue();
		List<QueueRow> rows = submissions.stream().map(this::toRow).toList();
		model.addAttribute("rows", rows);
		return "review/queue";
	}

	private QueueRow toRow(Submission s) {
		return new QueueRow(s.getTask().getId(), s.getTask().getTitle(), s.getTask().getDueDate(), s.getIntern().getId(),
				s.getIntern().getEmail(), s.getId(), s.getStatus(), summarizeRetrieval(s));
	}

	private static String statusLabel(SubmissionStatus status) {
		return switch (status) {
			case DRAFT -> "Draft (intern)";
			case SUBMITTED -> "Awaiting review";
			case UNDER_REVIEW -> "In progress";
			case OUTCOME_PUBLISHED -> "Published";
		};
	}

	private static String summarizeRetrieval(Submission s) {
		return switch (s.getGitRetrievalState()) {
			case NOT_STARTED -> "Not fetched";
			case IN_PROGRESS -> "In progress";
			case OK -> "Source OK";
			case ERROR -> "Retrieval error";
		};
	}

	public record QueueRow(UUID taskId, String taskTitle, LocalDate dueDate, UUID internId, String internEmail,
			UUID submissionId, SubmissionStatus submissionStatus, String retrievalSummary) {

		public String mentorStatusLabel() {
			return MentorReviewQueueController.statusLabel(submissionStatus);
		}
	}
}
