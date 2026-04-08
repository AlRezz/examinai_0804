package com.examinai.app.domain.review;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PublishedReviewRepository extends JpaRepository<PublishedReview, UUID> {

	List<PublishedReview> findBySubmission_IdOrderByPublishedAtDesc(UUID submissionId);
}
