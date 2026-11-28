package com.archive.backend.importer.domain.bkcatalog;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.importer.domain.source.SourceWarning;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * What each platform of bk-catalog is in the archive (docs/domain/imports.md#bk-catalog): the computers of the BK
 * family and the firmware of their ROM it runs on. Software for the PC is left out.
 */
final class BkCatalogPlatforms {

    static final Set<String> LEFT_OUT = Set.of("Windows", "MS-DOS");

    private static final Map<String, String> HARDWARE = Map.ofEntries(
            Map.entry("БК0010", "bk0010"),
            Map.entry("БК0010-01", "bk0010-01"),
            Map.entry("БК0010-01 БЕЙСИК", "bk0010-01 basic"),
            Map.entry("БК0010 ФОКАЛ", "bk0010 focal"),
            Map.entry("БК0010 БЕЙСИК 2026", "bk0010 basic"),
            Map.entry("БК0011 БЕЙСИК", "bk0011 basic"),
            Map.entry("БК0011 ФОКАЛ", "bk0011 focal"),
            Map.entry("БК0011М", "bk0011m"),
            Map.entry("БК0011М + СМК512", "bk0011m smk512"),
            Map.entry("БК0010 + СМК512", "bk0010 smk512"),
            Map.entry("БК0010 (11М)", "bk0010 bk0011m"),
            Map.entry("AZБК", "azbk"));

    private BkCatalogPlatforms() {}

    /** The hardware the platform names; none, with a warning, for a platform the import does not know. */
    static Set<HardwareCode> hardwareOf(String platform, List<SourceWarning> warnings) {
        if (platform.isEmpty()) {
            return Set.of();
        }
        String hardware = HARDWARE.get(platform);
        if (hardware == null) {
            warnings.add(new SourceWarning(SourceWarning.Kind.PLATFORM, platform));
            return Set.of();
        }
        return Pattern.compile(" ")
                .splitAsStream(hardware)
                .map(HardwareCode::new)
                .collect(Collectors.toSet());
    }
}
