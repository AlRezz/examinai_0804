package com.examinai.app.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examinai.app.domain.task.GitRetrievalState;
import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionRepository;
import com.examinai.app.web.task.GitRetrievalUiMessage;

@Service
public class CoordinatorCaseRecordService {

	private final SubmissionRepository submissionRepository;

	private final AiDraftPersistenceService aiDraftPersistenceService;

	private final MentorReviewService mentorReviewService;

	public CoordinatorCaseRecordService(SubmissionRepository submissionRepository,
			AiDraftPersistenceService aiDraftPersistenceService, MentorReviewService mentorReviewService) {
		this.submissionRepository = submissionRepository;
		this.aiDraftPersistenceService = aiDraftPersistenceService;
		this.mentorReviewService = mentorReviewService;
	}

	@Transactional(readOnly = true)
	public Optional<CoordinatorCaseRecordModel> findBySubmissionId(UUID submissionId) {
		return submissionRepository.findById(submissionId).map(this::toModel);
	}

	private CoordinatorCaseRecordModel toModel(Submission sub) {
		String gitMsg = sub.getGitRetrievalState() == GitRetrievalState.ERROR
				? GitRetrievalUiMessage.forErrorCode(sub.getGitRetrievalErrorCode()) : null;
		AiDraftView ai = aiDraftPersistenceService.findLatestForSubmission(sub.getId()).orElse(null);
		var draft = mentorReviewService.findDraftOrNull(sub.getId());
		var history = mentorReviewService.listPublishedHistory(sub.getId());
		var currentRev = mentorReviewService.findLatestPublishedForCurrentRevision(sub);
		return new CoordinatorCaseRecordModel(sub, gitMsg, ai, draft, history, currentRev);
	}

}
