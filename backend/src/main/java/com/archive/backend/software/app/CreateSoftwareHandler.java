package com.archive.backend.software.app;

import com.archive.backend.category.domain.CategorySets;
import com.archive.backend.hardware.domain.HardwareSets;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateSoftwareHandler {

    private final SoftwareRepository repository;
    private final HardwareSets hardwareSets;
    private final CategorySets categorySets;

    @Transactional
    public SoftwareId handle(CreateSoftwareCommand c) {
        SoftwareId id = new SoftwareId(UUID.randomUUID());
        repository.save(Software.create(
                id,
                c.title(),
                c.description(),
                c.releaseDate(),
                hardwareSets.of(c.platform(), c.hardware()),
                categorySets.of(c.categories())));
        return id;
    }
}
