package com.archive.backend.software.app;

import com.archive.backend.software.read.CreditedSoftware;
import com.archive.backend.software.read.SoftwareReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The software people and groups are credited on, as their pages list it (docs/domain/authors.md). */
@Service
@RequiredArgsConstructor
public class ListCreditedSoftwareHandler {

    private final SoftwareReadRepository reads;

    @Transactional(readOnly = true)
    public List<CreditedSoftware> handle(ListAuthorSoftwareQuery q) {
        return reads.findCreditedOnPerson(q.author().value());
    }

    @Transactional(readOnly = true)
    public List<CreditedSoftware> handle(ListGroupSoftwareQuery q) {
        return reads.findCreditedOnGroup(q.group().value());
    }

    @Transactional(readOnly = true)
    public List<CreditedSoftware> handle(ListLabelSoftwareQuery q) {
        return reads.findCreditedOnLabel(q.label().value());
    }
}
