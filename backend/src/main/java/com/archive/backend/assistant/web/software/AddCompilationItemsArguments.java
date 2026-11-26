package com.archive.backend.assistant.web.software;

import java.util.List;
import java.util.UUID;

public record AddCompilationItemsArguments(UUID id, List<UUID> compilationItemIds) {}
