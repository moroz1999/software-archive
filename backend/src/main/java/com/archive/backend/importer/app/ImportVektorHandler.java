package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.run.FailedProgram;
import com.archive.backend.importer.domain.run.ImportLogEntry;
import com.archive.backend.importer.domain.run.ImportOutcome;
import com.archive.backend.importer.domain.run.ImportProgress;
import com.archive.backend.importer.domain.run.ImportRunState;
import com.archive.backend.importer.domain.run.SkipReason;
import com.archive.backend.importer.domain.run.SkippedProgram;
import com.archive.backend.importer.domain.source.SourceProgram;
import com.archive.backend.importer.domain.vektor.VektorListPage;
import com.archive.backend.importer.domain.vektor.VektorListing;
import com.archive.backend.importer.domain.vektor.VektorProgram;
import com.archive.backend.importer.domain.vektor.VektorSections;
import com.archive.backend.importer.domain.vektor.VektorSite;
import com.archive.backend.importer.domain.vektor.VektorSourcePrograms;
import java.util.List;
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
    private final RecordImportProgressHandler progress;
    private final RecordImportLogHandler logged;
    private final EndImportRunHandler endRun;

    public ImportSummary handle(ImportVektorCommand c) {
        int added = 0;
        int known = 0;
        int skipped = 0;
        int failed = 0;
        int position = 0;
        VektorListPage first = site.firstPage(c.section());
        int pages = 1 + first.otherPages().size();
        for (int number = 1; number <= pages; number++) {
            VektorListPage page =
                    number == 1 ? first : site.page(first.otherPages().get(number - 2));
            logged.handle(new RecordImportLogCommand(
                    c.run(),
                    List.of(new ImportLogEntry.ListRead(
                            new ExternalPage(page.url()),
                            number,
                            pages,
                            page.listings().size()))));
            for (VektorListing listing : page.listings()) {
                // The site lists its programs page by page: how many there are is not known before the last one.
                position++;
                switch (take(c, listing, position)) {
                    case ADDED -> added++;
                    case KNOWN -> known++;
                    case SKIPPED -> skipped++;
                    case FAILED -> failed++;
                }
            }
        }
        ImportSummary summary = new ImportSummary(added, known, skipped, failed);
        endRun.handle(new EndImportRunCommand(c.run(), ImportRunState.DONE));
        log.info("Import of {} done: {}", VektorSite.ARCHIVE, summary);
        return summary;
    }

    /** What one program came to. */
    private enum Taken {
        ADDED,
        KNOWN,
        SKIPPED,
        FAILED
    }

    private Taken take(ImportVektorCommand c, VektorListing listing, int position) {
        progress.handle(new RecordImportProgressCommand(
                c.run(), new ImportProgress(position, null, new ExternalPage(listing.url()))));
        try {
            VektorProgram program = site.program(listing);
            if (!VektorSections.placementOf(program.section()).imported()) {
                record(
                        c,
                        new ImportOutcome.Skipped(new SkippedProgram(
                                listing.id(),
                                new ExternalPage(listing.url()),
                                program.title(),
                                SkipReason.SECTION,
                                program.section())));
                return Taken.SKIPPED;
            }
            SourceProgram source = VektorSourcePrograms.of(program, site);
            logged.handle(new RecordImportLogCommand(c.run(), ImportLogEntry.downloadsOf(source)));
            if (importProgram.handle(new ImportProgramCommand(c.run(), source)) == ImportProgramResult.ADDED) {
                log.info("Imported {} {}", listing.id(), program.title());
                return Taken.ADDED;
            }
            return Taken.KNOWN;
        } catch (RuntimeException e) {
            log.warn("Could not import {} {}", listing.id(), listing.url(), e);
            record(
                    c,
                    new ImportOutcome.Failed(
                            new FailedProgram(listing.id(), new ExternalPage(listing.url()), reasonOf(e))));
            return Taken.FAILED;
        }
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
