package com.archive.backend.search.read;

/** A value of a filter known by its code, a platform or an item of hardware, with the count of software it has. */
public record CodeCount(String code, long count) {}
