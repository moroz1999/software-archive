package com.archive.backend.user.web.dto;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * An account as an administrator changes it: the groups are those given on purpose, by their codes.
 */
public record UserRequestDto(
        @Nullable String name,
        @Nullable String email,
        @Nullable List<String> groups) {}
