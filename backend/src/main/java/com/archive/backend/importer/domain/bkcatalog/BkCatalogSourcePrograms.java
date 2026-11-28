package com.archive.backend.importer.domain.bkcatalog;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.source.SourceAuthor;
import com.archive.backend.importer.domain.source.SourceProgram;
import com.archive.backend.importer.domain.source.SourceRelease;
import com.archive.backend.importer.domain.source.SourceWarning;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.software.domain.LineageKind;
import com.archive.backend.software.domain.LineageReliability;
import com.archive.backend.software.domain.LineageRework;
import com.archive.backend.software.domain.LineageSource;
import com.archive.backend.software.domain.PreservationStatus;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.domain.ReleaseDescription;
import com.archive.backend.software.domain.ScreenshotFileName;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareLineage;
import com.archive.backend.software.domain.SoftwareLink;
import com.archive.backend.software.domain.SoftwareLinkType;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.software.domain.SoftwareUrl;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/**
 * What the parser of bk-catalog hands the one import: an entry of a list with its files downloaded, by the rules of
 * the site (docs/domain/imports.md#bk-catalog). What the import cannot take as the entry gives it is left out of the
 * program, with a warning.
 */
public final class BkCatalogSourcePrograms {

    private static final PlatformCode BK = new PlatformCode("bk");
    private static final int FILES = 5;
    private static final int SCREENSHOTS = 12;
    private static final Pattern COMMA = Pattern.compile("\\s*,\\s*");
    private static final Pattern GAME_ID = Pattern.compile("\\d+");

    /** The columns of the people of an entry, each with the role it credits them with. */
    private static final Map<String, String> ROLES = rolesByColumn();

    private static final Map<String, LineageKind> KINDS = Map.of(
            "Оригинал", LineageKind.ORIGINAL,
            "Порт", LineageKind.PORT,
            "Порт с изменениями", LineageKind.PORT_WITH_CHANGES,
            "Ремейк", LineageKind.REMAKE);
    private static final Map<String, LineageReliability> RELIABILITIES =
            Map.of("Достоверно", LineageReliability.RELIABLE);
    private static final Map<String, LineageRework> REWORKS = Map.of(
            "Без существенных изменений", LineageRework.NO_SUBSTANTIAL_CHANGES,
            "Использованы материалы оригинала", LineageRework.ORIGINAL_MATERIAL_USED,
            "Добавлены новые уровни", LineageRework.NEW_LEVELS_ADDED,
            "Изменены графика и звук", LineageRework.GRAPHICS_AND_SOUND_CHANGED,
            "Полностью новая реализация", LineageRework.NEW_IMPLEMENTATION);

    private BkCatalogSourcePrograms() {}

    /** The platform the entry is left out by, software for the PC; none for software of the BK. */
    public static Optional<String> leftOutBy(BkCatalogEntry entry) {
        String platform = entry.get("Платформа");
        return BkCatalogPlatforms.LEFT_OUT.contains(platform) ? Optional.of(platform) : Optional.empty();
    }

    /** The id of the entry in the archive: its section and its id there, as {@code game-555901}. */
    public static String idOf(BkCatalogEntry entry) {
        return entry.section().card() + "-" + entry.id();
    }

    /** The card of the entry on the home page of the catalog. */
    public static ExternalPage pageOf(BkCatalogEntry entry) {
        return new ExternalPage(BkCatalogSite.cardUrl(entry.section(), entry.id()));
    }

    public static SourceProgram of(BkCatalogEntry entry, BkCatalogSite site) {
        List<SourceWarning> warnings = new ArrayList<>();
        String description = entry.get("Описание");
        BkCatalogDates.Reading came = BkCatalogDates.read(entry.get("Дата выхода"));
        if (!came.readable()) {
            warnings.add(new SourceWarning(SourceWarning.Kind.DATE, entry.get("Дата выхода")));
        }
        BkCatalogDates.Reading issued = BkCatalogDates.read(entry.get("Год выпуска"));
        if (!issued.readable()) {
            warnings.add(new SourceWarning(SourceWarning.Kind.DATE, entry.get("Год выпуска")));
        }
        return new SourceProgram(
                BkCatalogSite.ARCHIVE,
                idOf(entry),
                pageOf(entry),
                new SoftwareTitle(entry.get("Название")),
                description.isEmpty() ? null : new SoftwareDescription(description),
                issued.date() != null ? BkCatalogDates.yearOf(issued.date()) : BkCatalogDates.yearOf(came.date()),
                PreservationStatus.UNKNOWN,
                BK,
                BkCatalogPlatforms.hardwareOf(entry.get("Платформа"), warnings),
                BkCatalogGenres.categoriesOf(entry.section(), entry.get("Жанр"), warnings),
                authorsOf(entry),
                screenshotsOf(entry, site, warnings),
                releasesOf(entry, site, came, warnings),
                linksOf(entry),
                lineageOf(entry, warnings),
                warnings);
    }

    private static Map<String, String> rolesByColumn() {
        Map<String, String> roles = new LinkedHashMap<>();
        roles.put("Авторы", "unknown");
        roles.put("Издатель", "publisher");
        roles.put("Графика", "graphics");
        roles.put("Музыка", "music");
        return roles;
    }

    /**
     * Every name of the people columns, taken whole as a label, once, with the role of each column it is in; an
     * unknown role goes once a known one is there.
     */
    private static List<SourceAuthor> authorsOf(BkCatalogEntry entry) {
        Map<String, List<String>> roles = new LinkedHashMap<>();
        ROLES.forEach((column, role) -> namesOf(entry.get(column)).forEach(name -> {
            List<String> of = roles.computeIfAbsent(name, n -> new ArrayList<>());
            if (!of.contains(role)) {
                of.add(role);
            }
        }));
        return roles.entrySet().stream()
                .map(author -> {
                    List<String> known = author.getValue().stream()
                            .filter(role -> !role.equals("unknown"))
                            .toList();
                    return SourceAuthor.label(
                            new LabelName(author.getKey()), known.isEmpty() ? author.getValue() : known);
                })
                .toList();
    }

    private static List<String> namesOf(String names) {
        return names.isEmpty()
                ? List.of()
                : Arrays.stream(COMMA.split(names))
                        .filter(name -> !name.isEmpty())
                        .toList();
    }

    private static List<ScreenshotImage> screenshotsOf(
            BkCatalogEntry entry, BkCatalogSite site, List<SourceWarning> warnings) {
        List<ScreenshotImage> screenshots = new ArrayList<>();
        for (int i = 1; i <= SCREENSHOTS; i++) {
            String name = entry.get("Скриншот " + i);
            if (name.isEmpty()) {
                continue;
            }
            Optional<byte[]> picture = site.download(entry.section().screenshots() + "/" + name);
            if (picture.isEmpty()) {
                warnings.add(new SourceWarning(SourceWarning.Kind.SCREENSHOT_MISSING, name));
                continue;
            }
            byte[] bytes = picture.get();
            screenshots.add(ScreenshotImage.of(
                    Arrays.copyOf(bytes, Math.min(bytes.length, ScreenshotImage.HEAD_BYTES)),
                    bytes.length,
                    new ScreenshotFileName(name),
                    () -> new ByteArrayInputStream(bytes)));
        }
        return screenshots;
    }

    /** Every file of the entry a release of its own, dated by the day or the year the program came out. */
    private static List<SourceRelease> releasesOf(
            BkCatalogEntry entry, BkCatalogSite site, BkCatalogDates.Reading came, List<SourceWarning> warnings) {
        List<SourceRelease> releases = new ArrayList<>();
        for (int i = 1; i <= FILES; i++) {
            String name = entry.get("Имя файла " + i);
            if (name.isEmpty()) {
                continue;
            }
            String about = entry.get("Описание файла " + i);
            Optional<byte[]> file = site.download(entry.section().files() + "/" + name);
            if (file.isEmpty()) {
                warnings.add(new SourceWarning(SourceWarning.Kind.FILE_MISSING, name));
            }
            releases.add(new SourceRelease(
                    about.isEmpty() ? null : new ReleaseDescription(about),
                    came.date(),
                    Set.of(),
                    file.map(bytes ->
                                    new ReleaseArchiveUpload(name, bytes.length, () -> new ByteArrayInputStream(bytes)))
                            .orElse(null)));
        }
        return releases;
    }

    /**
     * The page of the entry elsewhere, its repository and its video; never the catalog itself. Each address is linked
     * once: a page that is also the repository or the video is linked as that.
     */
    private static List<SoftwareLink> linksOf(BkCatalogEntry entry) {
        List<SoftwareLink> links = new ArrayList<>();
        String page = entry.get("Ссылка на страницу");
        if (!page.equals(entry.get("ГитХаб")) && !page.equals(entry.get("Видео"))) {
            link(page, SoftwareLinkType.GENERAL).ifPresent(links::add);
        }
        link(entry.get("ГитХаб"), SoftwareLinkType.HOMEPAGE).ifPresent(links::add);
        link(entry.get("Видео"), SoftwareLinkType.YOUTUBE).ifPresent(links::add);
        return links;
    }

    private static Optional<SoftwareLink> link(String address, SoftwareLinkType type) {
        if (address.isEmpty() || address.startsWith(BkCatalogSite.HOME)) {
            return Optional.empty();
        }
        try {
            return Optional.of(new SoftwareLink(type, new SoftwareUrl(address)));
        } catch (InvalidValueException e) {
            return Optional.empty();
        }
    }

    /**
     * The kind, the reliability, the rework and the original the entry tells; a part it names that the import does not
     * know is left out with a warning, and without a kind there is neither reliability nor rework.
     */
    private static SoftwareLineage lineageOf(BkCatalogEntry entry, List<SourceWarning> warnings) {
        LineageKind kind = known(entry.get("Происхождение"), KINDS, warnings);
        LineageReliability reliability = known(entry.get("Достоверность происхождения"), RELIABILITIES, warnings);
        Set<LineageRework> rework = EnumSet.noneOf(LineageRework.class);
        for (String value : namesOf(entry.get("Характер переработки"))) {
            LineageRework known = known(value, REWORKS, warnings);
            if (known != null) {
                rework.add(known);
            }
        }
        LineageSource source = sourceOf(entry, warnings);
        return kind == null
                ? new SoftwareLineage(null, null, Set.of(), source)
                : new SoftwareLineage(kind, reliability, rework, source);
    }

    private static <T> @Nullable T known(String value, Map<String, T> meanings, List<SourceWarning> warnings) {
        if (value.isEmpty()) {
            return null;
        }
        T meaning = meanings.get(value);
        if (meaning == null) {
            warnings.add(new SourceWarning(SourceWarning.Kind.LINEAGE, value));
        }
        return meaning;
    }

    /** The title, the platform and the address of the original in one line; a game of the catalog by its card. */
    private static @Nullable LineageSource sourceOf(BkCatalogEntry entry, List<SourceWarning> warnings) {
        String link = entry.get("Связанные игры (ссылка)");
        if (GAME_ID.matcher(link).matches()) {
            link = BkCatalogSite.cardUrl(BkCatalogSection.GAMES, link);
        }
        String text = Arrays.stream(new String[] {
                    entry.get("Связанные игры (первоисточник)"), entry.get("Связанные игры (Платформа)"), link
                })
                .filter(part -> !part.isEmpty())
                .collect(Collectors.joining(", "));
        if (text.isEmpty()) {
            return null;
        }
        if (text.length() > LineageSource.MAX_LENGTH) {
            warnings.add(new SourceWarning(SourceWarning.Kind.LINEAGE_SOURCE_TOO_LONG, text));
            return null;
        }
        return new LineageSource(text);
    }
}
