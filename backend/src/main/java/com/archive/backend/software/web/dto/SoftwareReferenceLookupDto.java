package com.archive.backend.software.web.dto;

import java.util.List;

/** The people, groups and labels offered for a phrase. */
public record SoftwareReferenceLookupDto(List<SoftwareReferenceDto> items) {}
