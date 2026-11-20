package com.archive.backend;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.typesense.TypesenseContainer;

/**
 * Typesense for integration tests: one throwaway container with the version of docker-compose per database of the
 * test JVM (TestcontainersConfiguration), shared by the test contexts on that database, so the index always follows
 * its database. The search worker does not run on a schedule in tests (src/test/resources/config/application.properties):
 * a test drives it.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TypesenseTestConfiguration {

    /** One Typesense per test database of the JVM, started for the first context that uses that database. */
    private static final Map<String, TypesenseContainer> BY_DATABASE = new ConcurrentHashMap<>();

    /**
     * For a test that freezes it. A context closing stops it, which happens only when the JVM ends: the few contexts of
     * the tests all fit the context cache.
     */
    @Bean
    TypesenseContainer typesenseContainer(Environment environment) {
        return BY_DATABASE.computeIfAbsent(environment.getRequiredProperty("test.database"), database -> {
            TypesenseContainer typesense = new TypesenseContainer("typesense/typesense:28.0");
            typesense.start();
            return typesense;
        });
    }

    @Bean
    DynamicPropertyRegistrar typesenseProperties(TypesenseContainer typesense) {
        return registry -> {
            registry.add("search.typesense.url", () -> "http://" + typesense.getHost() + ":" + typesense.getHttpPort());
            registry.add("search.typesense.api-key", typesense::getApiKey);
            registry.add("search.typesense.timeout", () -> "1s");
            // A test that freezes Typesense waits for one failed call, not for the retries production makes.
            registry.add("search.typesense.attempts", () -> "1");
            registry.add("search.typesense.retry-interval", () -> "0s");
        };
    }
}
