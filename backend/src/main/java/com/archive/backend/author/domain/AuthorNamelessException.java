package com.archive.backend.author.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * An author is known by a real name or a label (docs/domain/authors.md).
 */
public class AuthorNamelessException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public AuthorNamelessException() {
        super("author.nameless", "An author needs a real name or a label.", "realName");
    }
}
