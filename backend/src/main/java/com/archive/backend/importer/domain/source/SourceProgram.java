package com.archive.backend.importer.domain.source;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.software.domain.PreservationStatus;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.domain.ReleaseDate;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareLineage;
import com.archive.backend.software.domain.SoftwareLink;
import com.archive.backend.software.domain.SoftwareTitle;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * A program as the parser of a source archive gathered it, for the one import that takes in every source by the same
 * rules (docs/domain/imports.md). What the source does not tell stays empty.
 *
 * @param archive the source archive, such as vektor-06c.ru
 * @param id the id of the program there
 * @param hardware the base hardware of the software
 * @param releases its releases, each with its file; most sources give one
 * @param lineage where the work comes from, unknown when the source does not tell
 * @param warnings what of the program the import could not take as the source gives it
 */
@ValueObject
public record SourceProgram(
        String archive,
        String id,
        ExternalPage page,
        SoftwareTitle title,
        @Nullable SoftwareDescription description,
        @Nullable ReleaseDate softwareDate,
        PreservationStatus preservation,
        PlatformCode platform,
        Set<HardwareCode> hardware,
        Set<CategoryId> categories,
        List<SourceAuthor> authors,
        List<ScreenshotImage> screenshots,
        List<SourceRelease> releases,
        List<SoftwareLink> links,
        SoftwareLineage lineage,
        List<SourceWarning> warnings) {

    public SourceProgram {
        Objects.requireNonNull(archive, "archive");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(preservation, "preservation");
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(lineage, "lineage");
        hardware = Set.copyOf(hardware);
        categories = Set.copyOf(categories);
        authors = List.copyOf(authors);
        screenshots = List.copyOf(screenshots);
        releases = List.copyOf(releases);
        links = List.copyOf(links);
        warnings = List.copyOf(warnings);
    }

    /** A program of one release, without links, lineage or warnings: what most sources give. */
    public SourceProgram(
            String archive,
            String id,
            ExternalPage page,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable ReleaseDate softwareDate,
            @Nullable ReleaseDate releaseDate,
            PreservationStatus preservation,
            PlatformCode platform,
            Set<HardwareCode> hardware,
            Set<HardwareCode> releaseHardware,
            Set<CategoryId> categories,
            List<SourceAuthor> authors,
            List<ScreenshotImage> screenshots,
            @Nullable ReleaseArchiveUpload file) {
        this(
                archive,
                id,
                page,
                title,
                description,
                softwareDate,
                preservation,
                platform,
                hardware,
                categories,
                authors,
                screenshots,
                List.of(new SourceRelease(null, releaseDate, releaseHardware, file)),
                List.of(),
                SoftwareLineage.UNKNOWN,
                List.of());
    }
}
