package com.examinai.app.web.task;

public class MentorReviewForm {

	private Integer qualityScore;

	private Integer readabilityScore;

	private Integer correctnessScore;

	private String narrativeFeedback;

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
}
