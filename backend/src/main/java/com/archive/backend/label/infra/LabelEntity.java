package com.archive.backend.label.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * A label as its owner holds it; the owner column is the join column of the owner's entity.
 */
@Entity
@Table(name = "label")
@Data
public class LabelEntity {
    /**
     * Assigned by the domain, never generated.
     */
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_display_name")
    private @Nullable Boolean display;

    /**
     * Set by the database default.
     */
    @Column(name = "created_at", columnDefinition = "timestamptz", insertable = false, updatable = false)
    private Instant createdAt;
}
