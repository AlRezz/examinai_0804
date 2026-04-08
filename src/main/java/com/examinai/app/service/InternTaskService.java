package com.examinai.app.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examinai.app.domain.task.Task;
import com.examinai.app.domain.task.TaskAssignmentRepository;

@Service
public class InternTaskService {

	private final TaskAssignmentRepository assignmentRepository;

	public InternTaskService(TaskAssignmentRepository assignmentRepository) {
		this.assignmentRepository = assignmentRepository;
	}

	@Transactional(readOnly = true)
	public List<Task> listAssignedTasksForIntern(UUID internUserId) {
		return assignmentRepository.findByInternIdWithTask(internUserId).stream().map(a -> a.getTask()).toList();
	}

	@Transactional(readOnly = true)
	public boolean isAssigned(UUID taskId, UUID internUserId) {
		return assignmentRepository.findByInternIdWithTask(internUserId).stream().anyMatch(a -> taskId.equals(a.getTask().getId()));
	}
}
