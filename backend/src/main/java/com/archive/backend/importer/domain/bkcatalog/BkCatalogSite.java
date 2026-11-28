package com.archive.backend.importer.domain.bkcatalog;

import java.util.List;
import java.util.Optional;

/**
 * bk-catalog, the catalog of software for the BK (docs/features/bk-catalog-import.md): its lists and the files of its
 * repository.
 */
public interface BkCatalogSite {

    /** The name the archive knows the source by. */
    String ARCHIVE = "bk-catalog";

    /** The home page of the catalog, where its cards open. */
    String HOME = "https://kalininskiy.github.io/bk-catalog/";

    /** Where the files of the repository are read from. */
    String FILES = "https://raw.githubusercontent.com/kalininskiy/bk-catalog/main/";

    /** Every entry of the list of the section, in its order. */
    List<BkCatalogEntry> entries(BkCatalogSection section);

    /** A file of the repository by its path there, such as {@code bk_games_files/elite.zip}; none when it lacks it. */
    Optional<byte[]> download(String path);

    /** The address of the list of the section. */
    static String listUrl(BkCatalogSection section) {
        return FILES + "content/" + section.code() + ".csv";
    }

    /** The card of an entry on the home page of the catalog, such as {@code …/bk-catalog/#game-555901}. */
    static String cardUrl(BkCatalogSection section, String id) {
        return HOME + "#" + section.card() + "-" + id;
    }
}
