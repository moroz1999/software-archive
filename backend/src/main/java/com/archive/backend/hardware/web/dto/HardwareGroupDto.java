package com.archive.backend.hardware.web.dto;

import java.util.List;

/** A group of hardware in the list of the management, with its hardware by name. */
public record HardwareGroupDto(String code, String name, List<Entry> hardware) {

    public record Entry(String code, String name) {}
}
