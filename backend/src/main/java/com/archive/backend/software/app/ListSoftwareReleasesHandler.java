package com.archive.backend.software.app;

import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareReadRepository;
import com.archive.backend.software.read.SoftwareRelease;
import com.archive.backend.user.domain.AddedBy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The releases of software, each with the hardware it shows (docs/domain/hardware.md).
 */
@Service
@RequiredArgsConstructor
public class ListSoftwareReleasesHandler {

    private final SoftwareReadRepository reads;

    @Transactional(readOnly = true)
    public List<SoftwareRelease> handle(ListSoftwareReleasesQuery q) {
        SoftwareDetails software =
                reads.findById(q.id().value(), q.language().code()).orElseThrow(SoftwareNotFoundException::new);
        AddedBy softwareAddedBy = AddedBy.of(software.addedBy());
        return reads.findReleases(software.id(), q.language().code()).stream()
                .map(release -> release.over(software.hardware())
                        .withCanChange(AddedBy.of(release.addedBy()).mayBeChangedBy(q.actor(), softwareAddedBy)))
                .toList();
    }
}
