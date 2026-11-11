package com.archive.backend.search.web.dto;

/** A value of a filter with the count of software it leaves. */
public record FacetValueDto<T>(T value, long count) {}
