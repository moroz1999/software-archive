package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.Objects;

/** Hardware put in another group: the software that has it is now filtered by it in that group. */
public record HardwareRegrouped(HardwareCode hardware) implements DomainEvent {

    public HardwareRegrouped {
        Objects.requireNonNull(hardware, "hardware");
    }
}
