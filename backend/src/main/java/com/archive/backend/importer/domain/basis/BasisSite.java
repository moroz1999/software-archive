package com.archive.backend.importer.domain.basis;

import java.util.List;
import org.jspecify.annotations.Nullable;

/** The card index "Базис" at caglrc.cc as the import reads it. */
public interface BasisSite {

    String ARCHIVE = "caglrc.cc";
    String CATEGORIES = "https://caglrc.cc/scalar/categories/";

    /** The one list of every card of the index. */
    String LIST = "https://caglrc.cc/scalar/categories/all/";

    /** Every card of the whole index or one category, in the order of that list. */
    List<BasisListing> listings(@Nullable BasisCategory category);

    static String listUrl(@Nullable BasisCategory category) {
        return category == null ? LIST : CATEGORIES + category.code() + "/";
    }

    BasisProgram program(BasisListing listing);

    /** The page of an author by its id there; each is read once in a run. */
    BasisAuthor author(String id);

    /** The bytes of a file of the site, such as a program or a screenshot, by its absolute address. */
    byte[] download(String url);
}
