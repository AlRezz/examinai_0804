package com.examinai.app.domain.task;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

	Optional<Submission> findByTask_IdAndIntern_Id(UUID taskId, UUID internId);

	@Query("""
			SELECT DISTINCT s FROM Submission s
			JOIN FETCH s.task t
			JOIN FETCH s.intern i
			WHERE s.status IN :statuses
			ORDER BY t.dueDate ASC, s.updatedAt DESC
			""")
	List<Submission> findQueuedForMentorReview(@Param("statuses") Collection<SubmissionStatus> statuses);
}
