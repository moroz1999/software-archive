package com.archive.backend.assistant.web.place;

import java.util.UUID;

/** A country or a city, and the one of its kind it is merged into. */
public record MergePlaceArguments(UUID id, UUID intoId) {}
