package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.ExternalOrigin;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.ImportedEntryRepository;
import com.archive.backend.importer.domain.vektor.VektorListing;
import com.archive.backend.importer.domain.vektor.VektorProgram;
import com.archive.backend.importer.domain.vektor.VektorSections;
import com.archive.backend.importer.domain.vektor.VektorSite;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Runs the import of vektor-06c.ru: every program the archive has not taken yet is added in a transaction of its own,
 * so a program that fails is left out, told in the log, and the run goes on (docs/features/vektor-import.md).
 */
@Service
@RequiredArgsConstructor
public class ImportVektorHandler {

    private static final Logger log = LoggerFactory.getLogger(ImportVektorHandler.class);

    private final VektorSite site;
    private final ImportedEntryRepository entries;
    private final ImportVektorProgramHandler importProgram;

    public ImportSummary handle(ImportVektorCommand c) {
        int added = 0;
        int known = 0;
        int skipped = 0;
        int failed = 0;
        for (VektorListing listing : site.listings(c.section())) {
            if (entries.findByOrigin(ExternalOrigin.software(VektorSite.ARCHIVE, listing.id()))
                    .isPresent()) {
                known++;
                continue;
            }
            try {
                VektorProgram program = site.program(listing);
                if (!VektorSections.placementOf(program.section()).imported()) {
                    skipped++;
                    continue;
                }
                importProgram.handle(new ImportVektorProgramCommand(program));
                added++;
                log.info("Imported {} {}", listing.id(), program.title());
            } catch (RuntimeException e) {
                failed++;
                log.warn("Could not import {} {}", listing.id(), listing.url(), e);
            }
        }
        ImportSummary summary = new ImportSummary(added, known, skipped, failed);
        log.info("Import of {} done: {}", VektorSite.ARCHIVE, summary);
        return summary;
    }
}
