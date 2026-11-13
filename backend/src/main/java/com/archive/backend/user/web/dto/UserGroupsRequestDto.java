package com.archive.backend.user.web.dto;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * The groups given on purpose, by their codes.
 */
public record UserGroupsRequestDto(@Nullable List<String> groups) {}
