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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "mentor_review_drafts")
public class MentorReviewDraft {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@OneToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "submission_id", nullable = false, unique = true)
	private Submission submission;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "mentor_user_id", nullable = false)
	private User mentor;

	@Column(name = "quality_score")
	private Integer qualityScore;

	@Column(name = "readability_score")
	private Integer readabilityScore;

	@Column(name = "correctness_score")
	private Integer correctnessScore;

	@Column(name = "narrative_feedback", columnDefinition = "text")
	private String narrativeFeedback;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected MentorReviewDraft() {
	}

	public MentorReviewDraft(Submission submission, User mentor) {
		this.submission = submission;
		this.mentor = mentor;
	}

	@PrePersist
	@PreUpdate
	void touch() {
		this.updatedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public Submission getSubmission() {
		return submission;
	}

	public User getMentor() {
		return mentor;
	}

	public void setMentor(User mentor) {
		this.mentor = mentor;
	}

	public Integer getQualityScore() {
		return qualityScore;
	}

	public void setQualityScore(Integer qualityScore) {
		this.qualityScore = qualityScore;
	}

	public Integer getReadabilityScore() {
		return readabilityScore;
	}

	public void setReadabilityScore(Integer readabilityScore) {
		this.readabilityScore = readabilityScore;
	}

	public Integer getCorrectnessScore() {
		return correctnessScore;
	}

	public void setCorrectnessScore(Integer correctnessScore) {
		this.correctnessScore = correctnessScore;
	}

	public String getNarrativeFeedback() {
		return narrativeFeedback;
	}

	public void setNarrativeFeedback(String narrativeFeedback) {
		this.narrativeFeedback = narrativeFeedback;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
