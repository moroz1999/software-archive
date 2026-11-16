package com.archive.backend.label.app;

import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adds a label of nobody.
 */
@Service
@RequiredArgsConstructor
public class CreateLabelHandler {

    private final LabelRepository repository;
    private final ApplicationEventPublisher events;

    @Transactional
    public LabelId handle(CreateLabelCommand c) {
        LabelId id = new LabelId(UUID.randomUUID());
        Label label = Label.create(id, c.name(), c.actor());
        repository.save(label);
        label.pullEvents().forEach(events::publishEvent);
        return id;
    }
}
