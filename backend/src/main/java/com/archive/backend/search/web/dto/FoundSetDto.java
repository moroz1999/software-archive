package com.archive.backend.search.web.dto;

import java.util.List;

/** The matches of one kind on a page of the full search: {@code type} is the code of the kind. */
public record FoundSetDto(String type, long total, List<?> items) {}
