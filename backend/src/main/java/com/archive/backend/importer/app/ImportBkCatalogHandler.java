package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.bkcatalog.BkCatalogEntry;
import com.archive.backend.importer.domain.bkcatalog.BkCatalogSection;
import com.archive.backend.importer.domain.bkcatalog.BkCatalogSite;
import com.archive.backend.importer.domain.bkcatalog.BkCatalogSourcePrograms;
import com.archive.backend.importer.domain.run.FailedProgram;
import com.archive.backend.importer.domain.run.ImportLogEntry;
import com.archive.backend.importer.domain.run.ImportOutcome;
import com.archive.backend.importer.domain.run.ImportProgress;
import com.archive.backend.importer.domain.run.ImportRunState;
import com.archive.backend.importer.domain.run.SkipReason;
import com.archive.backend.importer.domain.run.SkippedProgram;
import com.archive.backend.importer.domain.source.SourceProgram;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Runs the import of bk-catalog: reads the list of every section chosen, then takes every entry to the one import in a
 * transaction of its own, so an entry that fails is told in the log and the run goes on
 * (docs/features/bk-catalog-import.md).
 */
@Service
@RequiredArgsConstructor
public class ImportBkCatalogHandler {

    private static final Logger log = LoggerFactory.getLogger(ImportBkCatalogHandler.class);

    private final BkCatalogSite site;
    private final ImportProgramHandler importProgram;
    private final RecordImportOutcomeHandler recordOutcome;
    private final RecordImportProgressHandler progress;
    private final RecordImportLogHandler logged;
    private final EndImportRunHandler endRun;

    public ImportSummary handle(ImportBkCatalogCommand c) {
        BkCatalogSection chosen = c.section();
        List<BkCatalogSection> sections = chosen == null ? List.of(BkCatalogSection.values()) : List.of(chosen);
        List<BkCatalogEntry> entries = new ArrayList<>();
        int number = 0;
        for (BkCatalogSection section : sections) {
            number++;
            List<BkCatalogEntry> listed = site.entries(section);
            logged.handle(new RecordImportLogCommand(
                    c.run(),
                    List.of(new ImportLogEntry.ListRead(
                            new ExternalPage(BkCatalogSite.listUrl(section)),
                            number,
                            sections.size(),
                            listed.size()))));
            entries.addAll(listed);
        }
        int added = 0;
        int known = 0;
        int skipped = 0;
        int failed = 0;
        int position = 0;
        for (BkCatalogEntry entry : entries) {
            position++;
            String id = BkCatalogSourcePrograms.idOf(entry);
            ExternalPage page = BkCatalogSourcePrograms.pageOf(entry);
            progress.handle(
                    new RecordImportProgressCommand(c.run(), new ImportProgress(position, entries.size(), page)));
            try {
                Optional<String> leftOut = BkCatalogSourcePrograms.leftOutBy(entry);
                if (leftOut.isPresent()) {
                    skipped++;
                    record(
                            c,
                            new ImportOutcome.Skipped(new SkippedProgram(
                                    id, page, entry.get("Название"), SkipReason.PLATFORM, leftOut.get())));
                    continue;
                }
                SourceProgram source = BkCatalogSourcePrograms.of(entry, site);
                logged.handle(new RecordImportLogCommand(c.run(), ImportLogEntry.downloadsOf(source)));
                if (importProgram.handle(new ImportProgramCommand(c.run(), source)) == ImportProgramResult.ADDED) {
                    added++;
                    log.info("Imported {} {}", id, source.title().value());
                } else {
                    known++;
                }
            } catch (RuntimeException e) {
                failed++;
                log.warn("Could not import {} {}", id, page.url(), e);
                record(c, new ImportOutcome.Failed(new FailedProgram(id, page, reasonOf(e))));
            }
        }
        ImportSummary summary = new ImportSummary(added, known, skipped, failed);
        endRun.handle(new EndImportRunCommand(c.run(), ImportRunState.DONE));
        log.info("Import of {} done: {}", BkCatalogSite.ARCHIVE, summary);
        return summary;
    }

    private void record(ImportBkCatalogCommand c, ImportOutcome outcome) {
        recordOutcome.handle(new RecordImportOutcomeCommand(c.run(), outcome));
    }

    /** What went wrong, as the failure tells it: its message, or its kind when it has none. */
    private static String reasonOf(RuntimeException e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }
}
