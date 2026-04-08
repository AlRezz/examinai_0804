package com.examinai.app.domain.task;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

	Optional<Submission> findByTask_IdAndIntern_Id(UUID taskId, UUID internId);
}
