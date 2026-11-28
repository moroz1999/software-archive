package com.archive.backend.software.app;

import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Adds links to software, each address of each type once: those it has stay as they are. */
@Service
@RequiredArgsConstructor
public class AddSoftwareLinksHandler {

    private final SoftwareRepository repository;

    @Transactional
    public void handle(AddSoftwareLinksCommand c) {
        if (c.links().isEmpty()) {
            return;
        }
        Software software = repository.findById(c.softwareId()).orElseThrow(SoftwareNotFoundException::new);
        software.requireChangeableBy(c.actor());
        c.links().forEach(software::addLink);
        repository.save(software);
    }
}
