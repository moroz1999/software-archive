package com.archive.backend.search.web.dto;

/** An item of hardware the catalog offers to filter by, named, with its group and the count of its software. */
public record HardwareFacetValueDto(String value, String name, String group, String groupName, long count) {}
