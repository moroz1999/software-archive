package com.archive.backend.hardware.read;

import java.util.List;
import java.util.Map;

/** An item of hardware with its names by the codes of their languages, its group and its platforms by code. */
public record HardwareDetails(String code, Map<String, String> names, String group, List<String> platforms) {

    public HardwareDetails {
        names = Map.copyOf(names);
        platforms = List.copyOf(platforms);
    }
}
