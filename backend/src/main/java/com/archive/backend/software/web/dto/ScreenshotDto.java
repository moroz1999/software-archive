package com.archive.backend.software.web.dto;

import java.util.UUID;

/** A screenshot and the address of its picture. */
public record ScreenshotDto(UUID id, String url) {}
