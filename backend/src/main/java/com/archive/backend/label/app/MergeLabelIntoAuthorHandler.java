package com.archive.backend.label.app;

import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorRepository;
import com.archive.backend.group.domain.GroupRepository;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelOwnedException;
import com.archive.backend.label.domain.LabelOwnerUnknownException;
import com.archive.backend.label.domain.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Merges a label found out to be a duplicate of a person into them, taking it away from the group that has it: the
 * person gets its locations, credits, memberships and remembered origin, and the label goes; administrators only
 * (docs/domain/authors.md#label).
 */
@Service
@RequiredArgsConstructor
public class MergeLabelIntoAuthorHandler {

    private final AuthorRepository authors;
    private final GroupRepository groups;
    private final LabelRepository labels;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(MergeLabelIntoAuthorCommand c) {
        LabelHandOver.requireHandOver(c.label(), c.actor(), authors, labels);
        if (authors.findById(c.author()).isEmpty()) {
            throw new LabelOwnerUnknownException().onField("author");
        }
        LabelHandOver.release(c.label(), c.actor(), authors, groups, events);
        Label label = labels.lockOfNobody(c.label()).orElseThrow(LabelOwnedException::new);
        Author author = authors.findById(c.author()).orElseThrow(LabelOwnerUnknownException::new);
        author.absorb(label);
        authors.save(author);
        // The answers move the label's credits, memberships and origin to the person before the label goes.
        author.pullEvents().forEach(events::publishEvent);
        labels.delete(label);
    }
}
