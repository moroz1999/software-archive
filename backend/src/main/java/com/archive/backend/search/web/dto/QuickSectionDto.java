package com.archive.backend.search.web.dto;

import java.util.List;

public record QuickSectionDto<T>(List<T> items, long total) {}
