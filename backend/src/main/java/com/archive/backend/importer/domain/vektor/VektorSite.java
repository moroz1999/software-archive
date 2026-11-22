package com.archive.backend.importer.domain.vektor;

import org.jspecify.annotations.Nullable;

/**
 * vektor-06c.ru as the import reads it.
 */
public interface VektorSite {

    String ARCHIVE = "vektor-06c.ru";

    /** The first page of the list of the whole site, or of one of its sections, with the addresses of the others. */
    VektorListPage firstPage(@Nullable VektorSection section);

    /** Another page of a list, by the address the first one gave. */
    VektorListPage page(String url);

    VektorProgram program(VektorListing listing);

    /**
     * The bytes of a file of the site, such as an archive or a picture, by its absolute address.
     */
    byte[] download(String url);
}
