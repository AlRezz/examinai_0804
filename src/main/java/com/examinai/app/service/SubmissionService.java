package com.examinai.app.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

	private final TaskRepository taskRepository;
	private final UserRepository userRepository;
	private final SubmissionRepository submissionRepository;
	private final InternTaskService internTaskService;

	public SubmissionService(TaskRepository taskRepository, UserRepository userRepository,
			SubmissionRepository submissionRepository, InternTaskService internTaskService) {
		this.taskRepository = taskRepository;
		this.userRepository = userRepository;
		this.submissionRepository = submissionRepository;
		this.internTaskService = internTaskService;
	}

	@Transactional(readOnly = true)
	public Submission findForInternTask(UUID taskId, UUID internUserId) {
		return submissionRepository.findByTask_IdAndIntern_Id(taskId, internUserId).orElse(null);
	}

	@Transactional
	public Submission upsertCoordinates(UUID taskId, UUID internUserId, String repoIdentifier, String commitSha,
			String pathScope, SubmissionStatus status) {
		if (!internTaskService.isAssigned(taskId, internUserId)) {
			throw new IllegalArgumentException("Not assigned to this task.");
		}
		Task task = taskRepository.findById(taskId).orElseThrow();
		User intern = userRepository.findById(internUserId).orElseThrow();
		String repo = trimRequired(repoIdentifier, "Repository is required.");
		String sha = trimRequired(commitSha, "Commit SHA is required.");
		String scope = trimToNull(pathScope);

		return submissionRepository.findByTask_IdAndIntern_Id(taskId, internUserId).map(existing -> {
			existing.setRepoIdentifier(repo);
			existing.setCommitSha(sha);
			existing.setPathScope(scope);
			existing.setStatus(status);
			return submissionRepository.save(existing);
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
