package com.archive.backend.group.app;

import com.archive.backend.author.domain.LabelReplacedByAuthor;
import com.archive.backend.group.domain.LabelMemberships;
import com.archive.backend.group.domain.LabelReplacedByGroup;
import com.archive.backend.label.domain.LabelMergedIntoLabel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * The groups a label replaced by a person was a member of have the person as the member, and those of a label replaced
 * by a group have the group as a member group, and those of a label merged into another have that label as the
 * member; in the same transaction.
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

    @EventListener
    public void on(LabelMergedIntoLabel merged) {
        memberships.transfer(merged.label(), merged.into());
    }
}
