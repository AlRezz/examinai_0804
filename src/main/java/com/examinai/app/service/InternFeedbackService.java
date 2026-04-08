package com.examinai.app.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examinai.app.config.InternUiProperties;
import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionRepository;

@Service
public class InternFeedbackService {

	private final SubmissionRepository submissionRepository;

	private final SubmissionLifecycleService submissionLifecycleService;

	private final MentorReviewService mentorReviewService;

	private final AiDraftPersistenceService aiDraftPersistenceService;

	private final InternUiProperties internUiProperties;

	public InternFeedbackService(SubmissionRepository submissionRepository, SubmissionLifecycleService submissionLifecycleService,
			MentorReviewService mentorReviewService, AiDraftPersistenceService aiDraftPersistenceService,
			InternUiProperties internUiProperties) {
		this.submissionRepository = submissionRepository;
		this.submissionLifecycleService = submissionLifecycleService;
		this.mentorReviewService = mentorReviewService;
		this.aiDraftPersistenceService = aiDraftPersistenceService;
		this.internUiProperties = internUiProperties;
	}

	@Transactional(readOnly = true)
	public Optional<InternFeedbackBundle> loadFeedbackForIntern(UUID submissionId, UUID internUserId) {
		Optional<Submission> opt = submissionRepository.findByIdAndIntern_IdWithTask(submissionId,
				internUserId);
		if (opt.isEmpty()) {
			return Optional.empty();
		}
		var submission = opt.get();
		var task = submission.getTask();
		var lifecycle = submissionLifecycleService.viewForIntern(submission);
		var official = mentorReviewService.findLatestPublishedForCurrentRevision(submission);
		Optional<AiDraftView> ai = internUiProperties.isShowAiDraftToIntern()
				? aiDraftPersistenceService.findLatestForSubmission(submission.getId()) : Optional.empty();
		return Optional.of(new InternFeedbackBundle(submission.getId(), task, submission, lifecycle, official, ai));
	}
}
