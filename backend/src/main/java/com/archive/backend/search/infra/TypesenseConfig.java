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
            @Value("${search.typesense.timeout}") Duration timeout) {
        Node node = new Node(url.getScheme(), url.getHost(), String.valueOf(url.getPort()));
        return new Client(new org.typesense.api.Configuration(List.of(node), timeout, apiKey));
    }
}
