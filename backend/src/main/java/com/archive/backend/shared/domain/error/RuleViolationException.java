package com.archive.backend.shared.domain.error;

import org.jspecify.annotations.Nullable;

/**
 * The request is a correct form, but a business rule refuses its substance.
 * {@link #field()} names the request field the rule concerns, if any.
 */
public class RuleViolationException extends BusinessException {

    private static final long serialVersionUID = 1L;

    private final @Nullable String field;

    public RuleViolationException(String code, String message) {
        this(code, message, null);
    }

    public RuleViolationException(String code, String message, @Nullable String field) {
        super(code, message);
        this.field = field;
    }

    public @Nullable String field() {
        return field;
    }
}
