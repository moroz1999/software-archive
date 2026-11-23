package com.archive.backend.search.read;

/**
 * An item of hardware the catalog is narrowed by, named in the language asked for, with its group named the same way and
 * the count of its software.
 */
public record HardwareCount(String code, String name, String group, String groupName, long count) {}
