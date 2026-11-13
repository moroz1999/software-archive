package com.archive.backend.user.read;

import java.util.List;
import java.util.UUID;

/**
 * The signed-in user as they see themselves; groups and what they may do by their codes.
 */
public record CurrentUser(UUID id, String name, String email, List<String> groups, List<String> abilities) {

    /**
     * As read, before the handler tells what they may do.
     */
    public CurrentUser(UUID id, String name, String email, List<String> groups) {
        this(id, name, email, groups, List.of());
    }

    public CurrentUser withAbilities(List<String> abilities) {
        return new CurrentUser(id, name, email, groups, abilities);
    }
}
