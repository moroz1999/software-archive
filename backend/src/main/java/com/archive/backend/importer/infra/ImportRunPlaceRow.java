package com.archive.backend.importer.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A place a run added. */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportRunPlaceRow {

    @Column(nullable = false)
    private String kind;

    @Column(nullable = false)
    private String name;
}
