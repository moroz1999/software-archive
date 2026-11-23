package com.archive.backend.hardware.app;

import com.archive.backend.hardware.domain.Hardware;
import com.archive.backend.hardware.domain.HardwareNotFoundException;
import com.archive.backend.hardware.domain.HardwarePlacements;
import com.archive.backend.hardware.domain.HardwareRepository;
import com.archive.backend.hardware.domain.HardwareUses;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateHardwareHandler {

    private final HardwareRepository hardware;
    private final HardwarePlacements placements;
    private final HardwareUses uses;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(UpdateHardwareCommand c) {
        Hardware.requireKeptBy(c.actor());
        Hardware item = hardware.findByCode(c.code()).orElseThrow(HardwareNotFoundException::new);
        item.edit(c.names(), placements.of(c.group(), c.platforms()), uses);
        hardware.save(item);
        // The search answers another group within this transaction (docs/backend.md).
        item.pullEvents().forEach(events::publishEvent);
    }
}
