package com.examinai.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.examinai.app.domain.task.GitRetrievalState;
import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionRepository;
import com.examinai.app.domain.task.SubmissionStatus;
import com.examinai.app.domain.task.Task;
import com.examinai.app.domain.user.User;
import com.examinai.app.integration.git.GitFailureKind;
import com.examinai.app.integration.git.GitProviderException;
import com.examinai.app.integration.git.GitSourceClient;

@ExtendWith(MockitoExtension.class)
class SourceRetrievalServiceTest {

	@Mock
	private SubmissionRepository submissionRepository;

	@Mock
	private GitSourceClient gitSourceClient;

	@InjectMocks
	private SourceRetrievalService sourceRetrievalService;

	@Test
	void failureKeepsPreviousSnapshotText() {
		UUID id = UUID.randomUUID();
		Submission s = sampleSubmission();
		s.setGitRetrievedText("prior good text");
		s.setGitFetchVersion(2);
		when(submissionRepository.findById(id)).thenReturn(Optional.of(s));
		when(gitSourceClient.fetchNormalizedFileContent(anyString(), anyString(), any())).thenThrow(
			new GitProviderException(GitFailureKind.NOT_FOUND, "not found"));
		when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		sourceRetrievalService.retrieveAndPersist(id);

		assertThat(s.getGitRetrievalState()).isEqualTo(GitRetrievalState.ERROR);
		assertThat(s.getGitRetrievedText()).isEqualTo("prior good text");
		assertThat(s.getGitFetchVersion()).isEqualTo(3);
	}

	private static Submission sampleSubmission() {
		Task task = new Task("t", "d", java.time.LocalDate.now());
		User u = new User("i@test", "x");
		return new Submission(task, u, "o/r", "sha", null, SubmissionStatus.SUBMITTED);
	}
}
