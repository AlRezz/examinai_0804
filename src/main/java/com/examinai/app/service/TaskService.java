package com.examinai.app.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examinai.app.domain.task.Task;
import com.examinai.app.domain.task.TaskRepository;

@Service
public class TaskService {

	private final TaskRepository taskRepository;

	public TaskService(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@Transactional(readOnly = true)
	public List<Task> listAllOrderedByDueDate() {
		return taskRepository.findAll(Sort.by(Sort.Order.asc("dueDate"), Sort.Order.asc("title")));
	}

	@Transactional(readOnly = true)
	public Task requireTask(UUID id) {
		return taskRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Task not found."));
	}

	@Transactional
	public Task create(String title, String description, LocalDate dueDate) {
		var task = new Task(trim(title), trim(description), dueDate);
		return taskRepository.save(task);
	}

	@Transactional
	public Task update(UUID id, String title, String description, LocalDate dueDate) {
		Task task = requireTask(id);
		task.setTitle(trim(title));
		task.setDescription(trim(description));
		task.setDueDate(dueDate);
		return taskRepository.save(task);
	}

	private static String trim(String s) {
		return s == null ? "" : s.trim();
	}
}
