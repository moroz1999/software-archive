package com.archive.backend.importer.domain.vektor;

import org.jspecify.annotations.Nullable;

/**
 * vektor-06c.ru as the import reads it.
 */
public interface VektorSite {

    String ARCHIVE = "vektor-06c.ru";

    /**
     * Every program of the site, or of one of its sections, in the order of its lists.
     */
    Iterable<VektorListing> listings(@Nullable VektorSection section);

    VektorProgram program(VektorListing listing);

    /**
     * The bytes of a file of the site, such as an archive or a picture, by its absolute address.
     */
    byte[] download(String url);
}
