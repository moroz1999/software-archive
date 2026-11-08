package com.archive.backend.shared.domain.error;

/**
 * The requested entity does not exist.
 */
public class NotFoundException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String code, String message) {
        super(code, message);
    }
}
