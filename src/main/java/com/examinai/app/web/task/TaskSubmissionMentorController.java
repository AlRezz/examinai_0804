package com.examinai.app.web.task;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.examinai.app.domain.review.PublishedReview;
import com.examinai.app.domain.task.GitRetrievalState;
import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionStatus;
import com.examinai.app.domain.user.UserRepository;
import com.examinai.app.integration.ai.AiDraftAssessmentProperties;
import com.examinai.app.integration.ai.AiDraftAssessmentService;
import com.examinai.app.integration.ai.InferenceUnavailableException;
import com.examinai.app.service.MentorReviewService;
import com.examinai.app.service.SourceRetrievalService;
import com.examinai.app.service.SubmissionService;
import com.examinai.app.service.TaskAssignmentService;
import com.examinai.app.service.TaskService;
import com.examinai.app.web.intern.SubmissionForm;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/tasks")
public class TaskSubmissionMentorController {

	private final TaskService taskService;

	private final TaskAssignmentService taskAssignmentService;

	private final SubmissionService submissionService;

	private final SourceRetrievalService sourceRetrievalService;

	private final MentorReviewService mentorReviewService;

	private final AiDraftAssessmentService aiDraftAssessmentService;

	private final AiDraftAssessmentProperties aiDraftAssessmentProperties;

	private final UserRepository userRepository;

	public TaskSubmissionMentorController(TaskService taskService, TaskAssignmentService taskAssignmentService,
			SubmissionService submissionService, SourceRetrievalService sourceRetrievalService,
			MentorReviewService mentorReviewService, AiDraftAssessmentService aiDraftAssessmentService,
			AiDraftAssessmentProperties aiDraftAssessmentProperties, UserRepository userRepository) {
		this.taskService = taskService;
		this.taskAssignmentService = taskAssignmentService;
		this.submissionService = submissionService;
		this.sourceRetrievalService = sourceRetrievalService;
		this.mentorReviewService = mentorReviewService;
		this.aiDraftAssessmentService = aiDraftAssessmentService;
		this.aiDraftAssessmentProperties = aiDraftAssessmentProperties;
		this.userRepository = userRepository;
	}

	@GetMapping("/{taskId}/submissions")
	public String list(@PathVariable UUID taskId, Model model) {
		model.addAttribute("task", taskService.requireTask(taskId));
		List<Row> rows = taskAssignmentService.listForTask(taskId)
			.stream()
			.map(a -> {
				Submission s = submissionService.findForTaskAndInternOrNull(taskId, a.getIntern().getId());
				return new Row(a.getIntern().getId(), a.getIntern().getEmail(), s, summarizeRetrieval(s));
			})
			.collect(Collectors.toList());
		model.addAttribute("rows", rows);
		return "tasks/submissions";
	}

	@GetMapping("/{taskId}/submissions/{internId}")
	public String detail(@PathVariable UUID taskId, @PathVariable UUID internId, Model model) {
		taskService.requireTask(taskId);
		requireAssignment(taskId, internId);
		model.addAttribute("task", taskService.requireTask(taskId));
		model.addAttribute("internId", internId);
		Submission submission = submissionService.findForTaskAndInternOrNull(taskId, internId);
		model.addAttribute("submission", submission);
		if (!model.containsAttribute("submissionForm")) {
			SubmissionForm form = new SubmissionForm();
			if (submission != null) {
				form.setRepoIdentifier(submission.getRepoIdentifier());
				form.setCommitSha(submission.getCommitSha());
				form.setPathScope(submission.getPathScope());
			}
			model.addAttribute("submissionForm", form);
		}
		if (submission != null && submission.getGitRetrievalState() == GitRetrievalState.ERROR) {
			model.addAttribute("gitRetrievalMessage", GitRetrievalUiMessage.forErrorCode(submission.getGitRetrievalErrorCode()));
		}
		if (submission != null) {
			List<PublishedReview> history = mentorReviewService.listPublishedHistory(submission.getId());
			model.addAttribute("publishedReviewHistory", history);
			if (!model.containsAttribute("mentorReviewForm")) {
				model.addAttribute("mentorReviewForm", mentorReviewFormFromDraft(submission.getId()));
			}
		}
		return "tasks/submission-detail";
	}

	@PostMapping("/{taskId}/submissions/{internId}/coordinates")
	public String saveCoordinates(@PathVariable UUID taskId, @PathVariable UUID internId,
			@Valid @ModelAttribute("submissionForm") SubmissionForm form, BindingResult binding, Model model,
			RedirectAttributes redirectAttributes) {
		taskService.requireTask(taskId);
		requireAssignment(taskId, internId);
		if (binding.hasErrors()) {
			populateDetailModel(taskId, internId, model);
			return "tasks/submission-detail";
		}
		try {
			submissionService.mentorUpsertCoordinates(taskId, internId, form.getRepoIdentifier(), form.getCommitSha(),
					form.getPathScope(), SubmissionStatus.SUBMITTED);
			redirectAttributes.addFlashAttribute("submissionNotice", "Coordinates updated.");
		}
		catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("submissionError", ex.getMessage());
		}
		return "redirect:/tasks/" + taskId + "/submissions/" + internId;
	}

	@PostMapping("/{taskId}/submissions/{internId}/fetch")
	public String refetch(@PathVariable UUID taskId, @PathVariable UUID internId, RedirectAttributes redirectAttributes) {
		taskService.requireTask(taskId);
		requireAssignment(taskId, internId);
		Submission submission = submissionService.findForTaskAndInternOrNull(taskId, internId);
		if (submission == null) {
			redirectAttributes.addFlashAttribute("submissionError", "Save submission coordinates before fetching source.");
			return "redirect:/tasks/" + taskId + "/submissions/" + internId;
		}
		sourceRetrievalService.retrieveAndPersist(submission.getId());
		redirectAttributes.addFlashAttribute("submissionNotice", "Fetch completed. Review status below.");
		return "redirect:/tasks/" + taskId + "/submissions/" + internId;
	}

	@PostMapping("/{taskId}/submissions/{internId}/ai-draft-assessment")
	public String generateAiDraft(@PathVariable UUID taskId, @PathVariable UUID internId, RedirectAttributes redirectAttributes) {
		taskService.requireTask(taskId);
		requireAssignment(taskId, internId);
		Submission submission = submissionService.findForTaskAndInternOrNull(taskId, internId);
		if (submission == null) {
			redirectAttributes.addFlashAttribute("reviewError", "Save submission coordinates before requesting an AI draft.");
			return "redirect:/tasks/" + taskId + "/submissions/" + internId;
		}
		try {
			String draft = aiDraftAssessmentService.generateDraft(submission.getId());
			redirectAttributes.addFlashAttribute("aiDraftAssessment", truncateForFlash(draft));
			redirectAttributes.addFlashAttribute("submissionNotice",
					"AI draft generated below. Assistive only—you retain final judgment.");
		}
		catch (NoSuchElementException ex) {
			redirectAttributes.addFlashAttribute("reviewError", "Submission not found. It may have been removed.");
		}
		catch (IllegalStateException ex) {
			redirectAttributes.addFlashAttribute("reviewError", ex.getMessage());
		}
		catch (InferenceUnavailableException ex) {
			redirectAttributes.addFlashAttribute("reviewError", "AI draft unavailable. You can edit feedback manually. (" + ex.getMessage() + ")");
		}
		return "redirect:/tasks/" + taskId + "/submissions/" + internId;
	}

	@PostMapping("/{taskId}/submissions/{internId}/review-draft")
	public String saveReviewDraft(@PathVariable UUID taskId, @PathVariable UUID internId, Authentication authentication,
			@ModelAttribute("mentorReviewForm") MentorReviewForm form, RedirectAttributes redirectAttributes) {
		taskService.requireTask(taskId);
		requireAssignment(taskId, internId);
		Submission submission = submissionService.findForTaskAndInternOrNull(taskId, internId);
		if (submission == null) {
			redirectAttributes.addFlashAttribute("reviewError", "Save submission coordinates before working on a review.");
			return "redirect:/tasks/" + taskId + "/submissions/" + internId;
		}
		UUID mentorId = requireUserId(authentication);
		mentorReviewService.saveDraft(submission.getId(), mentorId, form.getQualityScore(), form.getReadabilityScore(),
				form.getCorrectnessScore(), form.getNarrativeFeedback());
		redirectAttributes.addFlashAttribute("submissionNotice", "Review draft saved.");
		return "redirect:/tasks/" + taskId + "/submissions/" + internId;
	}

	@PostMapping("/{taskId}/submissions/{internId}/publish-review")
	public String publishReview(@PathVariable UUID taskId, @PathVariable UUID internId, Authentication authentication,
			@ModelAttribute("mentorReviewForm") MentorReviewForm form, RedirectAttributes redirectAttributes) {
		taskService.requireTask(taskId);
		requireAssignment(taskId, internId);
		Submission submission = submissionService.findForTaskAndInternOrNull(taskId, internId);
		if (submission == null) {
			redirectAttributes.addFlashAttribute("reviewError", "Save submission coordinates before publishing.");
			return "redirect:/tasks/" + taskId + "/submissions/" + internId;
		}
		UUID mentorId = requireUserId(authentication);
		try {
			mentorReviewService.publish(submission.getId(), mentorId, form.getQualityScore(), form.getReadabilityScore(),
					form.getCorrectnessScore(), form.getNarrativeFeedback());
			redirectAttributes.addFlashAttribute("submissionNotice", "Official review published.");
		}
		catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("reviewError", ex.getMessage());
		}
		return "redirect:/tasks/" + taskId + "/submissions/" + internId;
	}

	public record Row(UUID internId, String internEmail, Submission submission, String retrievalSummary) {
	}

	private void populateDetailModel(UUID taskId, UUID internId, Model model) {
		model.addAttribute("task", taskService.requireTask(taskId));
		model.addAttribute("internId", internId);
		Submission submission = submissionService.findForTaskAndInternOrNull(taskId, internId);
		model.addAttribute("submission", submission);
		if (submission != null && submission.getGitRetrievalState() == GitRetrievalState.ERROR) {
			model.addAttribute("gitRetrievalMessage", GitRetrievalUiMessage.forErrorCode(submission.getGitRetrievalErrorCode()));
		}
		if (submission != null) {
			model.addAttribute("publishedReviewHistory", mentorReviewService.listPublishedHistory(submission.getId()));
			model.addAttribute("mentorReviewForm", mentorReviewFormFromDraft(submission.getId()));
		}
	}

	private MentorReviewForm mentorReviewFormFromDraft(UUID submissionId) {
		MentorReviewForm rf = new MentorReviewForm();
		var draft = mentorReviewService.findDraftOrNull(submissionId);
		if (draft != null) {
			rf.setQualityScore(draft.getQualityScore());
			rf.setReadabilityScore(draft.getReadabilityScore());
			rf.setCorrectnessScore(draft.getCorrectnessScore());
			rf.setNarrativeFeedback(draft.getNarrativeFeedback());
		}
		return rf;
	}

	private UUID requireUserId(Authentication authentication) {
		return userRepository.findByEmail(authentication.getName())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED))
			.getId();
	}

	private void requireAssignment(UUID taskId, UUID internId) {
		if (taskAssignmentService.listForTask(taskId).stream().noneMatch(a -> a.getIntern().getId().equals(internId))) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Intern is not assigned to this task.");
		}
	}

	private static String summarizeRetrieval(Submission s) {
		if (s == null) {
			return "No coordinates";
		}
		return switch (s.getGitRetrievalState()) {
			case NOT_STARTED -> "Not fetched";
			case IN_PROGRESS -> "In progress";
			case OK -> "OK";
			case ERROR -> GitRetrievalUiMessage.forErrorCode(s.getGitRetrievalErrorCode());
		};
	}

	private String truncateForFlash(String draft) {
		int max = aiDraftAssessmentProperties.getMaxFlashChars();
		if (draft.length() <= max) {
			return draft;
		}
		return draft.substring(0, max) + "\n\n[... truncated for display/session size; persist drafts in story 5.2 ...]";
	}
}
