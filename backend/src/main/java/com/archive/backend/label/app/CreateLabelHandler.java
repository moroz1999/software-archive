package com.archive.backend.label.app;

import com.archive.backend.geo.domain.Places;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adds a label of nobody, by hand or by an import, added by whoever gives it (docs/domain/authors.md#label).
 */
@Service
@RequiredArgsConstructor
public class CreateLabelHandler {

    private final LabelRepository repository;
    private final Places places;
    private final ApplicationEventPublisher events;

    @Transactional
    public LabelId handle(CreateLabelCommand c) {
        LabelId id = new LabelId(UUID.randomUUID());
        Label label = Label.create(id, c.name(), places.locations(c.locations()), c.actor());
        repository.save(label);
        label.pullEvents().forEach(events::publishEvent);
        return id;
    }
}
