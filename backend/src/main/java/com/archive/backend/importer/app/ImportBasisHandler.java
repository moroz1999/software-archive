package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.basis.BasisListing;
import com.archive.backend.importer.domain.basis.BasisProgram;
import com.archive.backend.importer.domain.basis.BasisSite;
import com.archive.backend.importer.domain.basis.BasisSourcePrograms;
import com.archive.backend.importer.domain.run.FailedProgram;
import com.archive.backend.importer.domain.run.ImportLogEntry;
import com.archive.backend.importer.domain.run.ImportOutcome;
import com.archive.backend.importer.domain.run.ImportProgress;
import com.archive.backend.importer.domain.run.ImportRunState;
import com.archive.backend.importer.domain.run.SkipReason;
import com.archive.backend.importer.domain.run.SkippedProgram;
import com.archive.backend.importer.domain.source.SourceProgram;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Runs the import of the card index "Базис": every card goes to the one import in a transaction of its own, so a card
 * that fails is left out, told in the log, and the run goes on (docs/features/basis-import.md).
 */
@Service
@RequiredArgsConstructor
public class ImportBasisHandler {

    private static final Logger log = LoggerFactory.getLogger(ImportBasisHandler.class);

    private final BasisSite site;
    private final ImportProgramHandler importProgram;
    private final RecordImportOutcomeHandler recordOutcome;
    private final RecordImportProgressHandler progress;
    private final RecordImportLogHandler logged;
    private final EndImportRunHandler endRun;

    public ImportSummary handle(ImportBasisCommand c) {
        int added = 0;
        int known = 0;
        int skipped = 0;
        int failed = 0;
        List<BasisListing> listings = site.listings(c.category());
        logged.handle(new RecordImportLogCommand(
                c.run(),
                List.of(new ImportLogEntry.ListRead(
                        new ExternalPage(BasisSite.listUrl(c.category())), 1, 1, listings.size()))));
        int position = 0;
        for (BasisListing listing : listings) {
            position++;
            progress.handle(new RecordImportProgressCommand(
                    c.run(), new ImportProgress(position, listings.size(), new ExternalPage(listing.url()))));
            try {
                BasisProgram program = site.program(listing);
                Optional<String> leftOut = BasisSourcePrograms.leftOutBy(program);
                if (leftOut.isPresent()) {
                    skipped++;
                    record(
                            c,
                            new ImportOutcome.Skipped(new SkippedProgram(
                                    listing.id(),
                                    new ExternalPage(listing.url()),
                                    program.title(),
                                    SkipReason.CATEGORY,
                                    leftOut.get())));
                    continue;
                }
                SourceProgram source = BasisSourcePrograms.of(program, site);
                logged.handle(new RecordImportLogCommand(c.run(), ImportLogEntry.downloadsOf(source)));
                ImportProgramResult result = importProgram.handle(new ImportProgramCommand(c.run(), source));
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
        log.info("Import of {} done: {}", BasisSite.ARCHIVE, summary);
        return summary;
    }

    private void record(ImportBasisCommand c, ImportOutcome outcome) {
        recordOutcome.handle(new RecordImportOutcomeCommand(c.run(), outcome));
    }

    /** What went wrong, as the failure tells it: its message, or its kind when it has none. */
    private static String reasonOf(RuntimeException e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }
}
