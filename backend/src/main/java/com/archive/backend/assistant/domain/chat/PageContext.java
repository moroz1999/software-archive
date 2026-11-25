package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.ValueObject;
import org.jspecify.annotations.Nullable;

/**
 * The page an editor wrote from: its address in the archive, its title, and on the page of an entry, its type and
 * id. None of it is known on a message from elsewhere than a page.
 */
@ValueObject
public record PageContext(
        @Nullable String path,
        @Nullable String title,
        @Nullable String entityType,
        @Nullable String entityId) {

    public static final PageContext NONE = new PageContext(null, null, null, null);
}
