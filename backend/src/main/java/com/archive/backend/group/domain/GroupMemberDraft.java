package com.archive.backend.group.domain;

import com.archive.backend.author.domain.PersonReference;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public record GroupMemberDraft(
        PersonReference person,
        List<String> roles,
        @Nullable LocalDate startDate,
        @Nullable LocalDate endDate) {

    public static final Set<String> ALLOWED_ROLES = Set.of(
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
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidValueException(
                    "group.member-dates-invalid", "A group membership cannot end before it starts.");
        }
    }
}
