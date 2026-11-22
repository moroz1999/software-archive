package com.archive.backend.search.read;

import java.util.List;
import java.util.Objects;

/**
 * A page of the catalog with the values of its filters, the hardware among them named by the reference book: the index
 * knows its codes only.
 */
public record SoftwareCatalogAnswer(SoftwareCatalogPage catalog, List<HardwareCount> hardware) {

    public SoftwareCatalogAnswer {
        Objects.requireNonNull(catalog, "catalog");
        hardware = List.copyOf(hardware);
    }
}
