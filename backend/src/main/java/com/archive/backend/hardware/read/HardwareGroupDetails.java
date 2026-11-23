package com.archive.backend.hardware.read;

import java.util.Map;

/** A group of hardware with its names by the codes of their languages, the Russian one always there. */
public record HardwareGroupDetails(String code, Map<String, String> names) {

    public HardwareGroupDetails {
        names = Map.copyOf(names);
    }
}
