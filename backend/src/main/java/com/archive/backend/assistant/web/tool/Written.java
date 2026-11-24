package com.archive.backend.assistant.web.tool;

import java.util.UUID;

/**
 * What a write produced: the id of the entry it created, or of the one it changed, merged into or made; the code of
 * hardware. A later change of the same plan may take it.
 */
public record Written(String id) {

    public static Written of(UUID id) {
        return new Written(id.toString());
    }

    /** The id as the id of an entry. */
    public UUID uuid() {
        return UUID.fromString(id);
    }
}
