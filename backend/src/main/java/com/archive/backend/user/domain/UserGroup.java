package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

/**
 * A user group: what a user may do comes from their groups (docs/domain/users.md).
 */
@ValueObject
public enum UserGroup {
    ADMIN("admin"),
    MODERATOR("moderator"),
    MEMBER("member"),
    PUBLIC_VISITOR("public-visitor");

    private final String code;

    UserGroup(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static UserGroup ofCode(String code) {
        for (UserGroup group : values()) {
            if (group.code.equals(code)) {
                return group;
            }
        }
        throw new IllegalArgumentException("Unknown user group: " + code);
    }
}
