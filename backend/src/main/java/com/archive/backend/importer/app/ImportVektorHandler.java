package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.run.FailedProgram;
import com.archive.backend.importer.domain.run.ImportOutcome;
import com.archive.backend.importer.domain.run.ImportRunState;
import com.archive.backend.importer.domain.vektor.VektorListing;
import com.archive.backend.importer.domain.vektor.VektorProgram;
import com.archive.backend.importer.domain.vektor.VektorSections;
import com.archive.backend.importer.domain.vektor.VektorSite;
import com.archive.backend.importer.domain.vektor.VektorSourcePrograms;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Runs the import of vektor-06c.ru: every program of the site goes to the one import in a transaction of its own, so
 * a program that fails is left out, told in the log, and the run goes on (docs/features/vektor-import.md).
 */
@Service
@RequiredArgsConstructor
public class ImportVektorHandler {

    private static final Logger log = LoggerFactory.getLogger(ImportVektorHandler.class);

    private final VektorSite site;
    private final ImportProgramHandler importProgram;
    private final RecordImportOutcomeHandler recordOutcome;
    private final EndImportRunHandler endRun;

    public ImportSummary handle(ImportVektorCommand c) {
        int added = 0;
        int known = 0;
        int skipped = 0;
        int failed = 0;
        for (VektorListing listing : site.listings(c.section())) {
            try {
                VektorProgram program = site.program(listing);
                if (!VektorSections.placementOf(program.section()).imported()) {
                    skipped++;
                    record(c, new ImportOutcome.Skipped());
                    continue;
                }
                ImportProgramResult result =
                        importProgram.handle(new ImportProgramCommand(c.run(), VektorSourcePrograms.of(program, site)));
                if (result == ImportProgramResult.ADDED) {
                    added++;
                    log.info("Imported {} {}", listing.id(), program.title());
                } else {
                    known++;
                }
            } catch (RuntimeException e) {
                failed++;
                log.warn("Could not import {} {}", listing.id(), listing.url(), e);
                record(
                        c,
                        new ImportOutcome.Failed(
                                new FailedProgram(listing.id(), new ExternalPage(listing.url()), reasonOf(e))));
            }
        }
        ImportSummary summary = new ImportSummary(added, known, skipped, failed);
        endRun.handle(new EndImportRunCommand(c.run(), ImportRunState.DONE));
        log.info("Import of {} done: {}", VektorSite.ARCHIVE, summary);
        return summary;
    }

    private void record(ImportVektorCommand c, ImportOutcome outcome) {
        recordOutcome.handle(new RecordImportOutcomeCommand(c.run(), outcome));
    }

    /** What went wrong, as the failure tells it: its message, or its kind when it has none. */
    private static String reasonOf(RuntimeException e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }
}
