package com.examinai.app.domain.review;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorReviewDraftRepository extends JpaRepository<MentorReviewDraft, UUID> {

	Optional<MentorReviewDraft> findBySubmission_Id(UUID submissionId);

	void deleteBySubmission_Id(UUID submissionId);
}
