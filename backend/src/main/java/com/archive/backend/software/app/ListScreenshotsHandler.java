package com.archive.backend.software.app;

import com.archive.backend.software.domain.ReleaseNotFoundException;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.read.SoftwareReadRepository;
import com.archive.backend.software.read.SoftwareScreenshot;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListScreenshotsHandler {

    private final SoftwareReadRepository reads;

    @Transactional(readOnly = true)
    public List<SoftwareScreenshot> handle(ListScreenshotsQuery q) {
        UUID softwareId = q.softwareId().value();
        if (!reads.exists(softwareId)) {
            throw new SoftwareNotFoundException();
        }
        if (q.releaseId() == null) {
            return reads.findScreenshots(softwareId);
        }
        UUID releaseId = q.releaseId().value();
        if (!reads.releaseExists(softwareId, releaseId)) {
            throw new ReleaseNotFoundException();
        }
        return reads.findReleaseScreenshots(releaseId);
    }
}
