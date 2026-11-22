package com.archive.backend.importer.domain.basis;

import java.util.List;

/** The card index "Базис" at caglrc.cc as the import reads it. */
public interface BasisSite {

    String ARCHIVE = "caglrc.cc";

    /** The one list of every card of the index. */
    String LIST = "https://caglrc.cc/scalar/categories/all/";

    /** Every card of the index, in the order of its whole list. */
    List<BasisListing> listings();

    BasisProgram program(BasisListing listing);

    /** The page of an author by its id there; each is read once in a run. */
    BasisAuthor author(String id);

    /** The bytes of a file of the site, such as a program or a screenshot, by its absolute address. */
    byte[] download(String url);
}
