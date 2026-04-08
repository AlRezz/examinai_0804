package com.examinai.app.domain.task;

import java.time.Instant;
import java.util.UUID;

import com.examinai.app.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "submissions", uniqueConstraints = @UniqueConstraint(name = "uk_submissions_task_intern", columnNames = {
		"task_id", "intern_user_id" }))
public class Submission {

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

	@Column(name = "repo_identifier", nullable = false, length = 2048)
	private String repoIdentifier;

	@Column(name = "commit_sha", nullable = false, length = 64)
	private String commitSha;

	@Column(name = "path_scope", length = 512)
	private String pathScope;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 32)
	private SubmissionStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Submission() {
	}

	public Submission(Task task, User intern, String repoIdentifier, String commitSha, String pathScope,
			SubmissionStatus status) {
		this.task = task;
		this.intern = intern;
		this.repoIdentifier = repoIdentifier;
		this.commitSha = commitSha;
		this.pathScope = pathScope;
		this.status = status;
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
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

	public String getRepoIdentifier() {
		return repoIdentifier;
	}

	public void setRepoIdentifier(String repoIdentifier) {
		this.repoIdentifier = repoIdentifier;
	}

	public String getCommitSha() {
		return commitSha;
	}

	public void setCommitSha(String commitSha) {
		this.commitSha = commitSha;
	}

	public String getPathScope() {
		return pathScope;
	}

	public void setPathScope(String pathScope) {
		this.pathScope = pathScope;
	}

	public SubmissionStatus getStatus() {
		return status;
	}

	public void setStatus(SubmissionStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
