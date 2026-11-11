package com.archive.backend;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.typesense.TypesenseContainer;

/**
 * Typesense for integration tests that start the whole application: a fresh container with the version of
 * docker-compose. The search worker does not run on a schedule here: a test drives it.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TypesenseTestConfiguration {

    @Bean
    TypesenseContainer typesenseContainer() {
        return new TypesenseContainer("typesense/typesense:28.0");
    }

    @Bean
    DynamicPropertyRegistrar typesenseProperties(TypesenseContainer typesense) {
        return registry -> {
            registry.add("search.typesense.url", () -> "http://" + typesense.getHost() + ":" + typesense.getHttpPort());
            registry.add("search.typesense.api-key", typesense::getApiKey);
            registry.add("search.typesense.timeout", () -> "1s");
            registry.add("search.worker.enabled", () -> "false");
        };
    }
}
