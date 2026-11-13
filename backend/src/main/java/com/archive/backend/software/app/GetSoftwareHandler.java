package com.archive.backend.software.app;

import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareReadRepository;
import com.archive.backend.user.domain.AddedBy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSoftwareHandler {

    private final SoftwareReadRepository reads;

    @Transactional(readOnly = true)
    public SoftwareDetails handle(GetSoftwareQuery q) {
        SoftwareDetails software = reads.findById(q.id().value()).orElseThrow(SoftwareNotFoundException::new);
        return software.withCanChange(AddedBy.of(software.addedBy()).mayBeChangedBy(q.actor()));
    }
}
