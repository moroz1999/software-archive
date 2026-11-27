package com.archive.backend.group.domain;

import com.archive.backend.author.domain.PersonReference;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.LinkedHashSet;
import java.util.List;

public record GroupMemberDraft(PersonReference person, List<String> roles) {

    /** The roles of a group member, in the order a form offers them. */
    public static final List<String> ALLOWED_ROLES = List.of(
            "coder",
            "cracker",
            "graphician",
            "hardware",
            "musician",
            "organizer",
            "support",
            "tester",
            "gamedesigner",
            "unknown");

    public GroupMemberDraft {
        roles = roles.isEmpty() ? List.of("unknown") : List.copyOf(new LinkedHashSet<>(roles));
        if (!ALLOWED_ROLES.containsAll(roles)) {
            throw new InvalidValueException("group.member-role-invalid", "A group member role is invalid.");
        }
    }
}
