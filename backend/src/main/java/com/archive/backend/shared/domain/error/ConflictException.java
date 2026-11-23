package com.archive.backend.shared.domain.error;

import org.jspecify.annotations.Nullable;

/**
 * The request clashes with the current state: a duplicate or a concurrent change. {@link #field()} names the request
 * field the clash concerns, if any: a code another entry has already.
 */
public class ConflictException extends BusinessException {

    private static final long serialVersionUID = 1L;

    private final @Nullable String field;

    public ConflictException(String code, String message) {
        this(code, message, null);
    }

    public ConflictException(String code, String message, @Nullable String field) {
        super(code, message);
        this.field = field;
    }

    public @Nullable String field() {
        return field;
    }
}
