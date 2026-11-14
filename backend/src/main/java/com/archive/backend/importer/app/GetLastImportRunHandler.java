package com.archive.backend.importer.app;

import com.archive.backend.importer.read.ImportRunReadRepository;
import com.archive.backend.importer.read.ImportRunReport;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** What the import started last did, for an administrator; empty before the first one. */
@Service
@RequiredArgsConstructor
public class GetLastImportRunHandler {

    private final ImportRunReadRepository reads;

    @Transactional(readOnly = true)
    public Optional<ImportRunReport> handle(GetLastImportRunQuery q) {
        q.actor().administrator();
        return reads.findLatest();
    }
}
