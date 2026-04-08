package com.examinai.app.domain.ai;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_drafts")
public class AiDraft {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@OneToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "model_invocation_id", nullable = false, unique = true)
	private ModelInvocation invocation;

	@Column(name = "assessment_text", nullable = false, columnDefinition = "text")
	private String assessmentText;

	protected AiDraft() {
	}

	public AiDraft(ModelInvocation invocation, String assessmentText) {
		this.invocation = invocation;
		this.assessmentText = assessmentText;
	}

	public UUID getId() {
		return id;
	}

	public ModelInvocation getInvocation() {
		return invocation;
	}

	public String getAssessmentText() {
		return assessmentText;
	}
}
