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
 * Makes a label a label of a group, taking it away from another group that has it; it keeps its credits and
 * memberships; administrators only (docs/domain/authors.md#label).
 */
@Service
@RequiredArgsConstructor
public class GiveLabelToGroupHandler {

    private final AuthorRepository authors;
    private final GroupRepository groups;
    private final LabelRepository labels;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(GiveLabelToGroupCommand c) {
        LabelHandOver.requireGivableToGroup(c.label(), c.group(), c.actor(), authors, groups, labels);
        boolean already = groups.findByLabel(c.label())
                .filter(owner -> owner.getId().equals(c.group()))
                .isPresent();
        if (already) {
            return;
        }
        LabelHandOver.release(c.label(), c.actor(), authors, groups, events);
        Label label = labels.lockOfNobody(c.label()).orElseThrow(LabelOwnedException::new);
        Group group = groups.findById(c.group()).orElseThrow(LabelOwnerUnknownException::new);
        group.adopt(label);
        groups.save(group);
        group.pullEvents().forEach(events::publishEvent);
    }
}
