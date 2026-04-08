package com.examinai.app.service;

import org.springframework.stereotype.Service;

import com.examinai.app.domain.task.GitRetrievalState;
import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionStatus;

/**
 * Single mapping from persisted aggregate → intern status label (Story 6.3 AC#2).
 *
 * <p>
 * Precedence (first match wins):
 * <ol>
 * <li>No row → {@link SubmissionLifecycleStatus#NOT_SUBMITTED}</li>
 * <li>{@link SubmissionStatus#OUTCOME_PUBLISHED} → {@link SubmissionLifecycleStatus#OUTCOME_PUBLISHED}</li>
 * <li>{@link SubmissionStatus#UNDER_REVIEW} → {@link SubmissionLifecycleStatus#UNDER_MENTOR_REVIEW}</li>
 * <li>{@link GitRetrievalState#ERROR} → {@link SubmissionLifecycleStatus#SOURCE_RETRIEVAL_FAILED}</li>
 * <li>{@link GitRetrievalState#IN_PROGRESS} → {@link SubmissionLifecycleStatus#SOURCE_FETCH_IN_PROGRESS}</li>
 * <li>{@link SubmissionStatus#DRAFT} → {@link SubmissionLifecycleStatus#NOT_SUBMITTED}</li>
 * <li>{@link SubmissionStatus#SUBMITTED} → {@link SubmissionLifecycleStatus#AWAITING_MENTOR_REVIEW}</li>
 * </ol>
 * </p>
 */
@Service
public class SubmissionLifecycleService {

	public SubmissionLifecycleView viewForIntern(Submission submissionOrNull) {
		SubmissionLifecycleStatus status = compute(submissionOrNull);
		return new SubmissionLifecycleView(status, label(status), badgeClass(status));
	}

	public SubmissionLifecycleStatus compute(Submission submissionOrNull) {
		if (submissionOrNull == null) {
			return SubmissionLifecycleStatus.NOT_SUBMITTED;
		}
		Submission s = submissionOrNull;
		if (s.getStatus() == SubmissionStatus.OUTCOME_PUBLISHED) {
			return SubmissionLifecycleStatus.OUTCOME_PUBLISHED;
		}
		if (s.getStatus() == SubmissionStatus.UNDER_REVIEW) {
			return SubmissionLifecycleStatus.UNDER_MENTOR_REVIEW;
		}
		if (s.getGitRetrievalState() == GitRetrievalState.ERROR) {
			return SubmissionLifecycleStatus.SOURCE_RETRIEVAL_FAILED;
		}
		if (s.getGitRetrievalState() == GitRetrievalState.IN_PROGRESS) {
			return SubmissionLifecycleStatus.SOURCE_FETCH_IN_PROGRESS;
		}
		if (s.getStatus() == SubmissionStatus.DRAFT) {
			return SubmissionLifecycleStatus.NOT_SUBMITTED;
		}
		return SubmissionLifecycleStatus.AWAITING_MENTOR_REVIEW;
	}

	private static String label(SubmissionLifecycleStatus status) {
		return switch (status) {
			case NOT_SUBMITTED -> "Not submitted";
			case SOURCE_FETCH_IN_PROGRESS -> "Fetching source";
			case SOURCE_RETRIEVAL_FAILED -> "Source retrieval failed";
			case AWAITING_MENTOR_REVIEW -> "Awaiting mentor review";
			case UNDER_MENTOR_REVIEW -> "In mentor review";
			case OUTCOME_PUBLISHED -> "Outcome published";
		};
	}

	private static String badgeClass(SubmissionLifecycleStatus status) {
		return switch (status) {
			case NOT_SUBMITTED -> "bg-secondary";
			case SOURCE_FETCH_IN_PROGRESS -> "bg-info";
			case SOURCE_RETRIEVAL_FAILED -> "bg-danger";
			case AWAITING_MENTOR_REVIEW -> "bg-warning text-dark";
			case UNDER_MENTOR_REVIEW -> "bg-primary";
			case OUTCOME_PUBLISHED -> "bg-success";
		};
	}
}
