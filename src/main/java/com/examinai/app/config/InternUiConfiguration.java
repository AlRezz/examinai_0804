package com.examinai.app.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(InternUiProperties.class)
public class InternUiConfiguration {
}
