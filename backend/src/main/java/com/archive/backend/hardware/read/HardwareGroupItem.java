package com.archive.backend.hardware.read;

import java.util.List;

/** A group of hardware as the management lists it: named, with its hardware by name. */
public record HardwareGroupItem(String code, String name, List<Entry> hardware) {

    public HardwareGroupItem {
        hardware = List.copyOf(hardware);
    }

    /** An item of hardware in the group. */
    public record Entry(String code, String name) {}
}
