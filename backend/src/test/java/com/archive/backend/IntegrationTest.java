package com.archive.backend;

import com.archive.backend.remote.RemoteTestConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * An integration test against the whole application, its database, its search and its REST API through MockMvc
 * (docs/testing.md). Every test so
 * marked shares one application context, started once per test JVM, so a test adds nothing to it of its own.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class, RemoteTestConfiguration.class})
public @interface IntegrationTest {}
