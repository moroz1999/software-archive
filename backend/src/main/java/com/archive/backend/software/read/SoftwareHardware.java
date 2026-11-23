package com.archive.backend.software.read;

/**
 * An item of hardware of software or of a release, with the group it is shown under, the name of that group and its
 * place in the order of the groups (docs/domain/hardware.md).
 */
public record SoftwareHardware(String code, String name, String group, String groupName, int groupPosition) {}
