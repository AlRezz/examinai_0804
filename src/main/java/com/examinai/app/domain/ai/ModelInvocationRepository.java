package com.examinai.app.domain.ai;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelInvocationRepository extends JpaRepository<ModelInvocation, UUID> {

	long countBySubmission_Id(UUID submissionId);
}
