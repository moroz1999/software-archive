package com.archive.backend.search.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import org.jspecify.annotations.Nullable;

/** The order of the software catalog: by title, or by rating (docs/domain/search.md#software-catalog). */
@ValueObject
public enum CatalogOrder {
    TITLE("title"),
    RATING("rating");

    private final String code;

    CatalogOrder(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    /** The order of its code; none is by title. */
    public static CatalogOrder of(@Nullable String code) {
        if (code == null) {
            return TITLE;
        }
        for (CatalogOrder order : values()) {
            if (order.code.equals(code)) {
                return order;
            }
        }
        throw new InvalidValueException("catalog.order-invalid", "The catalog is ordered by title or by rating.");
    }
}
