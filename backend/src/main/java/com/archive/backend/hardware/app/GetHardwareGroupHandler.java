package com.archive.backend.hardware.app;

import com.archive.backend.hardware.domain.HardwareGroupNotFoundException;
import com.archive.backend.hardware.read.HardwareGroupDetails;
import com.archive.backend.hardware.read.HardwareReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetHardwareGroupHandler {

    private final HardwareReadRepository reads;

    @Transactional(readOnly = true)
    public HardwareGroupDetails handle(GetHardwareGroupQuery q) {
        return reads.findGroup(q.code().value()).orElseThrow(HardwareGroupNotFoundException::new);
    }
}
