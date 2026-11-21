package com.archive.backend.label.app;

import com.archive.backend.author.domain.AuthorRepository;
import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupRepository;
import com.archive.backend.group.domain.GroupTypeSet;
import com.archive.backend.group.domain.GroupTypes;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelRepository;
import com.archive.backend.user.domain.AddedBy;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Replaces a label with a group of the same name, taking it away from whoever had it, and transfers its relations; the
 * group is added by the user who makes it (docs/domain/authors.md#label).
 */
@Service
@RequiredArgsConstructor
public class MakeGroupOfLabelHandler {

    private final AuthorRepository authors;
    private final GroupRepository groups;
    private final GroupTypes groupTypes;
    private final LabelRepository labels;
    private final ApplicationEventPublisher events;

    @Transactional
    public GroupId handle(MakeGroupOfLabelCommand c) {
        AddedBy addedBy = AddedBy.of(c.actor());
        GroupTypeSet types = groupTypes.of(c.types());
        Label label = LabelHandOver.takeAway(c.label(), c.actor(), authors, groups, labels, events);
        Group group = Group.replacing(new GroupId(UUID.randomUUID()), label, types, addedBy);
        groups.save(group);
        // The answers move the label's credits, memberships and origin to the group before the label goes.
        group.pullEvents().forEach(events::publishEvent);
        labels.delete(label);
        return group.getId();
    }
}
