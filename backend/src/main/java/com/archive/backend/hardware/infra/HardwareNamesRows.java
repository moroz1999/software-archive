package com.archive.backend.hardware.infra;

import com.archive.backend.hardware.domain.HardwareName;
import com.archive.backend.hardware.domain.HardwareNames;
import com.archive.backend.shared.domain.Language;
import java.util.HashMap;
import java.util.Map;

/** The names of hardware and of its groups as their rows keep them. */
final class HardwareNamesRows {

    private HardwareNamesRows() {}

    static HardwareNames toNames(Map<String, String> rows) {
        Map<Language, HardwareName> names = new HashMap<>();
        rows.forEach((code, name) -> names.put(Language.known(code), new HardwareName(name)));
        return new HardwareNames(names);
    }

    static Map<String, String> toRows(HardwareNames names) {
        Map<String, String> rows = new HashMap<>();
        names.values().forEach((language, name) -> rows.put(language.code(), name.value()));
        return rows;
    }
}
