package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.error.NotFoundException;

public class UserNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public UserNotFoundException() {
        super("user.not-found", "The user does not exist.");
    }
}
