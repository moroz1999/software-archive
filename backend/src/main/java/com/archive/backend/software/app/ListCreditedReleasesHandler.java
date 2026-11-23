package com.archive.backend.software.app;

import com.archive.backend.software.read.CreditedRelease;
import com.archive.backend.software.read.SoftwareReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The releases people, groups and labels are credited on, as their pages list them (docs/domain/authors.md). */
@Service
@RequiredArgsConstructor
public class ListCreditedReleasesHandler {

    private final SoftwareReadRepository reads;

    @Transactional(readOnly = true)
    public List<CreditedRelease> handle(ListAuthorReleasesQuery q) {
        return reads.findReleasesCreditedOnPerson(q.author().value());
    }

    @Transactional(readOnly = true)
    public List<CreditedRelease> handle(ListGroupReleasesQuery q) {
        return reads.findReleasesCreditedOnGroup(q.group().value());
    }

    @Transactional(readOnly = true)
    public List<CreditedRelease> handle(ListLabelReleasesQuery q) {
        return reads.findReleasesCreditedOnLabel(q.label().value());
    }
}
