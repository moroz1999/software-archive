package com.archive.backend.label.app;

import com.archive.backend.label.domain.LabelNotFoundException;
import com.archive.backend.label.read.LabelDetails;
import com.archive.backend.label.read.LabelReadRepository;
import com.archive.backend.user.domain.AddedBy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The page of a label. Labels of nobody were taken from imports, entries of nobody: only administrators attach them
 * to existing owners. Any user may make a new owner of a label, taking it away from an owner only when they may change
 * that owner (docs/domain/users.md).
 */
@Service
@RequiredArgsConstructor
public class GetLabelHandler {

    private final LabelReadRepository reads;

    @Transactional(readOnly = true)
    public LabelDetails handle(GetLabelQuery q) {
        LabelDetails label = reads.findById(q.id().value()).orElseThrow(LabelNotFoundException::new);
        LabelDetails.Owner owner = label.owner();
        boolean canMakeOwner = q.actor().mayAddEntries()
                && (owner == null || AddedBy.of(owner.addedBy()).mayBeChangedBy(q.actor()));
        return label.withRights(owner == null && q.actor().isAdministrator(), canMakeOwner);
    }
}
