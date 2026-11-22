package com.archive.backend.label.app;

import com.archive.backend.author.domain.AuthorRepository;
import com.archive.backend.group.domain.Group;
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
 * Merges a label found out to be a duplicate of a group into it, taking it away from the group that has it: the group
 * gets its locations, credits, memberships and remembered origin, and the label goes; administrators only
 * (docs/domain/authors.md#label).
 */
@Service
@RequiredArgsConstructor
public class MergeLabelIntoGroupHandler {

    private final AuthorRepository authors;
    private final GroupRepository groups;
    private final LabelRepository labels;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(MergeLabelIntoGroupCommand c) {
        LabelHandOver.requireGivableToGroup(c.label(), c.group(), c.actor(), authors, groups, labels);
        LabelHandOver.release(c.label(), c.actor(), authors, groups, events);
        Label label = labels.lockOfNobody(c.label()).orElseThrow(LabelOwnedException::new);
        Group group = groups.findById(c.group()).orElseThrow(LabelOwnerUnknownException::new);
        group.absorb(label);
        groups.save(group);
        // The answers move the label's credits, memberships and origin to the group before the label goes.
        group.pullEvents().forEach(events::publishEvent);
        labels.delete(label);
    }
}
