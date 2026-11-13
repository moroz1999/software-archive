package com.archive.backend.search.web.dto;

import java.util.UUID;

/** A screenshot a card shows, with where its picture is served. */
public record CardScreenshotDto(UUID id, String url) {}
