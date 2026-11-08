package com.archive.backend.shared.domain.error;

/**
 * A failure the domain reports on purpose. Its stable {@link #code()} ({@code {domain}.{error}})
 * reaches the client, which picks the user-facing text by it.
 */
public abstract class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String code;

    protected BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
