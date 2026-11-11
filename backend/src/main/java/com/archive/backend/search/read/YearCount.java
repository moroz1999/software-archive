package com.archive.backend.search.read;

/** A year of the catalog's filter with the count of software it leaves. */
public record YearCount(int year, long count) {}
