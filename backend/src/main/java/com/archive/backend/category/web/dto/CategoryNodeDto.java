package com.archive.backend.category.web.dto;

import java.util.List;
import java.util.UUID;

public record CategoryNodeDto(UUID id, String title, List<CategoryNodeDto> children) {}
