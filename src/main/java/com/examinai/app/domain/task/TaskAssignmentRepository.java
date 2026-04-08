package com.examinai.app.domain.task;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, UUID> {

	@Query("SELECT a FROM TaskAssignment a JOIN FETCH a.task t WHERE a.intern.id = :internId ORDER BY t.dueDate ASC, t.title ASC")
	List<TaskAssignment> findByInternIdWithTask(@Param("internId") UUID internId);

	@Query("SELECT a FROM TaskAssignment a JOIN FETCH a.intern WHERE a.task.id = :taskId ORDER BY a.intern.email ASC")
	List<TaskAssignment> findByTaskIdWithIntern(@Param("taskId") UUID taskId);

	void deleteByTask_Id(UUID taskId);
}
