package com.archive.backend.software.app;

import com.archive.backend.hardware.domain.HardwareSets;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwareRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateReleaseHandler {

    private final SoftwareRepository repository;
    private final HardwareSets hardwareSets;

    @Transactional
    public ReleaseId handle(CreateReleaseCommand c) {
        Software software = repository.findById(c.softwareId()).orElseThrow(SoftwareNotFoundException::new);
        ReleaseId id = new ReleaseId(UUID.randomUUID());
        software.addRelease(id, c.content(), hardwareSets.of(software.getPlatform(), c.hardware()));
        repository.save(software);
        return id;
    }
}
