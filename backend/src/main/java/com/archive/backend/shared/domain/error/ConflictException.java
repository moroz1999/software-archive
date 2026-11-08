package com.archive.backend.shared.domain.error;

/**
 * The request clashes with the current state: a duplicate or a concurrent change.
 */
public class ConflictException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public ConflictException(String code, String message) {
        super(code, message);
    }
}
