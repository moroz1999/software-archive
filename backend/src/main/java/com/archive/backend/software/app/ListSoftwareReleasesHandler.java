package com.archive.backend.software.app;

import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareReadRepository;
import com.archive.backend.software.read.SoftwareRelease;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The releases of software, each with the hardware it shows (docs/domain/hardware.md). */
@Service
@RequiredArgsConstructor
public class ListSoftwareReleasesHandler {

    private final SoftwareReadRepository reads;

    @Transactional(readOnly = true)
    public List<SoftwareRelease> handle(ListSoftwareReleasesQuery q) {
        SoftwareDetails software = reads.findById(q.id().value()).orElseThrow(SoftwareNotFoundException::new);
        return reads.findReleases(software.id()).stream()
                .map(release -> release.over(software.hardware()))
                .toList();
    }
}
