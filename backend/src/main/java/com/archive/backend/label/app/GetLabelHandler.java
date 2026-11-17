package com.archive.backend.label.app;

import com.archive.backend.label.domain.LabelNotFoundException;
import com.archive.backend.label.read.LabelDetails;
import com.archive.backend.label.read.LabelReadRepository;
import com.archive.backend.user.domain.AddedBy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The page of a label, with what the reader may do with it: whoever added the label, and administrators, edit it and
 * make a new owner of it, taking it away from an owner only when they may change that owner; only administrators say
 * whose it is (docs/domain/users.md).
 */
@Service
@RequiredArgsConstructor
public class GetLabelHandler {

    private final LabelReadRepository reads;

    @Transactional(readOnly = true)
    public LabelDetails handle(GetLabelQuery q) {
        LabelDetails label = reads.findById(q.id().value()).orElseThrow(LabelNotFoundException::new);
        LabelDetails.Owner owner = label.owner();
        boolean administrator = q.actor().isAdministrator();
        boolean canEdit = AddedBy.of(label.addedBy()).mayBeChangedBy(q.actor());
        boolean canMakeOwner =
                canEdit && (owner == null || AddedBy.of(owner.addedBy()).mayBeChangedBy(q.actor()));
        return label.withRights(
                new LabelDetails.Rights(owner == null && administrator, canMakeOwner, canEdit, administrator));
    }
}
