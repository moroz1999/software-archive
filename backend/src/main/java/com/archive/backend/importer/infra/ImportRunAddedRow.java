package com.archive.backend.importer.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A program a run added. */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportRunAddedRow {

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "software_id", nullable = false)
    private UUID softwareId;

    @Column(nullable = false)
    private String title;
}
