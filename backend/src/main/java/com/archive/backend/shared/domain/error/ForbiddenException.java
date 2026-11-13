package com.archive.backend.shared.domain.error;

/**
 * The signed-in user may not do what the request asks.
 */
public class ForbiddenException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public ForbiddenException(String code, String message) {
        super(code, message);
    }
}
