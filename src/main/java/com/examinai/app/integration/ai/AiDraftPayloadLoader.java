package com.examinai.app.integration.ai;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.examinai.app.domain.task.GitRetrievalState;
import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionRepository;

/**
 * Short read-only transaction to assemble the user prompt; LLM calls must happen outside this layer.
 */
@Service
public class AiDraftPayloadLoader {

	private final SubmissionRepository submissionRepository;

	private final AiDraftAssessmentProperties properties;

	public AiDraftPayloadLoader(SubmissionRepository submissionRepository, AiDraftAssessmentProperties properties) {
		this.submissionRepository = submissionRepository;
		this.properties = properties;
	}

	@Transactional(readOnly = true)
	public String loadUserPayload(UUID submissionId) {
		Submission submission = submissionRepository.findById(submissionId).orElseThrow();
		if (submission.getGitRetrievalState() != GitRetrievalState.OK) {
			throw new IllegalStateException("Source must be fetched successfully before generating an AI draft.");
		}
		if (!StringUtils.hasText(submission.getGitRetrievedText())) {
			throw new IllegalStateException("No normalized source text available for this submission.");
		}

		String source = truncate(submission.getGitRetrievedText(), properties.getMaxSourceChars());
		var task = submission.getTask();
		return """
				Task title: %s

				Task instructions:
				%s

				Submission source (normalized excerpt; may be truncated):
				%s
				""".formatted(task.getTitle(), task.getDescription(), source);
	}

	private static String truncate(String text, int maxChars) {
		if (text.length() <= maxChars) {
			return text;
		}
		return text.substring(0, maxChars) + "\n\n[... truncated for pilot size limits ...]";
	}
}
