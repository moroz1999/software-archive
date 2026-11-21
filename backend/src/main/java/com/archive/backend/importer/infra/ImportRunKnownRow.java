package com.archive.backend.importer.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A program a run found in the archive; what was added to it, by the codes of the parts joined by commas. */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportRunKnownRow {

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "software_id", nullable = false)
    private UUID softwareId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String supplements;
}
