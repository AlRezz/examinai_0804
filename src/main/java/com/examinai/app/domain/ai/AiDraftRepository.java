package com.examinai.app.domain.ai;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AiDraftRepository extends JpaRepository<AiDraft, UUID> {

	Optional<AiDraft> findFirstByInvocation_Submission_IdOrderByInvocation_InvokedAtDesc(UUID submissionId);
}
