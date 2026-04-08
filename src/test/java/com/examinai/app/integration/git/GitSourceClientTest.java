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

	@Test
	void fetchesAndDecodesBase64FileContent() {
		GitProviderProperties props = new GitProviderProperties();
		props.setBaseUrl("http://localhost");
		props.setMaxRetries(1);
		RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		RestClient rc = builder.build();
		String json = "{\"type\":\"file\",\"size\":5,\"encoding\":\"base64\",\"content\":\"aGVsbG8=\"}";
		server.expect(once(), requestTo("http://localhost/repos/a/b/contents/README.md?ref=main"))
			.andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
		GitSourceClient client = new GitSourceClient(props, rc, new ObjectMapper());
		assertThat(client.fetchNormalizedFileContent("a/b", "main", null)).isEqualTo("hello");
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
		server.expect(once(), requestTo("http://localhost/repos/x/y/contents/p.java?ref=abc"))
			.andRespond(withStatus(HttpStatus.FORBIDDEN).body("{\"message\":\"ghp_leaked_in_body\"}")
				.contentType(MediaType.APPLICATION_JSON));
		GitSourceClient client = new GitSourceClient(props, rc, new ObjectMapper());
		assertThatThrownBy(() -> client.fetchNormalizedFileContent("x/y", "abc", "p.java")).isInstanceOf(GitProviderException.class)
			.satisfies(ex -> assertThat(((GitProviderException) ex).getKind()).isEqualTo(GitFailureKind.ACCESS_DENIED))
			.satisfies(ex -> assertThat(ex.getMessage()).doesNotContain("ghp_"));
		server.verify();
	}
}
