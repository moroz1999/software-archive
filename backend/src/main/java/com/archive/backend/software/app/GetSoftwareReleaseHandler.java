package com.archive.backend.software.app;

import com.archive.backend.software.domain.ReleaseNotFoundException;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.read.ReleaseDetails;
import com.archive.backend.software.read.SoftwareReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A release as it states itself, to edit it.
 */
@Service
@RequiredArgsConstructor
public class GetSoftwareReleaseHandler {

    private final SoftwareReadRepository reads;

    @Transactional(readOnly = true)
    public ReleaseDetails handle(GetSoftwareReleaseQuery q) {
        if (reads.findById(q.softwareId().value()).isEmpty()) {
            throw new SoftwareNotFoundException();
        }
        return reads.findRelease(q.softwareId().value(), q.releaseId().value())
                .orElseThrow(ReleaseNotFoundException::new);
    }
}
