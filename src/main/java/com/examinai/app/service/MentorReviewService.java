package com.examinai.app.service;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.examinai.app.domain.review.MentorReviewDraft;
import com.examinai.app.domain.review.MentorReviewDraftRepository;
import com.examinai.app.domain.review.PublishedReview;
import com.examinai.app.domain.review.PublishedReviewRepository;
import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionRepository;
import com.examinai.app.domain.task.SubmissionStatus;
import com.examinai.app.domain.user.User;
import com.examinai.app.domain.user.UserRepository;

@Service
public class MentorReviewService {

	private static final int MIN_SCORE = 1;
	private static final int MAX_SCORE = 5;

	private final SubmissionRepository submissionRepository;
	private final UserRepository userRepository;
	private final MentorReviewDraftRepository draftRepository;
	private final PublishedReviewRepository publishedReviewRepository;

	public MentorReviewService(SubmissionRepository submissionRepository, UserRepository userRepository,
			MentorReviewDraftRepository draftRepository, PublishedReviewRepository publishedReviewRepository) {
		this.submissionRepository = submissionRepository;
		this.userRepository = userRepository;
		this.draftRepository = draftRepository;
		this.publishedReviewRepository = publishedReviewRepository;
	}

	@Transactional(readOnly = true)
	public List<Submission> listQueue() {
		return submissionRepository.findQueuedForMentorReview(
				EnumSet.of(SubmissionStatus.SUBMITTED, SubmissionStatus.UNDER_REVIEW));
	}

	@Transactional(readOnly = true)
	public List<PublishedReview> listPublishedHistory(UUID submissionId) {
		return publishedReviewRepository.findBySubmission_IdOrderByPublishedAtDesc(submissionId);
	}

	@Transactional(readOnly = true)
	public MentorReviewDraft findDraftOrNull(UUID submissionId) {
		return draftRepository.findBySubmission_Id(submissionId).orElse(null);
	}

	@Transactional
	public void saveDraft(UUID submissionId, UUID mentorUserId, Integer quality, Integer readability, Integer correctness,
			String narrative) {
		Submission submission = submissionRepository.findById(submissionId).orElseThrow();
		User mentor = userRepository.findById(mentorUserId).orElseThrow();
		MentorReviewDraft draft = draftRepository.findBySubmission_Id(submissionId)
			.orElseGet(() -> new MentorReviewDraft(submission, mentor));
		draft.setMentor(mentor);
		draft.setQualityScore(quality);
		draft.setReadabilityScore(readability);
		draft.setCorrectnessScore(correctness);
		draft.setNarrativeFeedback(trimToNull(narrative));
		draftRepository.save(draft);

		if (submission.getStatus() == SubmissionStatus.SUBMITTED) {
			submission.setStatus(SubmissionStatus.UNDER_REVIEW);
			submissionRepository.save(submission);
		}
	}

	@Transactional
	public void publish(UUID submissionId, UUID mentorUserId, Integer quality, Integer readability, Integer correctness,
			String narrative) {
		validateScore("Quality", quality);
		validateScore("Readability", readability);
		validateScore("Correctness", correctness);
		String feedback = trimRequiredNonEmpty(narrative, "Feedback is required to publish.");

		Submission submission = submissionRepository.findById(submissionId).orElseThrow();
		User mentor = userRepository.findById(mentorUserId).orElseThrow();

		PublishedReview published = new PublishedReview(submission, mentor, quality, readability, correctness, feedback,
				submission.getCommitSha(), submission.getGitFetchVersion(), submission.getPathScope());
		publishedReviewRepository.save(published);

		submission.setStatus(SubmissionStatus.OUTCOME_PUBLISHED);
		submissionRepository.save(submission);
		draftRepository.deleteBySubmission_Id(submissionId);
	}

	private static void validateScore(String label, Integer value) {
		if (value == null) {
			throw new IllegalArgumentException(label + " score is required to publish.");
		}
		if (value < MIN_SCORE || value > MAX_SCORE) {
			throw new IllegalArgumentException(label + " score must be between " + MIN_SCORE + " and " + MAX_SCORE + ".");
		}
	}

	private static String trimRequiredNonEmpty(String narrative, String message) {
		String t = narrative == null ? "" : narrative.trim();
		if (!StringUtils.hasText(t)) {
			throw new IllegalArgumentException(message);
		}
		return t;
	}

	private static String trimToNull(String narrative) {
		if (narrative == null) {
			return null;
		}
		String t = narrative.trim();
		return StringUtils.hasText(t) ? t : null;
	}
}
