package com.archive.backend.search;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.typesense.TypesenseContainer;
import org.typesense.api.Client;
import org.typesense.api.Configuration;
import org.typesense.api.exceptions.ObjectNotFound;
import org.typesense.resources.Node;

/** Looks into the Typesense of the tests as a reader would, and takes it down and up. */
public final class TypesenseProbe {

    private final TypesenseContainer container;
    private final Client client;

    public TypesenseProbe(TypesenseContainer container) {
        this.container = container;
        this.client = new Client(new Configuration(
                List.of(new Node("http", container.getHost(), container.getHttpPort())),
                Duration.ofSeconds(5),
                container.getApiKey()));
    }

    /** The document of the software in the collection the {@code software} alias points to. */
    public Optional<Map<String, Object>> software(UUID id) throws Exception {
        try {
            return Optional.of(
                    client.collections("software").documents(id.toString()).retrieve());
        } catch (ObjectNotFound e) {
            return Optional.empty();
        }
    }

    /** The collection an alias points to; empty without the alias. */
    public Optional<String> aliasTarget(String alias) throws Exception {
        try {
            return Optional.of(client.aliases(alias).retrieve().getCollectionName());
        } catch (ObjectNotFound e) {
            return Optional.empty();
        }
    }

    /** Freezes Typesense: every call to it times out until {@link #resume()}. */
    public void freeze() {
        DockerClientFactory.instance()
                .client()
                .pauseContainerCmd(container.getContainerId())
                .exec();
    }

    /** Lets a frozen Typesense go on; does nothing to a running one. */
    public void resume() {
        var docker = DockerClientFactory.instance().client();
        Boolean paused = docker.inspectContainerCmd(container.getContainerId())
                .exec()
                .getState()
                .getPaused();
        if (Boolean.TRUE.equals(paused)) {
            docker.unpauseContainerCmd(container.getContainerId()).exec();
        }
    }
}
