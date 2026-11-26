package com.archive.backend.software.web.dto;

import java.util.List;
import java.util.UUID;

public record SoftwareLinkDto(UUID id, String title) {

    public record Lookup(List<SoftwareLinkDto> items) {}
}
