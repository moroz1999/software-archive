package com.archive.backend.label.app;

import com.archive.backend.author.domain.AuthorRepository;
import com.archive.backend.group.domain.GroupRepository;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelMergedIntoItselfException;
import com.archive.backend.label.domain.LabelOwnedException;
import com.archive.backend.label.domain.LabelOwnerUnknownException;
import com.archive.backend.label.domain.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Merges a label found out to be a duplicate of another label into it, taking it away from the group that has it: the
 * other label gets its locations, credits, memberships and remembered origin, and the label goes; administrators only
 * (docs/domain/authors.md#label).
 */
@Service
@RequiredArgsConstructor
public class MergeLabelIntoLabelHandler {

    private final AuthorRepository authors;
    private final GroupRepository groups;
    private final LabelRepository labels;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(MergeLabelIntoLabelCommand c) {
        LabelHandOver.requireHandOver(c.label(), c.actor(), authors, labels);
        if (c.into().equals(c.label())) {
            throw new LabelMergedIntoItselfException();
        }
        Label into = labels.lock(c.into()).orElseThrow(() -> new LabelOwnerUnknownException().onField("label"));
        LabelHandOver.release(c.label(), c.actor(), authors, groups, events);
        Label label = labels.lockOfNobody(c.label()).orElseThrow(LabelOwnedException::new);
        into.absorb(label);
        labels.save(into);
        // The answers move the label's credits, memberships and origin to the other label before the label goes.
        into.pullEvents().forEach(events::publishEvent);
        labels.delete(label);
    }
}
