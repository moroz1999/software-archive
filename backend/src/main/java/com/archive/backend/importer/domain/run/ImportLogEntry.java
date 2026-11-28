package com.archive.backend.importer.domain.run;

import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.source.SourceProgram;
import com.archive.backend.importer.domain.source.SourceRelease;
import com.archive.backend.importer.domain.source.SourceWarning;
import com.archive.backend.shared.domain.EnumCodes;
import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.domain.ScreenshotImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** One step of a run as its log tells it (docs/domain/imports.md). */
public sealed interface ImportLogEntry {

    /**
     * A page of the list of programs read: its number, of how many pages when the source tells, and how many programs
     * it lists.
     */
    @ValueObject
    record ListRead(ExternalPage page, int number, @Nullable Integer pages, int programs) implements ImportLogEntry {

        public ListRead {
            Objects.requireNonNull(page, "page");
        }
    }

    /** What a file of a program downloaded from the source is. */
    enum DownloadedFile {
        ARCHIVE,
        SCREENSHOT;

        /** The code it is known by outside the backend: {@code ARCHIVE} as {@code archive}. */
        public String code() {
            return EnumCodes.kebab(this);
        }
    }

    /** A file of a program downloaded from the source: its name there and its size in bytes. */
    @ValueObject
    record Downloaded(String externalId, String title, DownloadedFile file, String name, long size)
            implements ImportLogEntry {

        public Downloaded {
            Objects.requireNonNull(externalId, "externalId");
            Objects.requireNonNull(title, "title");
            Objects.requireNonNull(file, "file");
            Objects.requireNonNull(name, "name");
        }
    }

    @ValueObject
    record Added(AddedProgram program) implements ImportLogEntry {}

    /** Found in the archive; what was added to it, or nothing. */
    @ValueObject
    record Known(KnownProgram program) implements ImportLogEntry {}

    @ValueObject
    record Skipped(SkippedProgram program) implements ImportLogEntry {}

    @ValueObject
    record Failed(FailedProgram program) implements ImportLogEntry {}

    /**
     * What of a program the import could not take as the source gives it, for the administrator to look at: the
     * program, leading to its page in the source, and the warning.
     */
    @ValueObject
    record Warning(String externalId, ExternalPage page, String title, SourceWarning warning)
            implements ImportLogEntry {

        public Warning {
            Objects.requireNonNull(externalId, "externalId");
            Objects.requireNonNull(page, "page");
            Objects.requireNonNull(title, "title");
            Objects.requireNonNull(warning, "warning");
        }
    }

    /** An author taken for the first time. */
    @ValueObject
    record Author(TakenAuthor author) implements ImportLogEntry {}

    /** A city or a country added. */
    @ValueObject
    record Place(AddedPlace place) implements ImportLogEntry {}

    /** What of the program the import could not take as the source gives it, one line each. */
    static List<ImportLogEntry> warningsOf(SourceProgram program) {
        return program.warnings().stream()
                .<ImportLogEntry>map(warning -> new Warning(
                        program.id(), program.page(), program.title().value(), warning))
                .toList();
    }

    /** The files of a program the source handed over, as they were downloaded: its archive, then its screenshots. */
    static List<ImportLogEntry> downloadsOf(SourceProgram program) {
        List<ImportLogEntry> downloads = new ArrayList<>();
        String title = program.title().value();
        for (SourceRelease release : program.releases()) {
            ReleaseArchiveUpload file = release.file();
            if (file != null) {
                downloads.add(
                        new Downloaded(program.id(), title, DownloadedFile.ARCHIVE, file.fileName(), file.size()));
            }
        }
        for (ScreenshotImage screenshot : program.screenshots()) {
            downloads.add(new Downloaded(
                    program.id(),
                    title,
                    DownloadedFile.SCREENSHOT,
                    screenshot.name().value(),
                    screenshot.size()));
        }
        return downloads;
    }
}
