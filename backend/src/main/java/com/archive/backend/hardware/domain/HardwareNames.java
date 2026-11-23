package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Map;

/** The names of an item of hardware or of a group of it, one per language; the Russian one is always there. */
@ValueObject
public record HardwareNames(Map<Language, HardwareName> values) {

    public HardwareNames {
        values = Map.copyOf(values);
        if (!values.containsKey(Language.RU)) {
            throw new InvalidValueException(
                    "hardware.name-required", "Hardware and its groups need a name in Russian.");
        }
    }
}
