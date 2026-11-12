package com.archive.backend.group.read;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * A member of a group under the label they were a member under; the person behind it when known, with their main
 * name unless it is that label.
 */
public record GroupMember(
        @Nullable UUID id,
        String type,
        String title,
        String name,
        @Nullable UUID personId,
        @Nullable String personName,
        List<String> roles,
        @Nullable LocalDate startDate,
        @Nullable LocalDate endDate) {

    public GroupMember(String name, @Nullable UUID personId, @Nullable String personName) {
        this(personId, "author", name, name, personId, personName, List.of("unknown"), null, null);
    }
}
