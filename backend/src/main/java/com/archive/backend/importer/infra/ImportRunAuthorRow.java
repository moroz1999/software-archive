package com.archive.backend.importer.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** An author a run took for the first time. */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportRunAuthorRow {

    @Column(nullable = false)
    private String kind;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean created;
}
