package com.archive.backend.search.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Arrays;

/** A kind of what the full search finds, in the order its results are listed (docs/domain/search.md#full-search). */
@ValueObject
public enum FoundKind {
    AUTHOR("author"),
    GROUP("group"),
    SOFTWARE("software");

    private final String code;

    FoundKind(String code) {
        this.code = code;
    }

    /** The code the kind is asked by. */
    public String code() {
        return code;
    }

    /** The kind of the code; fails as an invalid value for any other. */
    public static FoundKind of(String code) {
        return Arrays.stream(values())
                .filter(kind -> kind.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new InvalidValueException(
                        "search.type-invalid", "A kind of search results is one of author, group, software."));
    }
}
