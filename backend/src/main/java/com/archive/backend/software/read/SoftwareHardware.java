package com.archive.backend.software.read;

import java.util.List;

/**
 * An item of hardware of software or of a release, with the group it is shown under.
 */
public record SoftwareHardware(String code, String name, String group) {

    /**
     * The groups in the order they are shown (docs/domain/hardware.md).
     */
    public static final List<String> GROUPS = List.of("computers", "storage", "dos", "sound", "controls", "expansion");

    /**
     * The place of the item's group in the order of the groups.
     */
    public int groupOrder() {
        return GROUPS.indexOf(group);
    }
}
