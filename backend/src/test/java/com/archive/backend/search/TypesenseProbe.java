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
import org.typesense.api.FieldTypes;
import org.typesense.api.exceptions.ObjectNotFound;
import org.typesense.model.CollectionAliasSchema;
import org.typesense.model.CollectionSchema;
import org.typesense.model.Field;
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

    /**
     * Puts in use, under the {@code software} alias, a collection built by an older schema: it lacks fields the documents
     * have now.
     */
    public void aliasAnOutdatedSoftwareCollection() throws Exception {
        String name = "software_outdated_" + UUID.randomUUID().toString().replace("-", "");
        client.collections()
                .create(new CollectionSchema()
                        .name(name)
                        .fields(List.of(new Field().name("title").type(FieldTypes.STRING))));
        client.aliases().upsert("software", new CollectionAliasSchema().collectionName(name));
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
