package com.examinai.app.domain.task;

import java.util.UUID;

import com.examinai.app.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "task_assignments", uniqueConstraints = @UniqueConstraint(name = "uk_task_assignments_task_intern", columnNames = {
		"task_id", "intern_user_id" }))
public class TaskAssignment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "task_id", nullable = false)
	private Task task;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "intern_user_id", nullable = false)
	private User intern;

	protected TaskAssignment() {
	}

	public TaskAssignment(Task task, User intern) {
		this.task = task;
		this.intern = intern;
	}

	public UUID getId() {
		return id;
	}

	public Task getTask() {
		return task;
	}

	public User getIntern() {
		return intern;
	}
}
