package com.archive.backend.software.app;

import com.archive.backend.category.domain.CategorySets;
import com.archive.backend.hardware.domain.HardwareSets;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareCreditSets;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareRepository;
import com.archive.backend.user.domain.AddedBy;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateSoftwareHandler {

    private final SoftwareRepository repository;
    private final HardwareSets hardwareSets;
    private final CategorySets categorySets;
    private final SoftwareCreditSets creditSets;
    private final ApplicationEventPublisher events;

    @Transactional
    public SoftwareId handle(CreateSoftwareCommand c) {
        AddedBy addedBy = AddedBy.of(c.actor());
        SoftwareId id = new SoftwareId(UUID.randomUUID());
        Software software = Software.create(
                id,
                c.title(),
                c.description(),
                c.releaseDate(),
                hardwareSets.of(c.platform(), c.hardware()),
                categorySets.of(c.categories()),
                creditSets.of(c.credits()),
                addedBy);
        software.changePreservation(c.preservation());
        software.changeInstructions(c.instructions());
        repository.save(software);
        software.pullEvents().forEach(events::publishEvent);
        return software.getId();
    }
}
