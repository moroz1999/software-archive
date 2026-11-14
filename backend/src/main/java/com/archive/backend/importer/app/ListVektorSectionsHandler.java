package com.archive.backend.importer.app;

import com.archive.backend.importer.read.VektorSectionReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The sections of vektor-06c.ru an administrator can import one by one (docs/features/vektor-import.md). */
@Service
@RequiredArgsConstructor
public class ListVektorSectionsHandler {

    private final VektorSectionReadRepository reads;

    @Transactional(readOnly = true)
    public List<String> handle(ListVektorSectionsQuery q) {
        q.actor().administrator();
        return reads.findTaken();
    }
}
