package com.archive.backend.software.app;

import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteReleaseHandler {

    private final SoftwareRepository repository;

    @Transactional
    public void handle(DeleteReleaseCommand c) {
        Software software = repository.findById(c.softwareId()).orElseThrow(SoftwareNotFoundException::new);
        software.removeRelease(c.releaseId());
        repository.save(software);
    }
}
