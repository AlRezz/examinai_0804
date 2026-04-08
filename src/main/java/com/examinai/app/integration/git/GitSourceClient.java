package com.examinai.app.integration.git;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.examinai.app.config.GitProviderProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Git provider HTTP access using a single {@link RestClient} (Story 3.2). Targets GitHub REST v3-style
 * {@code GET /repos/{owner}/{repo}/contents/{path}?ref=sha}; other hosts can work if they mirror this shape at
 * {@code examinai.git.base-url}.
 */
public class GitSourceClient {

	private static final Logger log = LoggerFactory.getLogger(GitSourceClient.class);

	private final GitProviderProperties properties;

	private final RestClient restClient;

	private final ObjectMapper objectMapper;

	public GitSourceClient(GitProviderProperties properties, RestClient restClient, ObjectMapper objectMapper) {
		this.properties = properties;
		this.restClient = restClient;
		this.objectMapper = objectMapper;
	}

	/**
	 * Fetches a single file as UTF-8 text. When {@code pathScope} is blank, {@code README.md} is used at the repo root.
	 */
	public String fetchNormalizedFileContent(String repoIdentifier, String commitSha, String pathScope) {
		if (!StringUtils.hasText(properties.getBaseUrl())) {
			throw new GitProviderException(GitFailureKind.CONFIG_MISSING, "Git provider is not configured.");
		}
		String trimmedRepo = repoIdentifier.trim();
		int slash = trimmedRepo.indexOf('/');
		if (slash < 1 || slash == trimmedRepo.length() - 1) {
			throw new GitProviderException(GitFailureKind.INVALID_RESPONSE,
					"Repository must be owner/name (e.g. octocat/Hello-World).");
		}
		String owner = trimmedRepo.substring(0, slash);
		String repo = trimmedRepo.substring(slash + 1);
		String path = StringUtils.hasText(pathScope) ? pathScope.trim() : "README.md";
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		String sha = commitSha.trim();
		URI uri = contentsUri(owner, repo, path, sha);
		int max = Math.max(1, properties.getMaxRetries());
		GitProviderException lastEx = null;
		for (int attempt = 0; attempt < max; attempt++) {
			try {
				return executeGet(uri);
			}
			catch (GitProviderException ex) {
				lastEx = ex;
				if (!isRetriable(ex.getKind()) || attempt == max - 1) {
					throw ex;
				}
				backoff(attempt);
			}
			catch (RestClientException ex) {
				log.warn("Git HTTP client failure: {}", LogRedactionUtil.safeForLog(ex.getMessage()));
				lastEx = new GitProviderException(GitFailureKind.TIMEOUT, "Network error talking to Git host.", ex);
				if (attempt == max - 1) {
					throw lastEx;
				}
				backoff(attempt);
			}
		}
		throw lastEx != null ? lastEx
				: new GitProviderException(GitFailureKind.UPSTREAM_ERROR, "Git fetch failed after retries.");
	}

	private String executeGet(URI uri) {
		var spec = restClient.get().uri(uri).acceptCharset(StandardCharsets.UTF_8);
		spec = spec.header(HttpHeaders.ACCEPT, "application/vnd.github+json");
		if (StringUtils.hasText(properties.getToken())) {
			spec = spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getToken());
		}
		String body;
		try {
			body = spec.retrieve().body(String.class);
		}
		catch (RestClientResponseException ex) {
			throw mapHttpException(ex);
		}
		if (body == null || body.isBlank()) {
			throw new GitProviderException(GitFailureKind.INVALID_RESPONSE, "Empty response from Git host.");
		}
		return decodeFileContent(body);
	}

	private GitProviderException mapHttpException(RestClientResponseException ex) {
		HttpStatusCode s = ex.getStatusCode();
		int code = s != null ? s.value() : 0;
		String redacted = LogRedactionUtil.safeForLog(ex.getResponseBodyAsString(StandardCharsets.UTF_8));
		if (code == 403) {
			log.warn("Git provider returned 403 (body redacted/summary only): {}", LogRedactionUtil.safeForLog(redacted));
			return new GitProviderException(GitFailureKind.ACCESS_DENIED, "Access was denied by the Git host.", ex);
		}
		if (code == 404) {
			return new GitProviderException(GitFailureKind.NOT_FOUND, "Repository, commit, or path was not found.", ex);
		}
		if (code == 429) {
			log.warn("Git provider rate limit (429)");
			return new GitProviderException(GitFailureKind.RATE_LIMIT, "Git host rate limit reached.", ex);
		}
		if (code >= 500) {
			log.warn("Git provider server error status {}", code);
			return new GitProviderException(GitFailureKind.UPSTREAM_ERROR, "Git host returned a server error.", ex);
		}
		log.warn("Git provider unexpected status {}: {}", code, LogRedactionUtil.safeForLog(ex.getMessage()));
		return new GitProviderException(GitFailureKind.INVALID_RESPONSE, "Unexpected response from Git host.", ex);
	}

	private String decodeFileContent(String jsonBody) {
		try {
			JsonNode n = objectMapper.readTree(jsonBody);
			String type = n.path("type").asText("");
			if ("dir".equals(type)) {
				throw new GitProviderException(GitFailureKind.INVALID_RESPONSE,
						"Path refers to a directory; set path scope to a single file.");
			}
			if (!"file".equals(type)) {
				throw new GitProviderException(GitFailureKind.INVALID_RESPONSE, "Git response is not a file object.");
			}
			long size = n.path("size").asLong(0L);
			if (size > 2_000_000L) {
				throw new GitProviderException(GitFailureKind.INVALID_RESPONSE,
						"File is too large to load; choose a smaller path scope.");
			}
			String b64 = n.path("content").asText("").replaceAll("\\s+", "");
			if (b64.isEmpty()) {
				return "";
			}
			byte[] raw = Base64.getDecoder().decode(b64);
			return new String(raw, StandardCharsets.UTF_8);
		}
		catch (GitProviderException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new GitProviderException(GitFailureKind.INVALID_RESPONSE, "Could not parse Git host response.", ex);
		}
	}

	private URI contentsUri(String owner, String repo, String path, String ref) {
		UriComponentsBuilder b = UriComponentsBuilder.fromUriString(trimTrailingSlash(properties.getBaseUrl()))
			.pathSegment("repos", owner, repo, "contents");
		for (String seg : path.split("/")) {
			if (!seg.isEmpty()) {
				b.pathSegment(seg);
			}
		}
		b.queryParam("ref", ref);
		return b.build().encode().toUri();
	}

	private static String trimTrailingSlash(String u) {
		return u.endsWith("/") ? u.substring(0, u.length() - 1) : u;
	}

	private static boolean isRetriable(GitFailureKind kind) {
		return kind == GitFailureKind.RATE_LIMIT || kind == GitFailureKind.UPSTREAM_ERROR;
	}

	private void backoff(int attempt) {
		try {
			long ms = (long) properties.getRetryBackoffMs() * (attempt + 1);
			Thread.sleep(Math.min(ms, 5_000L));
		}
		catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			throw new GitProviderException(GitFailureKind.TIMEOUT, "Interrupted during retry.", ie);
		}
	}
}
