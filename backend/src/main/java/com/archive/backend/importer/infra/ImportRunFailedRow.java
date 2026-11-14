package com.archive.backend.importer.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A program a run could not add. */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportRunFailedRow {

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String reason;
}
