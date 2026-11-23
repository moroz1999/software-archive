package com.archive.backend.software.app;

import com.archive.backend.author.domain.LabelAdoptedByAuthor;
import com.archive.backend.author.domain.LabelReplacedByAuthor;
import com.archive.backend.group.domain.LabelAdoptedByGroup;
import com.archive.backend.group.domain.LabelReplacedByGroup;
import com.archive.backend.label.domain.LabelMergedIntoLabel;
import com.archive.backend.software.domain.LabelCredits;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Software and releases credited under a label replaced by a person, a group or another label credit them instead; a
 * label taken by its owner takes in the owner's own credits on the same works. Both in the same transaction.
 */
@Service
@RequiredArgsConstructor
public class JoinLabelCreditsHandler {

    private final LabelCredits credits;

    @EventListener
    public void on(LabelReplacedByAuthor replaced) {
        credits.transfer(replaced.label(), replaced.author());
    }

    @EventListener
    public void on(LabelReplacedByGroup replaced) {
        credits.transfer(replaced.label(), replaced.group());
    }

    @EventListener
    public void on(LabelMergedIntoLabel merged) {
        credits.transfer(merged.label(), merged.into());
    }

    @EventListener
    public void on(LabelAdoptedByAuthor adopted) {
        credits.takeIn(adopted.label(), adopted.author());
    }

    @EventListener
    public void on(LabelAdoptedByGroup adopted) {
        credits.takeIn(adopted.label(), adopted.group());
    }
}
