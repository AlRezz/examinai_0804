package com.examinai.app.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examinai.app.domain.ai.AiDraft;
import com.examinai.app.domain.ai.AiDraftRepository;
import com.examinai.app.domain.ai.ModelInvocation;
import com.examinai.app.domain.ai.ModelInvocationRepository;
import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionRepository;
import com.examinai.app.integration.ai.AiDraftPayloadLoader;
import com.examinai.app.integration.ai.AiModelAuditDescriptor;

@Service
public class AiDraftPersistenceService {

	private static final Logger log = LoggerFactory.getLogger(AiDraftPersistenceService.class);

	private final SubmissionRepository submissionRepository;

	private final AiDraftPayloadLoader payloadLoader;

	private final ModelInvocationRepository modelInvocationRepository;

	private final AiDraftRepository aiDraftRepository;

	private final AiModelAuditDescriptor modelAuditDescriptor;

	public AiDraftPersistenceService(SubmissionRepository submissionRepository, AiDraftPayloadLoader payloadLoader,
			ModelInvocationRepository modelInvocationRepository, AiDraftRepository aiDraftRepository,
			AiModelAuditDescriptor modelAuditDescriptor) {
		this.submissionRepository = submissionRepository;
		this.payloadLoader = payloadLoader;
		this.modelInvocationRepository = modelInvocationRepository;
		this.aiDraftRepository = aiDraftRepository;
		this.modelAuditDescriptor = modelAuditDescriptor;
	}

	/**
	 * Persists AI output and invocation metadata after a successful {@link com.examinai.app.integration.ai.AiDraftAssessmentService} call.
	 */
	@Transactional
	public void persistSuccessfulDraft(UUID submissionId, String assessmentText) {
		log.debug("persistSuccessfulDraft: submissionId={}, assessmentChars={}", submissionId,
				assessmentText == null ? 0 : assessmentText.length());
		String userPayload = payloadLoader.loadUserPayload(submissionId);
		String promptHash = sha256Hex(userPayload);
		Submission submission = submissionRepository.findById(submissionId).orElseThrow();
		String version = modelAuditDescriptor.modelVersion().orElse(null);
		ModelInvocation invocation = new ModelInvocation(submission, Instant.now(), modelAuditDescriptor.modelName(),
				version, promptHash);
		modelInvocationRepository.save(invocation);
		aiDraftRepository.save(new AiDraft(invocation, assessmentText));
	}

	@Transactional(readOnly = true)
	public Optional<AiDraftView> findLatestForSubmission(UUID submissionId) {
		log.debug("findLatestForSubmission: submissionId={}", submissionId);
		return aiDraftRepository.findFirstByInvocation_Submission_IdOrderByInvocation_InvokedAtDesc(submissionId)
			.map(d -> {
				var inv = d.getInvocation();
				String ver = inv.getModelVersion();
				return new AiDraftView(d.getAssessmentText(), inv.getInvokedAt(), inv.getModelName(), ver,
						inv.getPromptHash());
			});
	}

	private static String sha256Hex(String payload) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest(payload.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}
