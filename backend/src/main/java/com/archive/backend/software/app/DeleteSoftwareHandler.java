package com.archive.backend.software.app;

import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteSoftwareHandler {

    private final SoftwareRepository repository;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(DeleteSoftwareCommand c) {
        Software software = repository.findById(c.id()).orElseThrow(SoftwareNotFoundException::new);
        software.delete();
        // Told before the deletion, while what the answers look up is still there (docs/backend.md).
        software.pullEvents().forEach(events::publishEvent);
        repository.delete(software);
    }
}
