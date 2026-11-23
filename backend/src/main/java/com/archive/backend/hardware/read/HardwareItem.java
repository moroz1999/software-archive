package com.archive.backend.hardware.read;

/** An item of the hardware reference book, with the group it is shown under and the name of that group. */
public record HardwareItem(String code, String name, String group, String groupName) {}
