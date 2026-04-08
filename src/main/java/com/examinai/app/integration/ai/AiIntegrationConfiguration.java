package com.examinai.app.integration.ai;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiDraftAssessmentProperties.class)
public class AiIntegrationConfiguration {

	@Bean
	ChatClient chatClient(ChatModel chatModel) {
		return ChatClient.create(chatModel);
	}

	/** Virtual-thread executor for bounded AI calls; closed on context shutdown. */
	@Bean(destroyMethod = "close")
	ExecutorService aiDraftExecutor() {
		return Executors.newVirtualThreadPerTaskExecutor();
	}
}
