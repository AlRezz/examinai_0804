package com.examinai.app.integration.ai;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Mentor assistive draft via Spring AI only (FR18). Inference runs outside any database transaction;
 * payload assembly is {@link AiDraftPayloadLoader} (NFR4).
 */
@Service
public class AiDraftAssessmentService {

	private static final String SYSTEM = """
			You are assisting a human mentor grading an intern's work. Produce a concise draft assessment only.
			Use a short rubric-style note: strengths, gaps, and suggested feedback themes.
			Do not claim certainty; the mentor decides final scores and wording. Do not ask for credentials or secrets.
			""";

	private final ChatClient chatClient;

	private final AiDraftPayloadLoader payloadLoader;

	private final AiDraftAssessmentProperties properties;

	private final ExecutorService aiDraftExecutor;

	public AiDraftAssessmentService(ChatClient chatClient, AiDraftPayloadLoader payloadLoader,
			AiDraftAssessmentProperties properties, ExecutorService aiDraftExecutor) {
		this.chatClient = chatClient;
		this.payloadLoader = payloadLoader;
		this.properties = properties;
		this.aiDraftExecutor = aiDraftExecutor;
	}

	public String generateDraft(UUID submissionId) {
		String userPayload = payloadLoader.loadUserPayload(submissionId);

		InferenceUnavailableException lastFailure = null;
		int attempts = properties.getMaxRetries() + 1;
		for (int i = 0; i < attempts; i++) {
			if (i > 0) {
				sleepQuietly(properties.getRetryBackoffMs());
			}
			try {
				return invokeWithTimeout(userPayload);
			}
			catch (InferenceUnavailableException ex) {
				lastFailure = ex;
			}
		}
		throw lastFailure != null ? lastFailure
				: new InferenceUnavailableException("AI draft failed after " + attempts + " attempts.");
	}

	private String invokeWithTimeout(String userPayload) {
		Future<String> future = aiDraftExecutor.submit(() -> chatClient.prompt()
			.system(SYSTEM)
			.user(userPayload)
			.call()
			.content());
		try {
			String out = future.get(properties.getRequestTimeoutSeconds(), TimeUnit.SECONDS);
			if (!StringUtils.hasText(out)) {
				throw new InferenceUnavailableException("Model returned an empty response.");
			}
			return out.trim();
		}
		catch (TimeoutException e) {
			future.cancel(true);
			throw new InferenceUnavailableException("AI draft request timed out.", e);
		}
		catch (InterruptedException e) {
			future.cancel(true);
			Thread.currentThread().interrupt();
			throw new InferenceUnavailableException("AI draft interrupted.", e);
		}
		catch (ExecutionException e) {
			Throwable cause = e.getCause() != null ? e.getCause() : e;
			throw new InferenceUnavailableException("AI draft failed: " + cause.getMessage(), cause);
		}
	}

	private static void sleepQuietly(long ms) {
		try {
			Thread.sleep(ms);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
