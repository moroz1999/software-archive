package com.archive.backend.importer.domain.run;

import com.archive.backend.importer.domain.basis.BasisSite;
import com.archive.backend.importer.domain.vektor.VektorSite;
import com.archive.backend.shared.domain.ddd.ValueObject;

/** The source archives an import reads, each known by the name of its archive. */
@ValueObject
public enum ImportSource {
    VEKTOR(VektorSite.ARCHIVE),
    BASIS(BasisSite.ARCHIVE);

    private final String archive;

    ImportSource(String archive) {
        this.archive = archive;
    }

    public String archive() {
        return archive;
    }
}
