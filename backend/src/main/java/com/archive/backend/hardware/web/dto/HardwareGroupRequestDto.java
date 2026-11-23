package com.archive.backend.hardware.web.dto;

import java.util.Map;
import org.jspecify.annotations.Nullable;

/** Body of adding a group of hardware, its code with it, and of renaming one: its names by language code. */
public record HardwareGroupRequestDto(
        @Nullable String code, @Nullable Map<String, String> names) {}
