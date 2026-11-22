package com.archive.backend.importer.domain.vektor;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.List;
import java.util.Objects;

/**
 * A page of a list of vektor-06c.ru as it was read: its address, the programs it lists in their order, and, on the
 * first page, the addresses of the other pages of the list its pagination gives.
 */
@ValueObject
public record VektorListPage(String url, List<VektorListing> listings, List<String> otherPages) {

    public VektorListPage {
        Objects.requireNonNull(url, "url");
        listings = List.copyOf(listings);
        otherPages = List.copyOf(otherPages);
    }
}
