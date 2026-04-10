package com.examinai.app.integration.git;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.examinai.app.config.GitProviderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

class GitSourceClientTest {

	private static final String SAMPLE_COMMIT_JSON = """
			{"sha":"deadbeef","html_url":"http://localhost/c","commit":{"message":"init","author":{"date":"2020-01-01T00:00:00Z"}},"files":[{"filename":"README.md","status":"modified","patch":"@@ -1 +1 @@\\n-old\\n+hello"}]}
			""";

	@Test
	void fetchesCommitAndBuildsTextFromPatch() {
		GitProviderProperties props = new GitProviderProperties();
		props.setBaseUrl("http://localhost");
		props.setMaxRetries(1);
		RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		RestClient rc = builder.build();
		server.expect(once(), requestTo("http://localhost/repos/a/b/commits/main"))
			.andRespond(withSuccess(SAMPLE_COMMIT_JSON, MediaType.APPLICATION_JSON));
		GitSourceClient client = new GitSourceClient(props, rc, new ObjectMapper());
		String text = client.fetchNormalizedFileContent("a/b", "main", "README.md");
		assertThat(text).contains("deadbeef", "hello", "README.md");
		server.verify();
	}

	@Test
	void emptyPatchFetchesRawUrl() {
		GitProviderProperties props = new GitProviderProperties();
		props.setBaseUrl("http://localhost");
		props.setMaxRetries(1);
		RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		RestClient rc = builder.build();
		String commitJson = """
				{"sha":"s1","html_url":"u","commit":{"message":"m","author":{"date":"d"}},"files":[{"filename":"README.md","patch":"","raw_url":"http://localhost/raw/blob"}]}
				""";
		server.expect(once(), requestTo("http://localhost/repos/a/b/commits/main"))
			.andRespond(withSuccess(commitJson, MediaType.APPLICATION_JSON));
		server.expect(once(), requestTo("http://localhost/raw/blob"))
			.andRespond(withSuccess("raw-bytes", MediaType.TEXT_PLAIN));
		GitSourceClient client = new GitSourceClient(props, rc, new ObjectMapper());
		String text = client.fetchNormalizedFileContent("a/b", "main", "README.md");
		assertThat(text).contains("raw-bytes", "README.md");
		server.verify();
	}

	@Test
	void forbiddenDoesNotLeakTokenFromResponseBody() {
		GitProviderProperties props = new GitProviderProperties();
		props.setBaseUrl("http://localhost");
		props.setMaxRetries(1);
		RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		RestClient rc = builder.build();
		server.expect(once(), requestTo("http://localhost/repos/x/y/commits/abc"))
			.andRespond(withStatus(HttpStatus.FORBIDDEN).body("{\"message\":\"ghp_leaked_in_body\"}")
				.contentType(MediaType.APPLICATION_JSON));
		GitSourceClient client = new GitSourceClient(props, rc, new ObjectMapper());
		assertThatThrownBy(() -> client.fetchNormalizedFileContent("x/y", "abc", "p.java")).isInstanceOf(GitProviderException.class)
			.satisfies(ex -> assertThat(((GitProviderException) ex).getKind()).isEqualTo(GitFailureKind.ACCESS_DENIED))
			.satisfies(ex -> assertThat(ex.getMessage()).doesNotContain("ghp_"));
		server.verify();
	}

	@Test
	void unauthorizedMapsToAccessDenied() {
		GitProviderProperties props = new GitProviderProperties();
		props.setBaseUrl("http://localhost");
		props.setMaxRetries(1);
		RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		RestClient rc = builder.build();
		server.expect(once(), requestTo("http://localhost/repos/x/y/commits/main"))
			.andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("{\"message\":\"Bad credentials\"}")
				.contentType(MediaType.APPLICATION_JSON));
		GitSourceClient client = new GitSourceClient(props, rc, new ObjectMapper());
		assertThatThrownBy(() -> client.fetchNormalizedFileContent("x/y", "main", "README.md")).isInstanceOf(GitProviderException.class)
			.satisfies(ex -> assertThat(((GitProviderException) ex).getKind()).isEqualTo(GitFailureKind.ACCESS_DENIED));
		server.verify();
	}

	@Test
	void blankPathRejectedBeforeHttp() {
		GitProviderProperties props = new GitProviderProperties();
		props.setBaseUrl("http://localhost");
		props.setMaxRetries(1);
		RestClient rc = RestClient.builder().baseUrl("http://localhost").build();
		GitSourceClient client = new GitSourceClient(props, rc, new ObjectMapper());
		assertThatThrownBy(() -> client.fetchNormalizedFileContent("a/b", "main", null)).isInstanceOf(GitProviderException.class)
			.satisfies(ex -> assertThat(((GitProviderException) ex).getKind()).isEqualTo(GitFailureKind.INVALID_RESPONSE));
		assertThatThrownBy(() -> client.fetchNormalizedFileContent("a/b", "main", "  ")).isInstanceOf(GitProviderException.class)
			.satisfies(ex -> assertThat(((GitProviderException) ex).getKind()).isEqualTo(GitFailureKind.INVALID_RESPONSE));
	}

	@Test
	void blankCommitRefRejectedBeforeHttp() {
		GitProviderProperties props = new GitProviderProperties();
		props.setBaseUrl("http://localhost");
		props.setMaxRetries(1);
		RestClient rc = RestClient.builder().baseUrl("http://localhost").build();
		GitSourceClient client = new GitSourceClient(props, rc, new ObjectMapper());
		assertThatThrownBy(() -> client.fetchNormalizedFileContent("a/b", "  ", "README.md")).isInstanceOf(GitProviderException.class)
			.satisfies(ex -> assertThat(((GitProviderException) ex).getKind()).isEqualTo(GitFailureKind.INVALID_RESPONSE));
	}

	@Test
	void pathNotInCommitFilesFallsBackToRepositoryContents() {
		GitProviderProperties props = new GitProviderProperties();
		props.setBaseUrl("http://localhost");
		props.setMaxRetries(1);
		RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		RestClient rc = builder.build();
		String commitJson = """
				{"sha":"s","html_url":"u","commit":{"message":"m","author":{"date":"d"}},"files":[{"filename":"other.txt","patch":"x"}]}
				""";
		String contentsJson = "{\"type\":\"file\",\"size\":5,\"encoding\":\"base64\",\"content\":\"aGVsbG8=\"}";
		server.expect(once(), requestTo("http://localhost/repos/a/b/commits/main"))
			.andRespond(withSuccess(commitJson, MediaType.APPLICATION_JSON));
		server.expect(once(), requestTo("http://localhost/repos/a/b/contents/README.md?ref=main"))
			.andRespond(withSuccess(contentsJson, MediaType.APPLICATION_JSON));
		GitSourceClient client = new GitSourceClient(props, rc, new ObjectMapper());
		String text = client.fetchNormalizedFileContent("a/b", "main", "README.md");
		assertThat(text).contains("hello", "README.md");
		server.verify();
	}
}
