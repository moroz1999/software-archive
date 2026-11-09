package com.archive.backend.author.domain;

import com.archive.backend.shared.domain.error.NotFoundException;

public class AuthorNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public AuthorNotFoundException() {
        super("author.not-found", "Author not found.");
    }
}
