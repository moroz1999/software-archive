package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Set;

/** The platforms an item of hardware belongs to: at least one (docs/domain/hardware.md#hardware). */
@ValueObject
public record HardwarePlatforms(Set<PlatformCode> values) {

    public HardwarePlatforms {
        values = Set.copyOf(values);
        if (values.isEmpty()) {
            throw new InvalidValueException("hardware.platform-required", "Hardware belongs to a platform at least.");
        }
    }
}
