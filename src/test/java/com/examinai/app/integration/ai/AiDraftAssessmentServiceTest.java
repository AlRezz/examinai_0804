package com.examinai.app.integration.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

@ExtendWith(MockitoExtension.class)
class AiDraftAssessmentServiceTest {

	@Mock
	private AiDraftPayloadLoader payloadLoader;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ChatClient chatClient;

	private AiDraftAssessmentProperties properties;

	private ExecutorService executor;

	private AiDraftAssessmentService service;

	private final UUID submissionId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		properties = new AiDraftAssessmentProperties();
		properties.setRequestTimeoutSeconds(30);
		properties.setMaxRetries(1);
		properties.setMaxInferenceWallSeconds(600);
		executor = Executors.newVirtualThreadPerTaskExecutor();
		service = new AiDraftAssessmentService(chatClient, payloadLoader, properties, executor);
	}

	@AfterEach
	void tearDown() {
		executor.close();
	}

	@Test
	void generateDraftInvokesModelWithPayloadFromLoader() {
		when(payloadLoader.loadUserPayload(submissionId)).thenReturn("user payload text");
		when(chatClient.prompt().system(anyString()).user(anyString()).call().content()).thenReturn("Suggested feedback.");
		clearInvocations(chatClient);

		String out = service.generateDraft(submissionId);

		assertThat(out).isEqualTo("Suggested feedback.");
		verify(chatClient).prompt();
		verify(chatClient.prompt()).system(ArgumentMatchers.<String>argThat(
				systemPrompt -> systemPrompt.contains("## Feedback on the code")
						&& systemPrompt.contains("## Suggestions to improve")));
		verify(payloadLoader).loadUserPayload(submissionId);
	}

	@Test
	void propagatesIllegalStateFromLoader() {
		when(payloadLoader.loadUserPayload(submissionId))
			.thenThrow(new IllegalStateException("Source must be fetched successfully before generating an AI draft."));

		assertThatThrownBy(() -> service.generateDraft(submissionId)).isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Source must be fetched");
	}

	@Test
	void generateDraftMapsFailureToInferenceException() {
		when(payloadLoader.loadUserPayload(submissionId)).thenReturn("payload");
		when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
			.thenThrow(new RuntimeException("boom"));

		assertThatThrownBy(() -> service.generateDraft(submissionId)).isInstanceOf(InferenceUnavailableException.class);
	}

	@Test
	void timesOutWhenModelDoesNotReturnWithinDeadline() {
		when(payloadLoader.loadUserPayload(submissionId)).thenReturn("payload");
		properties.setRequestTimeoutSeconds(1);
		when(chatClient.prompt().system(anyString()).user(anyString()).call().content()).thenAnswer(invocation -> {
			try {
				new java.util.concurrent.CountDownLatch(1).await();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return "never normally";
		});

		assertThatThrownBy(() -> service.generateDraft(submissionId)).isInstanceOf(InferenceUnavailableException.class)
			.hasMessageContaining("timed out");
	}

	@Test
	void respectsTotalWallBudgetEvenWhenPerAttemptTimeoutIsHigher() {
		when(payloadLoader.loadUserPayload(submissionId)).thenReturn("payload");
		properties.setMaxInferenceWallSeconds(1);
		properties.setRequestTimeoutSeconds(120);
		properties.setMaxRetries(0);
		when(chatClient.prompt().system(anyString()).user(anyString()).call().content()).thenAnswer(invocation -> {
			try {
				new java.util.concurrent.CountDownLatch(1).await();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return "never normally";
		});

		assertThatThrownBy(() -> service.generateDraft(submissionId)).isInstanceOf(InferenceUnavailableException.class)
			.hasMessageContaining("timed out");
	}
}
