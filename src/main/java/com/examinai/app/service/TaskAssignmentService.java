package com.examinai.app.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examinai.app.domain.task.TaskAssignment;
import com.examinai.app.domain.task.TaskAssignmentRepository;
import com.examinai.app.domain.task.TaskRepository;
import com.examinai.app.domain.user.User;
import com.examinai.app.domain.user.UserRepository;

/**
 * Assignment updates use a <strong>replace-all</strong> strategy: each save deletes existing rows for the task
 * and inserts one row per selected intern (Story 2.2 AC#2).
 */
@Service
public class TaskAssignmentService {

	private static final String INTERN_ROLE = "intern";

	private final TaskRepository taskRepository;
	private final TaskAssignmentRepository assignmentRepository;
	private final UserRepository userRepository;

	public TaskAssignmentService(TaskRepository taskRepository, TaskAssignmentRepository assignmentRepository,
			UserRepository userRepository) {
		this.taskRepository = taskRepository;
		this.assignmentRepository = assignmentRepository;
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public List<TaskAssignment> listForTask(UUID taskId) {
		taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found."));
		return assignmentRepository.findByTaskIdWithIntern(taskId);
	}

	@Transactional
	public void replaceAssignmentsForTask(UUID taskId, List<UUID> internUserIds) {
		var task = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found."));
		Set<UUID> unique = new LinkedHashSet<>();
		if (internUserIds != null) {
			for (UUID id : internUserIds) {
				if (id != null) {
					unique.add(id);
				}
			}
		}
		assignmentRepository.deleteByTask_Id(taskId);
		for (UUID internId : unique) {
			User intern = userRepository.findById(internId).orElseThrow(() -> new IllegalArgumentException("User not found."));
			boolean isIntern = intern.getRoles().stream().anyMatch(r -> INTERN_ROLE.equals(r.getName()));
			if (!isIntern) {
				throw new IllegalArgumentException("Only users with the intern role can be assigned.");
			}
			assignmentRepository.save(new TaskAssignment(task, intern));
		}
	}
}
