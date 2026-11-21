package com.archive.backend.importer.domain.basis;

import com.archive.backend.author.domain.RealName;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.source.SourceAuthor;
import com.archive.backend.importer.domain.source.SourceAuthorKind;
import com.archive.backend.importer.domain.source.SourcePlace;
import com.archive.backend.importer.domain.source.SourcePlaceKind;
import com.archive.backend.importer.domain.source.SourceProgram;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.shared.domain.Language;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/**
 * What the parser of "Базис" hands the one import: a card of the index with its files downloaded and its authors read
 * from their pages, by the rules of the site (docs/domain/imports.md#caglrcccscalar).
 */
public final class BasisSourcePrograms {

    private static final PlatformCode VECTOR = new PlatformCode("vector");
    private static final HardwareCode VECTOR_06C = new HardwareCode("vector06c");
    /** The page the site credits a program of an unknown author to. */
    private static final String UNKNOWN_AUTHOR = "20";
    /** The place of the unknown author, "nowhere". */
    private static final String NOWHERE = "Нигде";

    private static final Pattern YEAR = Pattern.compile("\\d{4}");

    private BasisSourcePrograms() {}

    /** Whether the card is software of the archive: software for the PC and paper are not. */
    public static boolean imported(BasisProgram program) {
        return leftOutBy(program).isEmpty();
    }

    /** The category the card is left out by, the first of those it has; none for software of the archive. */
    public static Optional<String> leftOutBy(BasisProgram program) {
        return program.categories().stream()
                .filter(BasisCategories.LEFT_OUT::contains)
                .findFirst();
    }

    public static SourceProgram of(BasisProgram program, BasisSite site) {
        String description = program.description();
        return new SourceProgram(
                BasisSite.ARCHIVE,
                program.id(),
                new ExternalPage(program.url()),
                new SoftwareTitle(program.title()),
                description == null ? null : new SoftwareDescription(description),
                YEAR.matcher(program.year()).matches() ? ReleaseDate.ofYear(Year.parse(program.year())) : null,
                null,
                program.categories().contains(BasisCategories.MISSING)
                        ? PreservationStatus.MISSING_IN_ACTION
                        : PreservationStatus.UNKNOWN,
                VECTOR,
                hardwareOf(program.categories()),
                of(program.categories(), BasisCategories.STORAGE),
                of(program.categories(), BasisCategories.CATEGORIES),
                authorsOf(program, site),
                screenshotsOf(program, site),
                fileOf(program, site));
    }

    /** The computer the categories name, the Vector-06C when they name none, and the hardware the program uses. */
    private static Set<HardwareCode> hardwareOf(List<String> categories) {
        Set<HardwareCode> hardware = new LinkedHashSet<>(of(categories, BasisCategories.COMPUTERS));
        if (hardware.isEmpty()) {
            hardware.add(VECTOR_06C);
        }
        hardware.addAll(of(categories, BasisCategories.HARDWARE));
        return hardware;
    }

    private static <T> Set<T> of(List<String> categories, Map<String, T> meanings) {
        return categories.stream()
                .map(meanings::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static List<SourceAuthor> authorsOf(BasisProgram program, BasisSite site) {
        return program.authorIds().stream()
                .filter(id -> !id.equals(UNKNOWN_AUTHOR))
                .map(site::author)
                .map(BasisSourcePrograms::authorOf)
                .toList();
    }

    /**
     * A person when the page gives a nickname apart from the name, a group when the two are the same, and a label of
     * nobody under the name when the page gives no nickname.
     */
    private static SourceAuthor authorOf(BasisAuthor author) {
        String nickname = author.nickname();
        SourcePlace place = placeOf(author.place());
        if (nickname == null) {
            return new SourceAuthor(author.id(), new LabelName(author.name()), SourceAuthorKind.LABEL, null, place);
        }
        if (nickname.equals(author.name())) {
            return new SourceAuthor(author.id(), new LabelName(author.name()), SourceAuthorKind.GROUP, null, place);
        }
        return new SourceAuthor(
                author.id(), new LabelName(nickname), SourceAuthorKind.PERSON, new RealName(author.name()), place);
    }

    private static @Nullable SourcePlace placeOf(@Nullable String place) {
        return place == null || place.isBlank() || place.equals(NOWHERE)
                ? null
                : new SourcePlace(place, Language.RU, SourcePlaceKind.CITY_OR_COUNTRY);
    }

    private static List<ScreenshotImage> screenshotsOf(BasisProgram program, BasisSite site) {
        return program.screenshotUrls().stream()
                .map(url -> {
                    byte[] picture = site.download(url);
                    return ScreenshotImage.of(
                            Arrays.copyOf(picture, Math.min(picture.length, ScreenshotImage.HEAD_BYTES)),
                            picture.length,
                            new ScreenshotFileName(nameOf(url)),
                            () -> new ByteArrayInputStream(picture));
                })
                .toList();
    }

    private static @Nullable ReleaseArchiveUpload fileOf(BasisProgram program, BasisSite site) {
        String url = program.fileUrl();
        if (url == null) {
            return null;
        }
        byte[] file = site.download(url);
        return new ReleaseArchiveUpload(nameOf(url), file.length, () -> new ByteArrayInputStream(file));
    }

    private static String nameOf(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }
}
