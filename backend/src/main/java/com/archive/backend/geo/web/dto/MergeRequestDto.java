package com.archive.backend.geo.web.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Body of merging a country or a city into another one, the one that stays. */
public record MergeRequestDto(@Nullable UUID into) {}
