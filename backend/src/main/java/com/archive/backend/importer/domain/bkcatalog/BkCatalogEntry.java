package com.archive.backend.importer.domain.bkcatalog;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Map;
import java.util.Objects;

/**
 * One entry of a list of bk-catalog as the site gives it: the columns of its row by their names, such as
 * {@code Название}.
 */
@ValueObject
public record BkCatalogEntry(BkCatalogSection section, Map<String, String> columns) {

    public BkCatalogEntry {
        Objects.requireNonNull(section, "section");
        columns = Map.copyOf(columns);
    }

    /** The value of a column without the blanks around it; empty when the row has no such column or leaves it empty. */
    public String get(String column) {
        return columns.getOrDefault(column, "").strip();
    }

    /** The id of the entry in its section. */
    public String id() {
        return get("ID");
    }
}
