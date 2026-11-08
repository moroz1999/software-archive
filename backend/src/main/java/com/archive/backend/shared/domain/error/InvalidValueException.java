package com.archive.backend.shared.domain.error;

/**
 * A value object refused a raw value that breaks its format rules.
 */
public class InvalidValueException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public InvalidValueException(String code, String message) {
        super(code, message);
    }
}
