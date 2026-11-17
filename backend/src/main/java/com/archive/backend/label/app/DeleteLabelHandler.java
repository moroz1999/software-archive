package com.archive.backend.label.app;

import com.archive.backend.author.domain.AuthorRepository;
import com.archive.backend.group.domain.GroupRepository;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelNotFoundException;
import com.archive.backend.label.domain.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deletes a label with its credits, memberships and locations: whoever added it, and administrators, delete it; a
 * label of somebody is first taken away from them, only by whoever may change them
 * (docs/domain/authors.md#editing-a-label).
 */
@Service
@RequiredArgsConstructor
public class DeleteLabelHandler {

    private final LabelRepository labels;
    private final AuthorRepository authors;
    private final GroupRepository groups;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(DeleteLabelCommand c) {
        Label label = labels.lock(c.id()).orElseThrow(LabelNotFoundException::new);
        label.delete(c.actor());
        LabelHandOver.release(c.id(), c.actor(), authors, groups, events);
        // Told while the credits are still there, so that whatever they reach is found.
        label.pullEvents().forEach(events::publishEvent);
        labels.delete(label);
    }
}
