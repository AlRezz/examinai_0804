package com.examinai.app.service;

/**
 * Display bundle for Thymeleaf; keeps templates free of business branching.
 */
public record SubmissionLifecycleView(SubmissionLifecycleStatus status, String label, String badgeClass) {
}
