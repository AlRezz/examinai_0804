package com.examinai.app.integration.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import com.examinai.app.domain.task.GitRetrievalState;
import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionRepository;
import com.examinai.app.domain.task.SubmissionStatus;
import com.examinai.app.domain.task.Task;
import com.examinai.app.domain.user.User;

@ExtendWith(MockitoExtension.class)
class AiDraftAssessmentServiceTest {

	@Mock
	private SubmissionRepository submissionRepository;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ChatClient chatClient;

	private AiDraftAssessmentProperties properties;

	private AiDraftAssessmentService service;

	private final UUID submissionId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		properties = new AiDraftAssessmentProperties();
		properties.setRequestTimeoutSeconds(30);
		properties.setMaxRetries(1);
		service = new AiDraftAssessmentService(chatClient, submissionRepository, properties);
	}

	@Test
	void generateDraftInvokesModelWithStoredSource() {
		Task task = new Task("Lab", "Build X.", LocalDate.now());
		User intern = new User("intern@examinai.local", "x");
		Submission submission = new Submission(task, intern, "r", "c", null, SubmissionStatus.SUBMITTED);
		submission.setGitRetrievalState(GitRetrievalState.OK);
		submission.setGitRetrievedText("public class A {}");
		when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
		when(chatClient.prompt().system(anyString()).user(anyString()).call().content()).thenReturn("Suggested feedback.");
		clearInvocations(chatClient);

		String out = service.generateDraft(submissionId);

		assertThat(out).isEqualTo("Suggested feedback.");
		verify(chatClient).prompt();
	}

	@Test
	void generateDraftRejectsWhenNotFetched() {
		Task task = new Task("T", "D", LocalDate.now());
		User intern = new User("intern@examinai.local", "x");
		Submission submission = new Submission(task, intern, "r", "c", null, SubmissionStatus.SUBMITTED);
		submission.setGitRetrievalState(GitRetrievalState.NOT_STARTED);
		submission.setGitRetrievedText("x");
		when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));

		assertThatThrownBy(() -> service.generateDraft(submissionId)).isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Source must be fetched");
	}

	@Test
	void generateDraftMapsFailureToInferenceException() {
		Task task = new Task("Lab", "Build X.", LocalDate.now());
		User intern = new User("intern@examinai.local", "x");
		Submission submission = new Submission(task, intern, "r", "c", null, SubmissionStatus.SUBMITTED);
		submission.setGitRetrievalState(GitRetrievalState.OK);
		submission.setGitRetrievedText("code");
		when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
		when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
			.thenThrow(new RuntimeException("boom"));

		assertThatThrownBy(() -> service.generateDraft(submissionId)).isInstanceOf(InferenceUnavailableException.class);
	}
}
