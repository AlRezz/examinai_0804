package com.examinai.app.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examinai.app.domain.task.GitRetrievalState;
import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionRepository;
import com.examinai.app.integration.git.GitProviderException;
import com.examinai.app.integration.git.GitSourceClient;

/**
 * Loads normalized source text from the configured Git provider and updates {@link Submission} snapshot fields (Stories 3.2–3.3).
 * A failed attempt does not clear prior successful text.
 */
@Service
public class SourceRetrievalService {

	private static final Logger log = LoggerFactory.getLogger(SourceRetrievalService.class);

	private final SubmissionRepository submissionRepository;

	private final GitSourceClient gitSourceClient;

	public SourceRetrievalService(SubmissionRepository submissionRepository, GitSourceClient gitSourceClient) {
		this.submissionRepository = submissionRepository;
		this.gitSourceClient = gitSourceClient;
	}

	@Transactional
	public void retrieveAndPersist(java.util.UUID submissionId) {
		log.debug("retrieveAndPersist: submissionId={}", submissionId);
		Submission s = submissionRepository.findById(submissionId).orElseThrow();
		s.setGitRetrievalState(GitRetrievalState.IN_PROGRESS);
		s.setGitRetrievalErrorCode(null);
		submissionRepository.save(s);
		try {
			String text = gitSourceClient.fetchNormalizedFileContent(s.getRepoIdentifier(), s.getCommitSha(), s.getPathScope());
			s.setGitRetrievedText(text);
			s.setGitRetrievalState(GitRetrievalState.OK);
			s.setGitLastSuccessAt(Instant.now());
		}
		catch (GitProviderException ex) {
			log.debug("retrieveAndPersist failed: submissionId={}, kind={}", submissionId, ex.getKind());
			s.setGitRetrievalState(GitRetrievalState.ERROR);
			s.setGitRetrievalErrorCode(ex.getKind().name());
		}
		finally {
			s.setGitLastAttemptAt(Instant.now());
			s.setGitFetchVersion(s.getGitFetchVersion() + 1);
			submissionRepository.save(s);
		}
	}
}
