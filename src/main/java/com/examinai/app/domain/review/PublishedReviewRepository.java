package com.examinai.app.domain.review;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PublishedReviewRepository extends JpaRepository<PublishedReview, UUID> {

	@Query("""
			SELECT DISTINCT pr FROM PublishedReview pr
			JOIN FETCH pr.publishingMentor
			WHERE pr.submission.id = :submissionId
			ORDER BY pr.publishedAt DESC
			""")
	List<PublishedReview> findBySubmission_IdOrderByPublishedAtDesc(@Param("submissionId") UUID submissionId);
}
