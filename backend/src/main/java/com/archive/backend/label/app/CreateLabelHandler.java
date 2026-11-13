package com.archive.backend.label.app;

import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adds a label of nobody.
 */
@Service
@RequiredArgsConstructor
public class CreateLabelHandler {

    private final LabelRepository repository;

    @Transactional
    public LabelId handle(CreateLabelCommand c) {
        LabelId id = new LabelId(UUID.randomUUID());
        repository.save(Label.create(id, c.name(), c.actor()));
        return id;
    }
}
