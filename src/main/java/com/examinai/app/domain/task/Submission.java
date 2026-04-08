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

	@Enumerated(EnumType.STRING)
	@Column(name = "git_retrieval_state", nullable = false, length = 32)
	private GitRetrievalState gitRetrievalState = GitRetrievalState.NOT_STARTED;

	@Column(name = "git_retrieval_error_code", length = 64)
	private String gitRetrievalErrorCode;

	@Column(name = "git_retrieved_text", columnDefinition = "text")
	private String gitRetrievedText;

	@Column(name = "git_last_success_at")
	private Instant gitLastSuccessAt;

	@Column(name = "git_last_attempt_at")
	private Instant gitLastAttemptAt;

	@Column(name = "git_fetch_version", nullable = false)
	private int gitFetchVersion;

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

	public GitRetrievalState getGitRetrievalState() {
		return gitRetrievalState;
	}

	public void setGitRetrievalState(GitRetrievalState gitRetrievalState) {
		this.gitRetrievalState = gitRetrievalState;
	}

	public String getGitRetrievalErrorCode() {
		return gitRetrievalErrorCode;
	}

	public void setGitRetrievalErrorCode(String gitRetrievalErrorCode) {
		this.gitRetrievalErrorCode = gitRetrievalErrorCode;
	}

	public String getGitRetrievedText() {
		return gitRetrievedText;
	}

	public void setGitRetrievedText(String gitRetrievedText) {
		this.gitRetrievedText = gitRetrievedText;
	}

	public Instant getGitLastSuccessAt() {
		return gitLastSuccessAt;
	}

	public void setGitLastSuccessAt(Instant gitLastSuccessAt) {
		this.gitLastSuccessAt = gitLastSuccessAt;
	}

	public Instant getGitLastAttemptAt() {
		return gitLastAttemptAt;
	}

	public void setGitLastAttemptAt(Instant gitLastAttemptAt) {
		this.gitLastAttemptAt = gitLastAttemptAt;
	}

	public int getGitFetchVersion() {
		return gitFetchVersion;
	}

	public void setGitFetchVersion(int gitFetchVersion) {
		this.gitFetchVersion = gitFetchVersion;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
