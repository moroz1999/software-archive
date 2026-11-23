package com.archive.backend.hardware.app;

import com.archive.backend.hardware.domain.Hardware;
import com.archive.backend.hardware.domain.HardwareNotFoundException;
import com.archive.backend.hardware.domain.HardwareRepository;
import com.archive.backend.hardware.domain.HardwareUses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteHardwareHandler {

    private final HardwareRepository hardware;
    private final HardwareUses uses;

    @Transactional
    public void handle(DeleteHardwareCommand c) {
        Hardware.requireKeptBy(c.actor());
        Hardware item = hardware.findByCode(c.code()).orElseThrow(HardwareNotFoundException::new);
        item.requireDeletable(uses);
        hardware.delete(item);
    }
}
