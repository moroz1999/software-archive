package com.archive.backend.hardware.web.dto;

import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Body of adding an item of hardware, its code with it, and of editing one: its names by language code, the code of
 * its group and the codes of its platforms.
 */
public record HardwareRequestDto(
        @Nullable String code,
        @Nullable Map<String, String> names,
        @Nullable String group,
        @Nullable List<String> platforms) {}
