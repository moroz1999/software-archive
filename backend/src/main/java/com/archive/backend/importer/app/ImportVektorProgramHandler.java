package com.archive.backend.importer.app;

import com.archive.backend.author.domain.PersonReference;
import com.archive.backend.author.domain.PersonReferenceId;
import com.archive.backend.author.domain.PersonReferenceType;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.importer.domain.ExternalOrigin;
import com.archive.backend.importer.domain.ImportedEntry;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.importer.domain.ImportedEntryRepository;
import com.archive.backend.importer.domain.Importer;
import com.archive.backend.importer.domain.vektor.VektorAuthors;
import com.archive.backend.importer.domain.vektor.VektorProgram;
import com.archive.backend.importer.domain.vektor.VektorSections;
import com.archive.backend.importer.domain.vektor.VektorSite;
import com.archive.backend.importer.domain.vektor.VektorStorage;
import com.archive.backend.importer.domain.vektor.VektorYears;
import com.archive.backend.label.app.CreateLabelCommand;
import com.archive.backend.label.app.CreateLabelHandler;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.software.app.AddScreenshotsCommand;
import com.archive.backend.software.app.AddScreenshotsHandler;
import com.archive.backend.software.app.CreateReleaseCommand;
import com.archive.backend.software.app.CreateReleaseHandler;
import com.archive.backend.software.app.CreateSoftwareCommand;
import com.archive.backend.software.app.CreateSoftwareHandler;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.ReleaseDate;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.user.domain.Actor;
import java.io.ByteArrayInputStream;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adds one program of vektor-06c.ru as software with its release, picture and labels, through the use cases of the
 * archive, and remembers where it came from (docs/domain/imports.md). It all commits together or not at all.
 */
@Service
@RequiredArgsConstructor
public class ImportVektorProgramHandler {

    private static final PlatformCode VECTOR = new PlatformCode("vector");
    private static final HardwareCode VECTOR_06C = new HardwareCode("vector06c");

    private final VektorSite site;
    private final ImportedEntryRepository entries;
    private final CreateLabelHandler createLabel;
    private final CreateSoftwareHandler createSoftware;
    private final CreateReleaseHandler createRelease;
    private final AddScreenshotsHandler addScreenshots;

    @Transactional
    public void handle(ImportVektorProgramCommand c) {
        Actor actor = Importer.ACTOR;
        VektorProgram program = c.program();
        VektorYears years = VektorYears.parse(program.year());
        CategoryId category = VektorSections.placementOf(program.section()).category();
        List<SoftwareCreditDraft> credits = VektorAuthors.labels(program.authors()).stream()
                .map(name -> new SoftwareCreditDraft(
                        new PersonReference(PersonReferenceType.AUTHOR_LABEL, new PersonReferenceId(labelOf(name))),
                        List.of()))
                .toList();
        String description = program.description();
        SoftwareId software = createSoftware.handle(new CreateSoftwareCommand(
                actor,
                new SoftwareTitle(program.title()),
                description == null ? null : new SoftwareDescription(description),
                dateOf(years.software()),
                VECTOR,
                Set.of(VECTOR_06C),
                category == null ? Set.of() : Set.of(category),
                Set.of(),
                Set.of(),
                credits));
        createRelease.handle(new CreateReleaseCommand(
                actor,
                software,
                new ReleaseContent(null, null, ReleaseType.ORIGINAL, dateOf(years.release()), null),
                VektorStorage.of(program.archiveFiles()),
                archiveOf(program)));
        String screenshot = program.screenshotUrl();
        if (screenshot != null) {
            byte[] picture = site.download(screenshot);
            addScreenshots.handle(new AddScreenshotsCommand(
                    actor,
                    software,
                    null,
                    List.of(ScreenshotImage.of(
                            Arrays.copyOf(picture, Math.min(picture.length, ScreenshotImage.HEAD_BYTES)),
                            picture.length,
                            () -> new ByteArrayInputStream(picture)))));
        }
        entries.save(new ImportedEntry(
                ExternalOrigin.software(VektorSite.ARCHIVE, program.id()), new ImportedEntryId(software.value())));
    }

    /**
     * The label of this name the import took before, or a new label of nobody.
     */
    private UUID labelOf(LabelName name) {
        ExternalOrigin origin = ExternalOrigin.label(VektorSite.ARCHIVE, name);
        return entries.findByOrigin(origin)
                .map(entry -> entry.getEntry().value())
                .orElseGet(() -> {
                    UUID id = createLabel
                            .handle(new CreateLabelCommand(Importer.ACTOR, name))
                            .value();
                    entries.save(new ImportedEntry(origin, new ImportedEntryId(id)));
                    return id;
                });
    }

    private @Nullable ReleaseArchiveUpload archiveOf(VektorProgram program) {
        String url = program.archiveUrl();
        if (url == null) {
            return null;
        }
        byte[] archive = site.download(url);
        String name = program.archiveName();
        return new ReleaseArchiveUpload(
                name == null || name.isBlank() ? url.substring(url.lastIndexOf('/') + 1) : name,
                archive.length,
                () -> new ByteArrayInputStream(archive));
    }

    private static @Nullable ReleaseDate dateOf(@Nullable Year year) {
        return year == null ? null : ReleaseDate.ofYear(year);
    }
}
