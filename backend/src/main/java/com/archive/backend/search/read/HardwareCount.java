package com.archive.backend.search.read;

/** An item of hardware the catalog is narrowed by, named in the language asked for, with the count of its software. */
public record HardwareCount(String code, String name, String group, long count) {}
