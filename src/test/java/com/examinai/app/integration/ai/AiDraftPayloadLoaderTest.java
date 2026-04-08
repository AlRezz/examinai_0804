package com.examinai.app.integration.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.examinai.app.domain.task.GitRetrievalState;
import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionRepository;
import com.examinai.app.domain.task.SubmissionStatus;
import com.examinai.app.domain.task.Task;
import com.examinai.app.domain.user.User;

@ExtendWith(MockitoExtension.class)
class AiDraftPayloadLoaderTest {

	@Mock
	private SubmissionRepository submissionRepository;

	private AiDraftAssessmentProperties properties;

	private AiDraftPayloadLoader loader;

	private final UUID submissionId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		properties = new AiDraftAssessmentProperties();
		properties.setMaxSourceChars(10_000);
		loader = new AiDraftPayloadLoader(submissionRepository, properties);
	}

	@Test
	void loadUserPayloadBuildsMinimizedPrompt() {
		Task task = new Task("Lab", "Do the thing.", LocalDate.now());
		User intern = new User("intern@examinai.local", "x");
		Submission submission = new Submission(task, intern, "r", "c", null, SubmissionStatus.SUBMITTED);
		submission.setGitRetrievalState(GitRetrievalState.OK);
		submission.setGitRetrievedText("code line");
		when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));

		String payload = loader.loadUserPayload(submissionId);

		assertThat(payload).contains("Lab").contains("Do the thing.").contains("code line").doesNotContain("r/repo");
	}

	@Test
	void rejectsWhenNotFetched() {
		Task task = new Task("T", "D", LocalDate.now());
		User intern = new User("intern@examinai.local", "x");
		Submission submission = new Submission(task, intern, "r", "c", null, SubmissionStatus.SUBMITTED);
		submission.setGitRetrievalState(GitRetrievalState.NOT_STARTED);
		submission.setGitRetrievedText("x");
		when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));

		assertThatThrownBy(() -> loader.loadUserPayload(submissionId)).isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Source must be fetched");
	}
}
