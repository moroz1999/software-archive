package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** What a user may do beyond reading, by their groups (docs/features/accounts.md), each known by its code. */
@ValueObject
public enum Ability {
    ADD_ENTRIES("add-entries"),
    KEEP_CATEGORIES("keep-categories"),
    USE_ASSISTANT("use-assistant");

    private final String code;

    Ability(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
