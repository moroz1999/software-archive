package com.archive.backend.search.infra;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.typesense.api.Client;
import org.typesense.resources.Node;

@Configuration
public class TypesenseConfig {

    @Bean
    Client typesenseClient(
            @Value("${search.typesense.url}") URI url,
            @Value("${search.typesense.api-key}") String apiKey,
            @Value("${search.typesense.timeout}") Duration timeout,
            @Value("${search.typesense.attempts}") int attempts,
            @Value("${search.typesense.retry-interval}") Duration retryInterval) {
        Node node = new Node(url.getScheme(), url.getHost(), String.valueOf(url.getPort()));
        var configuration = new org.typesense.api.Configuration(List.of(node), timeout, apiKey);
        // The client names the number of attempts, the first one included, its retries.
        configuration.numRetries = attempts;
        configuration.retryInterval = retryInterval;
        return new Client(configuration);
    }
}
