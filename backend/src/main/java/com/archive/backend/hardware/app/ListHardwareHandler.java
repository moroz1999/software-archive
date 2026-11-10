package com.archive.backend.hardware.app;

import com.archive.backend.hardware.read.HardwareItem;
import com.archive.backend.hardware.read.HardwareReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The hardware of a platform, to choose from (docs/domain/hardware.md).
 */
@Service
@RequiredArgsConstructor
public class ListHardwareHandler {

    private final HardwareReadRepository reads;

    @Transactional(readOnly = true)
    public List<HardwareItem> handle(ListHardwareQuery q) {
        return reads.findByPlatform(q.platform().value());
    }
}
