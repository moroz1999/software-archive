package com.archive.backend.hardware.app;

import com.archive.backend.hardware.read.HardwareGroupItem;
import com.archive.backend.hardware.read.HardwareReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Every group of hardware in its order with its hardware: the reference book as the management keeps it. */
@Service
@RequiredArgsConstructor
public class ListHardwareGroupsHandler {

    private final HardwareReadRepository reads;

    @Transactional(readOnly = true)
    public List<HardwareGroupItem> handle(ListHardwareGroupsQuery q) {
        return reads.findGroups(q.language().code());
    }
}
