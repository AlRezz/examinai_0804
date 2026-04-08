package com.examinai.app.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.examinai.app.domain.task.GitRetrievalState;
import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionStatus;
import com.examinai.app.domain.task.Task;
import com.examinai.app.domain.user.User;

class SubmissionLifecycleServiceTest {

	private final SubmissionLifecycleService service = new SubmissionLifecycleService();

	@Test
	void nullSubmission_isNotSubmitted() {
		assertThat(service.compute(null)).isEqualTo(SubmissionLifecycleStatus.NOT_SUBMITTED);
	}

	@Test
	void outcomePublished_winsOverGitError() {
		Submission s = submission(SubmissionStatus.SUBMITTED);
		s.setStatus(SubmissionStatus.OUTCOME_PUBLISHED);
		s.setGitRetrievalState(GitRetrievalState.ERROR);
		assertThat(service.compute(s)).isEqualTo(SubmissionLifecycleStatus.OUTCOME_PUBLISHED);
	}

	@Test
	void underReview_beforeGitSignals() {
		Submission s = submission(SubmissionStatus.UNDER_REVIEW);
		s.setGitRetrievalState(GitRetrievalState.ERROR);
		assertThat(service.compute(s)).isEqualTo(SubmissionLifecycleStatus.UNDER_MENTOR_REVIEW);
	}

	@Test
	void submittedWithRetrievalError() {
		Submission s = submission(SubmissionStatus.SUBMITTED);
		s.setGitRetrievalState(GitRetrievalState.ERROR);
		assertThat(service.compute(s)).isEqualTo(SubmissionLifecycleStatus.SOURCE_RETRIEVAL_FAILED);
	}

	@Test
	void submittedWhileFetchInProgress() {
		Submission s = submission(SubmissionStatus.SUBMITTED);
		s.setGitRetrievalState(GitRetrievalState.IN_PROGRESS);
		assertThat(service.compute(s)).isEqualTo(SubmissionLifecycleStatus.SOURCE_FETCH_IN_PROGRESS);
	}

	@Test
	void draftStatus_mapsToNotSubmitted() {
		Submission s = submission(SubmissionStatus.DRAFT);
		s.setGitRetrievalState(GitRetrievalState.OK);
		assertThat(service.compute(s)).isEqualTo(SubmissionLifecycleStatus.NOT_SUBMITTED);
	}

	@Test
	void submittedAwaitingReview_whenGitIdleOrOk() {
		Submission s = submission(SubmissionStatus.SUBMITTED);
		s.setGitRetrievalState(GitRetrievalState.NOT_STARTED);
		assertThat(service.compute(s)).isEqualTo(SubmissionLifecycleStatus.AWAITING_MENTOR_REVIEW);
		s.setGitRetrievalState(GitRetrievalState.OK);
		assertThat(service.compute(s)).isEqualTo(SubmissionLifecycleStatus.AWAITING_MENTOR_REVIEW);
	}

	private static Submission submission(SubmissionStatus status) {
		Task task = new Task("t", "b", LocalDate.now());
		User intern = new User("intern@example.com", "x");
		Submission s = new Submission(task, intern, "o/r", "sha", null, status);
		s.setGitRetrievalState(GitRetrievalState.NOT_STARTED);
		return s;
	}
}
