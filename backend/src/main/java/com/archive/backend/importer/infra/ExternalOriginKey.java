package com.archive.backend.importer.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.Data;

@Embeddable
@Data
public class ExternalOriginKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "origin", nullable = false)
    private String origin;

    @Column(name = "kind", nullable = false)
    private String kind;

    @Column(name = "external_id", nullable = false)
    private String externalId;
}
