package com.archive.backend.label.app;

import com.archive.backend.label.domain.LabelNotFoundException;
import com.archive.backend.label.read.LabelDetails;
import com.archive.backend.label.read.LabelReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The page of a label. Labels of nobody were taken from imports, entries of nobody: only administrators attach them
 * to their owners (docs/domain/users.md).
 */
@Service
@RequiredArgsConstructor
public class GetLabelHandler {

    private final LabelReadRepository reads;

    @Transactional(readOnly = true)
    public LabelDetails handle(GetLabelQuery q) {
        LabelDetails label = reads.findById(q.id().value()).orElseThrow(LabelNotFoundException::new);
        return label.withCanChange(label.owner() == null && q.actor().isAdministrator());
    }
}
