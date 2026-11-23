package com.archive.backend.hardware.app;

import com.archive.backend.hardware.domain.HardwareGroup;
import com.archive.backend.hardware.domain.HardwareGroupCode;
import com.archive.backend.hardware.domain.HardwareGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Adds a group of hardware after the others (docs/domain/hardware.md#hardware). */
@Service
@RequiredArgsConstructor
public class CreateHardwareGroupHandler {

    private final HardwareGroupRepository groups;

    @Transactional
    public HardwareGroupCode handle(CreateHardwareGroupCommand c) {
        HardwareGroup.requireKeptBy(c.actor());
        HardwareGroup added = new HardwareGroup(c.code(), c.names());
        groups.add(added);
        return added.getCode();
    }
}
