package com.archive.backend.hardware.app;

import com.archive.backend.hardware.domain.HardwareGroup;
import com.archive.backend.hardware.domain.HardwareGroupNotFoundException;
import com.archive.backend.hardware.domain.HardwareGroupRepository;
import com.archive.backend.hardware.domain.HardwareUses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteHardwareGroupHandler {

    private final HardwareGroupRepository groups;
    private final HardwareUses uses;

    @Transactional
    public void handle(DeleteHardwareGroupCommand c) {
        HardwareGroup.requireKeptBy(c.actor());
        HardwareGroup group = groups.findByCode(c.code()).orElseThrow(HardwareGroupNotFoundException::new);
        group.requireDeletable(uses);
        groups.delete(group);
    }
}
