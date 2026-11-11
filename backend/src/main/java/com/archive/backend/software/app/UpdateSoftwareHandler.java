package com.archive.backend.software.app;

import com.archive.backend.category.domain.CategorySets;
import com.archive.backend.hardware.domain.HardwareSets;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateSoftwareHandler {

    private final SoftwareRepository repository;
    private final HardwareSets hardwareSets;
    private final CategorySets categorySets;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(UpdateSoftwareCommand c) {
        Software software = repository.findById(c.id()).orElseThrow(SoftwareNotFoundException::new);
        software.edit(
                c.title(),
                c.description(),
                c.releaseDate(),
                hardwareSets.of(c.platform(), c.hardware()),
                categorySets.of(c.categories()));
        repository.save(software);
        software.pullEvents().forEach(events::publishEvent);
    }
}
