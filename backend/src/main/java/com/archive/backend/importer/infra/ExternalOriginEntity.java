package com.archive.backend.importer.infra;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "external_origin")
@Data
public class ExternalOriginEntity {

    @EmbeddedId
    private ExternalOriginKey key;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "url")
    private @Nullable String url;
}
