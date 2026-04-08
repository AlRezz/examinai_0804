package com.examinai.app.domain.review;

import java.time.Instant;
import java.util.UUID;

import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.user.User;

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
@Table(name = "published_reviews")
public class PublishedReview {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "submission_id", nullable = false)
	private Submission submission;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "publishing_mentor_user_id", nullable = false)
	private User publishingMentor;

	@Column(name = "published_at", nullable = false)
	private Instant publishedAt;

	@Column(name = "quality_score", nullable = false)
	private int qualityScore;

	@Column(name = "readability_score", nullable = false)
	private int readabilityScore;

	@Column(name = "correctness_score", nullable = false)
	private int correctnessScore;

	@Column(name = "narrative_feedback", nullable = false, columnDefinition = "text")
	private String narrativeFeedback;

	@Column(name = "snapshot_commit_sha", nullable = false, length = 64)
	private String snapshotCommitSha;

	@Column(name = "snapshot_git_fetch_version", nullable = false)
	private int snapshotGitFetchVersion;

	@Column(name = "snapshot_path_scope", length = 512)
	private String snapshotPathScope;

	protected PublishedReview() {
	}

	public PublishedReview(Submission submission, User publishingMentor, int qualityScore, int readabilityScore,
			int correctnessScore, String narrativeFeedback, String snapshotCommitSha, int snapshotGitFetchVersion,
			String snapshotPathScope) {
		this.submission = submission;
		this.publishingMentor = publishingMentor;
		this.qualityScore = qualityScore;
		this.readabilityScore = readabilityScore;
		this.correctnessScore = correctnessScore;
		this.narrativeFeedback = narrativeFeedback;
		this.snapshotCommitSha = snapshotCommitSha;
		this.snapshotGitFetchVersion = snapshotGitFetchVersion;
		this.snapshotPathScope = snapshotPathScope;
	}

	@PrePersist
	void onCreate() {
		if (publishedAt == null) {
			publishedAt = Instant.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public Submission getSubmission() {
		return submission;
	}

	public User getPublishingMentor() {
		return publishingMentor;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}

	public int getQualityScore() {
		return qualityScore;
	}

	public int getReadabilityScore() {
		return readabilityScore;
	}

	public int getCorrectnessScore() {
		return correctnessScore;
	}

	public String getNarrativeFeedback() {
		return narrativeFeedback;
	}

	public String getSnapshotCommitSha() {
		return snapshotCommitSha;
	}

	public int getSnapshotGitFetchVersion() {
		return snapshotGitFetchVersion;
	}

	public String getSnapshotPathScope() {
		return snapshotPathScope;
	}
}
