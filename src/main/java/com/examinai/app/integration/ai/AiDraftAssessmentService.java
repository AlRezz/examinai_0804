package com.examinai.app.integration.ai;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.examinai.app.domain.task.GitRetrievalState;
import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionRepository;

/**
 * Mentor assistive draft via Spring AI only (FR18). Sends minimized payload: task brief + truncated
 * normalized source already stored on the submission—no repo URLs, tokens, or environment (NFR7).
 */
@Service
public class AiDraftAssessmentService {

	private static final String SYSTEM = """
			You are assisting a human mentor grading an intern's work. Produce a concise draft assessment only.
			Use a short rubric-style note: strengths, gaps, and suggested feedback themes.
			Do not claim certainty; the mentor decides final scores and wording. Do not ask for credentials or secrets.
			""";

	private final ChatClient chatClient;

	private final SubmissionRepository submissionRepository;

	private final AiDraftAssessmentProperties properties;

	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

	public AiDraftAssessmentService(ChatClient chatClient, SubmissionRepository submissionRepository,
			AiDraftAssessmentProperties properties) {
		this.chatClient = chatClient;
		this.submissionRepository = submissionRepository;
		this.properties = properties;
	}

	@Transactional(readOnly = true)
	public String generateDraft(UUID submissionId) {
		Submission submission = submissionRepository.findById(submissionId).orElseThrow();
		if (submission.getGitRetrievalState() != GitRetrievalState.OK) {
			throw new IllegalStateException("Source must be fetched successfully before generating an AI draft.");
		}
		if (!StringUtils.hasText(submission.getGitRetrievedText())) {
			throw new IllegalStateException("No normalized source text available for this submission.");
		}

		String source = truncate(submission.getGitRetrievedText(), properties.getMaxSourceChars());
		var task = submission.getTask();
		String userPayload = """
				Task title: %s

				Task instructions:
				%s

				Submission source (normalized excerpt; may be truncated):
				%s
				""".formatted(task.getTitle(), task.getDescription(), source);

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
		Future<String> future = executor.submit(() -> chatClient.prompt()
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

	private static String truncate(String text, int maxChars) {
		if (text.length() <= maxChars) {
			return text;
		}
		return text.substring(0, maxChars) + "\n\n[... truncated for pilot size limits ...]";
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
