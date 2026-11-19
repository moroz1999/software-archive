package com.archive.backend.importer.domain.vektor;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.source.SourceAuthor;
import com.archive.backend.importer.domain.source.SourceProgram;
import com.archive.backend.software.domain.PreservationStatus;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.domain.ReleaseDate;
import com.archive.backend.software.domain.ScreenshotFileName;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareTitle;
import java.io.ByteArrayInputStream;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * What the parser of vektor-06c.ru hands the one import: a program of the site with its archive and picture
 * downloaded, read by the rules of the site (docs/domain/imports.md#vektor-06cru).
 */
public final class VektorSourcePrograms {

    private static final PlatformCode VECTOR = new PlatformCode("vector");
    private static final HardwareCode VECTOR_06C = new HardwareCode("vector06c");

    private VektorSourcePrograms() {}

    public static SourceProgram of(VektorProgram program, VektorSite site) {
        VektorYears years = VektorYears.parse(program.year());
        CategoryId category = VektorSections.placementOf(program.section()).category();
        String description = program.description();
        return new SourceProgram(
                VektorSite.ARCHIVE,
                program.id(),
                new ExternalPage(program.url()),
                new SoftwareTitle(program.title()),
                description == null ? null : new SoftwareDescription(description),
                dateOf(years.software()),
                dateOf(years.release()),
                PreservationStatus.UNKNOWN,
                VECTOR,
                Set.of(VECTOR_06C),
                VektorStorage.of(program.archiveFiles()),
                category == null ? Set.of() : Set.of(category),
                VektorAuthors.labels(program.authors()).stream()
                        .map(SourceAuthor::label)
                        .toList(),
                screenshotsOf(program, site),
                archiveOf(program, site));
    }

    private static List<ScreenshotImage> screenshotsOf(VektorProgram program, VektorSite site) {
        String url = program.screenshotUrl();
        if (url == null) {
            return List.of();
        }
        byte[] picture = site.download(url);
        return List.of(ScreenshotImage.of(
                Arrays.copyOf(picture, Math.min(picture.length, ScreenshotImage.HEAD_BYTES)),
                picture.length,
                new ScreenshotFileName(url.substring(url.lastIndexOf('/') + 1)),
                () -> new ByteArrayInputStream(picture)));
    }

    private static @Nullable ReleaseArchiveUpload archiveOf(VektorProgram program, VektorSite site) {
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
