package com.examinai.app.service;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionRepository;

@Service
public class InternFeedbackService {

	private static final Logger log = LoggerFactory.getLogger(InternFeedbackService.class);

	private final SubmissionRepository submissionRepository;

	private final SubmissionLifecycleService submissionLifecycleService;

	private final MentorReviewService mentorReviewService;

	public InternFeedbackService(SubmissionRepository submissionRepository, SubmissionLifecycleService submissionLifecycleService,
			MentorReviewService mentorReviewService) {
		this.submissionRepository = submissionRepository;
		this.submissionLifecycleService = submissionLifecycleService;
		this.mentorReviewService = mentorReviewService;
	}

	@Transactional(readOnly = true)
	public Optional<InternFeedbackBundle> loadFeedbackForIntern(UUID submissionId, UUID internUserId) {
		log.debug("loadFeedbackForIntern: submissionId={}, internUserId={}", submissionId, internUserId);
		Optional<Submission> opt = submissionRepository.findByIdAndIntern_IdWithTask(submissionId,
				internUserId);
		if (opt.isEmpty()) {
			return Optional.empty();
		}
		var submission = opt.get();
		var task = submission.getTask();
		var lifecycle = submissionLifecycleService.viewForIntern(submission);
		var official = mentorReviewService.findLatestPublishedForCurrentRevision(submission);
		return Optional.of(new InternFeedbackBundle(submission.getId(), task, submission, lifecycle, official, Optional.empty()));
	}
}
