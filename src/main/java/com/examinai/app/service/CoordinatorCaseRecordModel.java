package com.examinai.app.service;

import java.util.List;
import java.util.Optional;

import com.examinai.app.domain.review.MentorReviewDraft;
import com.examinai.app.domain.review.PublishedReview;
import com.examinai.app.domain.task.Submission;

public record CoordinatorCaseRecordModel(Submission submission, String gitRetrievalMessage, AiDraftView latestAiDraft,
		MentorReviewDraft mentorDraft, List<PublishedReview> publishedHistory,
		Optional<PublishedReview> publishedForCurrentRevision) {
}
