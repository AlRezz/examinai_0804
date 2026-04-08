package com.examinai.app.service;

/**
 * Intern-facing lifecycle states derived only from persisted submission, retrieval, and review fields (Story 6.3).
 * Ordering in {@link SubmissionLifecycleService} defines precedence when multiple signals could apply.
 */
public enum SubmissionLifecycleStatus {

	NOT_SUBMITTED,
	SOURCE_FETCH_IN_PROGRESS,
	SOURCE_RETRIEVAL_FAILED,
	AWAITING_MENTOR_REVIEW,
	UNDER_MENTOR_REVIEW,
	OUTCOME_PUBLISHED
}
