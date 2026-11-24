package com.archive.backend.assistant.web.tool;

import java.util.UUID;

/**
 * What a write produced: the id of the entry it created, or of the one it changed, merged into or made. A later
 * change of the same plan may take it.
 */
public record Written(UUID id) {}
