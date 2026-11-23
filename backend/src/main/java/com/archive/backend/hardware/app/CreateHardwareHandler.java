package com.archive.backend.hardware.app;

import com.archive.backend.hardware.domain.Hardware;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.HardwarePlacements;
import com.archive.backend.hardware.domain.HardwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Adds an item to the hardware reference book (docs/domain/hardware.md#hardware). */
@Service
@RequiredArgsConstructor
public class CreateHardwareHandler {

    private final HardwareRepository hardware;
    private final HardwarePlacements placements;

    @Transactional
    public HardwareCode handle(CreateHardwareCommand c) {
        Hardware.requireKeptBy(c.actor());
        Hardware added = new Hardware(c.code(), c.names(), placements.of(c.group(), c.platforms()));
        hardware.add(added);
        return added.getCode();
    }
}
