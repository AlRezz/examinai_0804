package com.examinai.app.service;

import java.util.Optional;
import java.util.UUID;

import com.examinai.app.domain.review.PublishedReview;
import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.Task;

public record InternFeedbackBundle(UUID submissionId, Task task, Submission submission, SubmissionLifecycleView submissionLifecycle,
		Optional<PublishedReview> officialForCurrentRevision, Optional<AiDraftView> aiDraftForIntern) {
}
