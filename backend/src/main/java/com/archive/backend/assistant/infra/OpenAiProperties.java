package com.archive.backend.assistant.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The OpenAI account of the assistant: the key (from {@code OPENAI_API_KEY}; none, and the assistant says it is not
 * configured), the model, and the address of the API, which the tests point to a server of their own.
 */
@ConfigurationProperties(prefix = "assistant.openai")
public record OpenAiProperties(String apiKey, String model, String baseUrl) {}
