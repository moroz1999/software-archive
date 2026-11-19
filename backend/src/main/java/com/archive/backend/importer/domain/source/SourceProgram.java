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
 * @param releaseHardware the hardware of its one release, such as its storage
 */
@ValueObject
public record SourceProgram(
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

    public SourceProgram {
        Objects.requireNonNull(archive, "archive");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(preservation, "preservation");
        Objects.requireNonNull(platform, "platform");
        hardware = Set.copyOf(hardware);
        releaseHardware = Set.copyOf(releaseHardware);
        categories = Set.copyOf(categories);
        authors = List.copyOf(authors);
        screenshots = List.copyOf(screenshots);
    }
}
