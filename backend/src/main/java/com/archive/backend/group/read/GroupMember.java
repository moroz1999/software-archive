package com.archive.backend.group.read;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A member of a group under the label they were a member under; the person behind it when known, with their main
 * name unless it is that label.
 */
public record GroupMember(String name, @Nullable UUID personId, @Nullable String personName) {
}
