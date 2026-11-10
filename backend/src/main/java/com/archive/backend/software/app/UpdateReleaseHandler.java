package com.archive.backend.software.app;

import com.archive.backend.hardware.domain.HardwareSets;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateReleaseHandler {

    private final SoftwareRepository repository;
    private final HardwareSets hardwareSets;

    @Transactional
    public void handle(UpdateReleaseCommand c) {
        Software software = repository.findById(c.softwareId()).orElseThrow(SoftwareNotFoundException::new);
        software.editRelease(c.releaseId(), c.content(), hardwareSets.of(software.getPlatform(), c.hardware()));
        repository.save(software);
    }
}
