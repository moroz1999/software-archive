package com.archive.backend.hardware.app;

import com.archive.backend.hardware.domain.HardwareGroup;
import com.archive.backend.hardware.domain.HardwareGroupNotFoundException;
import com.archive.backend.hardware.domain.HardwareGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateHardwareGroupHandler {

    private final HardwareGroupRepository groups;

    @Transactional
    public void handle(UpdateHardwareGroupCommand c) {
        HardwareGroup.requireKeptBy(c.actor());
        HardwareGroup group = groups.findByCode(c.code()).orElseThrow(HardwareGroupNotFoundException::new);
        group.rename(c.names());
        groups.save(group);
    }
}
