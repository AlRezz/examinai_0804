package com.examinai.app.web.intern;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SubmissionForm {

	@NotBlank
	@Size(max = 2048)
	private String repoIdentifier;

	@NotBlank
	@Size(max = 64)
	private String commitSha;

	@Size(max = 512)
	private String pathScope;

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
}
