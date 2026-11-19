package com.archive.backend.software.web.dto;

import java.util.List;
import java.util.UUID;

public record ReleaseFileDto(
        UUID id, String name, boolean directory, boolean executable, List<ReleaseFileDto> children) {}
