package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * One credit of software: a person, a group or the exact label of either, with the roles they had in it; publishing
 * and developing the software are roles too (docs/domain/software.md#credits).
 */
public record SoftwareCreditDraft(SoftwareReference participant, List<String> roles) {

    public static final Set<String> ALLOWED_ROLES = Set.of(
            "unknown",
            "developer",
            "publisher",
            "code",
            "release",
            "adaptation",
            "restoring",
            "music",
            "sfx",
            "support",
            "testing",
            "graphics",
            "loading_screen",
            "intro_code",
            "intro_graphics",
            "intro_music",
            "organizing",
            "direction",
            "3dmodels",
            "design",
            "logo",
            "font",
            "ascii",
            "illustrating",
            "tools",
            "localization",
            "concept",
            "gamedesign",
            "leveldesign",
            "story",
            "text",
            "editing",
            "translation",
            "video",
            "guest");

    public SoftwareCreditDraft {
        Objects.requireNonNull(participant, "participant");
        roles = roles.isEmpty() ? List.of("unknown") : List.copyOf(new LinkedHashSet<>(roles));
        if (!ALLOWED_ROLES.containsAll(roles)) {
            throw new InvalidValueException("software.credit-role-invalid", "A software credit role is invalid.");
        }
    }
}
