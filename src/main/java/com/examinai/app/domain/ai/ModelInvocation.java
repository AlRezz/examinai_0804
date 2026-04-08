package com.examinai.app.domain.ai;

import java.time.Instant;
import java.util.UUID;

import com.examinai.app.domain.task.Submission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "model_invocations")
public class ModelInvocation {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "submission_id", nullable = false)
	private Submission submission;

	@Column(name = "invoked_at", nullable = false)
	private Instant invokedAt;

	@Column(name = "model_name", nullable = false, length = 256)
	private String modelName;

	@Column(name = "model_version", length = 256)
	private String modelVersion;

	@Column(name = "prompt_hash", length = 64)
	private String promptHash;

	protected ModelInvocation() {
	}

	public ModelInvocation(Submission submission, Instant invokedAt, String modelName, String modelVersion,
			String promptHash) {
		this.submission = submission;
		this.invokedAt = invokedAt;
		this.modelName = modelName;
		this.modelVersion = modelVersion;
		this.promptHash = promptHash;
	}

	@PrePersist
	void onCreate() {
		if (invokedAt == null) {
			invokedAt = Instant.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public Submission getSubmission() {
		return submission;
	}

	public Instant getInvokedAt() {
		return invokedAt;
	}

	public String getModelName() {
		return modelName;
	}

	public String getModelVersion() {
		return modelVersion;
	}

	public String getPromptHash() {
		return promptHash;
	}
}
