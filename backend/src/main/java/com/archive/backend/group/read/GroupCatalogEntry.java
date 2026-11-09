package com.archive.backend.group.read;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * An entry of the group catalog: a group under its main name, or one of its other labels. The name of the group is
 * shown next to the entry, unless it is the entry's name itself.
 */
public record GroupCatalogEntry(
        UUID groupId, String name, @Nullable String groupName) {}
