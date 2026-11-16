package com.archive.backend.group.app;

import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupRepository;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelNotFoundException;
import com.archive.backend.label.domain.LabelOwnedException;
import com.archive.backend.label.domain.LabelOwnerUnknownException;
import com.archive.backend.label.domain.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Attaches a label of nobody to the group found out to be its owner (docs/domain/authors.md#label). */
@Service
@RequiredArgsConstructor
public class AttachLabelToGroupHandler {

    private final GroupRepository groups;
    private final LabelRepository labels;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(AttachLabelToGroupCommand c) {
        Label label = labels.lockOfNobody(c.label())
                .orElseThrow(() -> labels.exists(c.label()) ? new LabelOwnedException() : new LabelNotFoundException());
        Group group = groups.findById(c.group()).orElseThrow(LabelOwnerUnknownException::new);
        group.requireChangeableBy(c.actor());
        group.adopt(label);
        groups.save(group);
        group.pullEvents().forEach(events::publishEvent);
    }
}
