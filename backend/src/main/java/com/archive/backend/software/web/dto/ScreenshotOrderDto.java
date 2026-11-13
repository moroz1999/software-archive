package com.archive.backend.software.web.dto;

import java.util.List;
import java.util.UUID;

/** Every screenshot in its new order. */
public record ScreenshotOrderDto(List<UUID> order) {}
