package com.archive.backend.hardware.app;

import com.archive.backend.hardware.read.PlatformItem;
import com.archive.backend.hardware.read.PlatformReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListPlatformsHandler {

    private final PlatformReadRepository reads;

    @Transactional(readOnly = true)
    public List<PlatformItem> handle(ListPlatformsQuery q) {
        return reads.findAll();
    }
}
