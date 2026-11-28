package com.archive.backend.importer.app;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.importer.domain.ExternalKind;
import com.archive.backend.importer.domain.ExternalOrigin;
import com.archive.backend.importer.domain.ImportedEntry;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.importer.domain.ImportedEntryRepository;
import com.archive.backend.importer.domain.Importer;
import com.archive.backend.importer.domain.run.AddedProgram;
import com.archive.backend.importer.domain.run.ImportLogEntry;
import com.archive.backend.importer.domain.run.ImportOutcome;
import com.archive.backend.importer.domain.run.ImportedCredits;
import com.archive.backend.importer.domain.run.KnownProgram;
import com.archive.backend.importer.domain.source.SourceProgram;
import com.archive.backend.importer.domain.source.SourceRelease;
import com.archive.backend.software.app.AddScreenshotsCommand;
import com.archive.backend.software.app.AddScreenshotsHandler;
import com.archive.backend.software.app.AddSoftwareLinksCommand;
import com.archive.backend.software.app.AddSoftwareLinksHandler;
import com.archive.backend.software.app.CreateReleaseCommand;
import com.archive.backend.software.app.CreateReleaseHandler;
import com.archive.backend.software.app.CreateSoftwareCommand;
import com.archive.backend.software.app.CreateSoftwareHandler;
import com.archive.backend.software.app.SupplementSoftwareCommand;
import com.archive.backend.software.app.SupplementSoftwareHandler;
import com.archive.backend.software.domain.ProgramFiles;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.ReleaseFile;
import com.archive.backend.software.domain.ReleaseFileHash;
import com.archive.backend.software.domain.ReleaseFileStructureParser;
import com.archive.backend.software.domain.ReleaseFileTrees;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.ReleasedFileTree;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareSupplement;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The one import of every source archive (docs/domain/imports.md): finds the program among the software of the archive
 * by its origin or by the files of its releases, adds it when it is not there, and otherwise adds to it only what the
 * archive lacks; then logs what of it could not be taken as the source gives it. It all commits together or not at all.
 */
@Service
@RequiredArgsConstructor
public class ImportProgramHandler {

    private final ImportedEntryRepository entries;
    private final CreditImportedAuthorsHandler creditAuthors;
    private final ReleaseFileStructureParser parser;
    private final ReleaseFileTrees trees;
    private final CreateSoftwareHandler createSoftware;
    private final CreateReleaseHandler createRelease;
    private final AddScreenshotsHandler addScreenshots;
    private final SupplementSoftwareHandler supplementSoftware;
    private final RecordImportOutcomeHandler recordOutcome;
    private final RecordImportLogHandler logged;
    private final AddSoftwareLinksHandler addLinks;

    @Transactional
    public ImportProgramResult handle(ImportProgramCommand c) {
        SourceProgram program = c.program();
        ImportedCredits imported = creditAuthors.handle(new CreditImportedAuthorsCommand(program));
        List<SoftwareCreditDraft> credits = imported.credits();
        Optional<SoftwareId> known = knownSoftware(program);
        if (known.isPresent()) {
            Set<SoftwareSupplement> supplements = supplementSoftware.handle(new SupplementSoftwareCommand(
                    Importer.ACTOR,
                    known.get(),
                    program.description(),
                    program.softwareDate(),
                    program.preservation(),
                    program.categories(),
                    credits,
                    program.screenshots(),
                    program.lineage(),
                    program.links()));
            recordOutcome.handle(new RecordImportOutcomeCommand(
                    c.run(),
                    new ImportOutcome.Known(
                            new KnownProgram(
                                    program.id(),
                                    new ImportedEntryId(known.get().value()),
                                    program.title().value(),
                                    supplements),
                            imported.taken())));
            logWarnings(c, program);
            return ImportProgramResult.KNOWN;
        }
        SoftwareId software = add(program, credits);
        remember(program, software);
        recordOutcome.handle(new RecordImportOutcomeCommand(
                c.run(),
                new ImportOutcome.Added(
                        new AddedProgram(
                                program.id(),
                                new ImportedEntryId(software.value()),
                                program.title().value()),
                        imported.taken())));
        logWarnings(c, program);
        return ImportProgramResult.ADDED;
    }

    /** What of the program could not be taken as the source gives it, after what it came to. */
    private void logWarnings(ImportProgramCommand c, SourceProgram program) {
        logged.handle(new RecordImportLogCommand(c.run(), ImportLogEntry.warningsOf(program)));
    }

    /**
     * The software the program became in an earlier run, or the one with a release of the program files of any of its
     * releases; the latter is remembered as coming from this source too.
     */
    private Optional<SoftwareId> knownSoftware(SourceProgram program) {
        Optional<SoftwareId> imported = entries.findByOrigin(origin(program))
                .map(entry -> new SoftwareId(entry.getEntry().value()));
        if (imported.isPresent()) {
            return imported;
        }
        Optional<SoftwareId> matched = program.releases().stream()
                .map(SourceRelease::file)
                .filter(Objects::nonNull)
                .map(file -> sameRelease(program, parser.parse(program.platform(), file)))
                .flatMap(Optional::stream)
                .findFirst();
        matched.ifPresent(software -> remember(program, software));
        return matched;
    }

    /**
     * A release whose program files are exactly those of the file, all of them and none more, is the same release, and
     * its software the same software (docs/domain/imports.md#software). A file without program files matches nothing,
     * and software that came from this source already is another entry of it, never the match.
     */
    private Optional<SoftwareId> sameRelease(SourceProgram program, ReleaseFile file) {
        PlatformCode platform = program.platform();
        Set<ReleaseFileHash> files = ProgramFiles.of(platform, file);
        if (files.isEmpty()) {
            return Optional.empty();
        }
        return trees.holdingAnyOf(platform, files).stream()
                .filter(tree -> ProgramFiles.of(platform, tree.root()).equals(files))
                .map(ReleasedFileTree::software)
                .filter(software -> !entries.isFrom(
                        program.archive(), ExternalKind.SOFTWARE, new ImportedEntryId(software.value())))
                .findFirst();
    }

    private SoftwareId add(SourceProgram program, List<SoftwareCreditDraft> credits) {
        SoftwareId software = createSoftware.handle(new CreateSoftwareCommand(
                Importer.ACTOR,
                program.title(),
                program.description(),
                program.softwareDate(),
                program.platform(),
                program.hardware(),
                program.categories(),
                credits,
                program.preservation(),
                null,
                null,
                null,
                program.lineage()));
        for (SourceRelease release : program.releases()) {
            createRelease.handle(new CreateReleaseCommand(
                    Importer.ACTOR,
                    software,
                    new ReleaseContent(null, null, ReleaseType.ORIGINAL, release.date(), release.description()),
                    release.hardware(),
                    release.file()));
        }
        List<ScreenshotImage> screenshots = ofDistinctSizes(program.screenshots());
        if (!screenshots.isEmpty()) {
            addScreenshots.handle(new AddScreenshotsCommand(Importer.ACTOR, software, null, screenshots));
        }
        addLinks.handle(new AddSoftwareLinksCommand(Importer.ACTOR, software, program.links()));
        return software;
    }

    private void remember(SourceProgram program, SoftwareId software) {
        entries.save(new ImportedEntry(origin(program), new ImportedEntryId(software.value()), program.page()));
    }

    private static ExternalOrigin origin(SourceProgram program) {
        return ExternalOrigin.software(program.archive(), program.id());
    }

    /** Screenshots of the same size are the same one (docs/domain/imports.md#software). */
    private static List<ScreenshotImage> ofDistinctSizes(List<ScreenshotImage> screenshots) {
        Set<Long> sizes = new HashSet<>();
        return screenshots.stream().filter(image -> sizes.add(image.size())).toList();
    }
}
