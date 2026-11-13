package com.archive.backend.importer.domain;

import com.archive.backend.label.domain.LabelName;
import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/**
 * Where an imported entry came from: the source archive, the kind of the entry and the id it has there; a name that
 * has no id there is known by the name itself (docs/domain/imports.md).
 */
@ValueObject
public record ExternalOrigin(String archive, ExternalKind kind, String externalId) {

    public ExternalOrigin {
        Objects.requireNonNull(archive, "archive");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(externalId, "externalId");
    }

    public static ExternalOrigin software(String archive, String id) {
        return new ExternalOrigin(archive, ExternalKind.SOFTWARE, id);
    }

    public static ExternalOrigin label(String archive, LabelName name) {
        return new ExternalOrigin(archive, ExternalKind.LABEL, name.value());
    }
}
