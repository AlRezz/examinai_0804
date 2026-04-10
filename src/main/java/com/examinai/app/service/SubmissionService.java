package com.examinai.app.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.examinai.app.domain.review.MentorReviewDraftRepository;
import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionRepository;
import com.examinai.app.domain.task.SubmissionStatus;
import com.examinai.app.domain.task.Task;
import com.examinai.app.domain.task.TaskRepository;
import com.examinai.app.domain.user.User;
import com.examinai.app.domain.user.UserRepository;

/**
 * One submission row per (task, intern); POST upserts coordinates — <strong>newest wins</strong> (Story 2.4).
 */
@Service
public class SubmissionService {

	private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

	private final TaskRepository taskRepository;
	private final UserRepository userRepository;
	private final SubmissionRepository submissionRepository;
	private final InternTaskService internTaskService;
	private final MentorReviewDraftRepository mentorReviewDraftRepository;

	public SubmissionService(TaskRepository taskRepository, UserRepository userRepository,
			SubmissionRepository submissionRepository, InternTaskService internTaskService,
			MentorReviewDraftRepository mentorReviewDraftRepository) {
		this.taskRepository = taskRepository;
		this.userRepository = userRepository;
		this.submissionRepository = submissionRepository;
		this.internTaskService = internTaskService;
		this.mentorReviewDraftRepository = mentorReviewDraftRepository;
	}

	@Transactional(readOnly = true)
	public Submission findForInternTask(UUID taskId, UUID internUserId) {
		log.debug("findForInternTask: taskId={}, internUserId={}", taskId, internUserId);
		return submissionRepository.findByTask_IdAndIntern_Id(taskId, internUserId).orElse(null);
	}

	@Transactional
	public Submission upsertCoordinates(UUID taskId, UUID internUserId, String repoIdentifier, String commitSha,
			String pathScope, SubmissionStatus status) {
		log.debug("upsertCoordinates: taskId={}, internUserId={}, status={}", taskId, internUserId, status);
		if (!internTaskService.isAssigned(taskId, internUserId)) {
			throw new IllegalArgumentException("Not assigned to this task.");
		}
		return persistCoordinates(taskId, internUserId, repoIdentifier, commitSha, pathScope, status);
	}

	/**
	 * Mentor/admin coordinate fix without intern assignment check (Story 3.3).
	 */
	@Transactional
	public Submission mentorUpsertCoordinates(UUID taskId, UUID internUserId, String repoIdentifier, String commitSha,
			String pathScope, SubmissionStatus status) {
		log.debug("mentorUpsertCoordinates: taskId={}, internUserId={}, status={}", taskId, internUserId, status);
		return persistCoordinates(taskId, internUserId, repoIdentifier, commitSha, pathScope, status);
	}

	@Transactional(readOnly = true)
	public Submission findForTaskAndInternOrNull(UUID taskId, UUID internUserId) {
		log.debug("findForTaskAndInternOrNull: taskId={}, internUserId={}", taskId, internUserId);
		return submissionRepository.findByTask_IdAndIntern_Id(taskId, internUserId).orElse(null);
	}

	private Submission persistCoordinates(UUID taskId, UUID internUserId, String repoIdentifier, String commitSha,
			String pathScope, SubmissionStatus status) {
		Task task = taskRepository.findById(taskId).orElseThrow();
		User intern = userRepository.findById(internUserId).orElseThrow();
		String repo = trimRequired(repoIdentifier, "Repository is required.");
		String sha = trimRequired(commitSha, "Commit SHA is required.");
		String scope = trimToNull(pathScope);

		return submissionRepository.findByTask_IdAndIntern_Id(taskId, internUserId).map(existing -> {
			SubmissionStatus previous = existing.getStatus();
			existing.setRepoIdentifier(repo);
			existing.setCommitSha(sha);
			existing.setPathScope(scope);
			existing.setStatus(status);
			Submission saved = submissionRepository.save(existing);
			if (previous == SubmissionStatus.OUTCOME_PUBLISHED && status == SubmissionStatus.SUBMITTED) {
				mentorReviewDraftRepository.deleteBySubmission_Id(saved.getId());
			}
			return saved;
		}).orElseGet(() -> submissionRepository.save(new Submission(task, intern, repo, sha, scope, status)));
	}

	private static String trimRequired(String value, String message) {
		String t = value == null ? "" : value.trim();
		if (!StringUtils.hasText(t)) {
			throw new IllegalArgumentException(message);
		}
		return t;
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String t = value.trim();
		return StringUtils.hasText(t) ? t : null;
	}
}
