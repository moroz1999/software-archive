package com.archive.backend.software.app;

import com.archive.backend.software.read.SoftwareReadRepository;
import com.archive.backend.software.read.SoftwareSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListLatestSoftwareHandler {

    private final SoftwareReadRepository reads;

    @Transactional(readOnly = true)
    public Page<SoftwareSummary> handle(ListLatestSoftwareQuery q) {
        return reads.findLatest(q.pageable());
    }
}
