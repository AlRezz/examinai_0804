package com.examinai.app.service;

import java.time.Instant;

public record AiDraftView(String assessmentText, Instant invokedAt, String modelName, String modelDisplayVersion,
		String promptHash) {
}
