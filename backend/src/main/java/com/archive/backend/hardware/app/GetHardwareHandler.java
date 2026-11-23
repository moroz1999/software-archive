package com.archive.backend.hardware.app;

import com.archive.backend.hardware.domain.HardwareNotFoundException;
import com.archive.backend.hardware.read.HardwareDetails;
import com.archive.backend.hardware.read.HardwareReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetHardwareHandler {

    private final HardwareReadRepository reads;

    @Transactional(readOnly = true)
    public HardwareDetails handle(GetHardwareQuery q) {
        return reads.findHardware(q.code().value()).orElseThrow(HardwareNotFoundException::new);
    }
}
