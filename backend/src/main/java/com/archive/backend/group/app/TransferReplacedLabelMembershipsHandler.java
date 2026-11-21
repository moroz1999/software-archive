package com.archive.backend.group.app;

import com.archive.backend.author.domain.LabelReplacedByAuthor;
import com.archive.backend.group.domain.LabelMemberships;
import com.archive.backend.group.domain.LabelReplacedByGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * The groups a label replaced by a person was a member of have the person as the member, and those of a label replaced
 * by a group have the group as a member group; in the same transaction.
 */
@Service
@RequiredArgsConstructor
public class TransferReplacedLabelMembershipsHandler {

    private final LabelMemberships memberships;

    @EventListener
    public void on(LabelReplacedByAuthor replaced) {
        memberships.transfer(replaced.label(), replaced.author());
    }

    @EventListener
    public void on(LabelReplacedByGroup replaced) {
        memberships.transfer(replaced.label(), replaced.group());
    }
}
