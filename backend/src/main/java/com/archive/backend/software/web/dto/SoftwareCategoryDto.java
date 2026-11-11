package com.archive.backend.software.web.dto;

import java.util.List;
import java.util.UUID;

public record SoftwareCategoryDto(UUID id, String title, List<SoftwareCategoryStepDto> branch) {}
