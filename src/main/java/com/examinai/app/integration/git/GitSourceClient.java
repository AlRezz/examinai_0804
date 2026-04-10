package com.examinai.app.integration.git;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.examinai.app.config.GitProviderProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Git provider HTTP access using a single {@link RestClient} (Story 3.2). Loads
 * {@code GET /repos/{owner}/{repo}/commits/{ref}}, then resolves file text by: {@code files[].patch} if present,
 * else {@code raw_url}, else {@code contents_url} (Contents API JSON), else {@code GET …/contents/{path}?ref=}.
 */
public class GitSourceClient {

	private static final Logger log = LoggerFactory.getLogger(GitSourceClient.class);

	private static final int MAX_RETRIEVED_CHARS = 2_000_000;

	private final GitProviderProperties properties;

	private final RestClient restClient;

	private final ObjectMapper objectMapper;

	public GitSourceClient(GitProviderProperties properties, RestClient restClient, ObjectMapper objectMapper) {
		this.properties = properties;
		this.restClient = restClient;
		this.objectMapper = objectMapper;
	}

	/**
	 * Loads commit metadata and UTF-8 text for review. When {@code pathScope} is null or blank/whitespace,
	 * the path scope is treated as empty (no default file); output is commit metadata only.
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
		String path = pathScope == null ? "" : pathScope.trim();
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		if (commitSha == null || !StringUtils.hasText(commitSha.trim())) {
			throw new GitProviderException(GitFailureKind.INVALID_RESPONSE,
					"Commit ref (SHA, branch, or tag) is required.");
		}
		String ref = commitSha.trim();
		URI uri = commitUri(owner, repo, ref);
		int max = Math.max(1, properties.getMaxRetries());
		GitProviderException lastEx = null;
		for (int attempt = 0; attempt < max; attempt++) {
			try {
				String body = executeCommitGet(uri);
				return buildTextFromCommitResponse(body, owner, repo, ref, path);
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

	private String executeCommitGet(URI uri) {
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
		return body;
	}

	private GitProviderException mapHttpException(RestClientResponseException ex) {
		HttpStatusCode s = ex.getStatusCode();
		int code = s != null ? s.value() : 0;
		String redacted = LogRedactionUtil.safeForLog(ex.getResponseBodyAsString(StandardCharsets.UTF_8));
		if (code == 401) {
			log.warn("Git provider returned 401 (body redacted/summary only): {}", redacted);
			return new GitProviderException(GitFailureKind.ACCESS_DENIED,
					"Authentication failed or token is not valid for this Git host.", ex);
		}
		if (code == 403) {
			log.warn("Git provider returned 403 (body redacted/summary only): {}", redacted);
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

	private String buildTextFromCommitResponse(String jsonBody, String owner, String repo, String ref, String path) {
		final JsonNode root;
		try {
			root = objectMapper.readTree(jsonBody);
		}
		catch (JsonProcessingException ex) {
			throw new GitProviderException(GitFailureKind.INVALID_RESPONSE, "Could not parse Git host commit JSON.", ex);
		}
		String sha = root.path("sha").asText("");
		String htmlUrl = root.path("html_url").asText("");
		JsonNode commitNode = root.path("commit");
		String message = commitNode.path("message").asText("");
		String authorDate = commitNode.path("author").path("date").asText("");

		StringBuilder out = new StringBuilder();
		out.append("Commit ").append(sha.isEmpty() ? "(unknown)" : sha).append('\n');
		if (!htmlUrl.isEmpty()) {
			out.append("URL: ").append(htmlUrl).append('\n');
		}
		if (!authorDate.isEmpty()) {
			out.append("Author date: ").append(authorDate).append('\n');
		}
		out.append('\n').append(message).append("\n\n");
		JsonNode files = root.path("files");
		JsonNode fileNode = (files.isArray() && !files.isEmpty()) ? findFileForPath(files, path) : null;

		appendFileBodyFromGithubFileEntry(out, files.get(0), owner, repo, ref);
		
		enforceMaxLength(out);
		return out.toString();
	}

	/**
	 * Prefer {@code patch}; if empty, GET {@code raw_url}; if still empty, GET {@code contents_url} JSON; else
	 * repository Contents API for the file path at {@code ref}.
	 */
	private void appendFileBodyFromGithubFileEntry(StringBuilder out, JsonNode fileNode, String owner, String repo,
			String ref) {
		String patch = fileNode.path("patch").asText("");
		if (StringUtils.hasText(patch)) {
			out.append(patch);
			if (!patch.endsWith("\n")) {
				out.append('\n');
			}
			return;
		}
		String rawUrl = fileNode.path("raw_url").asText("");
		if (StringUtils.hasText(rawUrl)) {
			try {
				String raw = httpGetBody(URI.create(rawUrl.trim()), false);
				appendChunk(out, raw);
				return;
			}
			catch (GitProviderException ex) {
				log.debug("raw_url fetch failed, trying contents_url: {}", LogRedactionUtil.safeForLog(ex.getMessage()));
			}
		}
		String contentsUrl = fileNode.path("contents_url").asText("");
		if (StringUtils.hasText(contentsUrl)) {
			try {
				String json = httpGetBody(URI.create(contentsUrl.trim()), true);
				appendChunk(out, decodeContentsFileJson(json));
				return;
			}
			catch (GitProviderException ex) {
				log.debug("contents_url fetch failed, trying repository contents API: {}",
						LogRedactionUtil.safeForLog(ex.getMessage()));
			}
		}
		String fname = fileNode.path("filename").asText("");
		if (StringUtils.hasText(fname)) {
			appendChunk(out, fetchViaRepositoryContents(owner, repo, fname.replace('\\', '/'), ref));
		}
		else {
			out.append("(No patch, raw_url, contents_url, or filename in Git response.)\n");
		}
	}

	private void appendChunk(StringBuilder out, String chunk) {
		if (chunk.length() > MAX_RETRIEVED_CHARS) {
			throw new GitProviderException(GitFailureKind.INVALID_RESPONSE,
					"Retrieved text exceeds size limit; narrow path scope.");
		}
		out.append(chunk);
	}

	private String fetchViaRepositoryContents(String owner, String repo, String path, String ref) {
		URI u = contentsUri(owner, repo, path, ref);
		log.debug("Git contents fallback GET: {}", u);
		String json = httpGetBody(u, true);
		return decodeContentsFileJson(json);
	}

	private String httpGetBody(URI uri, boolean acceptGithubJson) {
		var spec = restClient.get().uri(uri).acceptCharset(StandardCharsets.UTF_8);
		if (acceptGithubJson) {
			spec = spec.header(HttpHeaders.ACCEPT, "application/vnd.github+json");
		}
		else {
			spec = spec.accept(MediaType.ALL);
		}
		if (StringUtils.hasText(properties.getToken())) {
			spec = spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getToken());
		}
		try {
			String body = spec.retrieve().body(String.class);
			if (body == null) {
				throw new GitProviderException(GitFailureKind.INVALID_RESPONSE, "Empty response from Git host.");
			}
			return body;
		}
		catch (RestClientResponseException ex) {
			throw mapHttpException(ex);
		}
		catch (RestClientException ex) {
			log.warn("Git HTTP client failure: {}", LogRedactionUtil.safeForLog(ex.getMessage()));
			throw new GitProviderException(GitFailureKind.TIMEOUT, "Network error talking to Git host.", ex);
		}
	}

	private String decodeContentsFileJson(String jsonBody) {
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

	private static JsonNode findFileForPath(JsonNode files, String path) {
		String normalized = path.replace('\\', '/');
		for (JsonNode f : files) {
			String fn = f.path("filename").asText("");
			if (!StringUtils.hasText(fn)) {
				continue;
			}
			if (fn.replace('\\', '/').equals(normalized)) {
				return f;
			}
		}
		return null;
	}

	private static void enforceMaxLength(StringBuilder out) {
		if (out.length() > MAX_RETRIEVED_CHARS) {
			throw new GitProviderException(GitFailureKind.INVALID_RESPONSE,
					"Retrieved text exceeds size limit; narrow path scope.");
		}
	}

	private URI commitUri(String owner, String repo, String ref) {
		UriComponentsBuilder b = UriComponentsBuilder.fromUriString(trimTrailingSlash(properties.getBaseUrl()))
			.pathSegment("repos", owner, repo, "commits", ref);
		log.debug("Git commit GET: {}", b.toUriString());
		return b.build().encode().toUri();
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
