package com.archive.backend.importer.domain.vektor;

import com.archive.backend.hardware.domain.HardwareCode;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The storage a release of vektor-06c.ru comes on, told by the files of its archive: disk images are floppies, ROM
 * files and their recordings are tape.
 */
public final class VektorStorage {

    private static final HardwareCode FLOPPY = new HardwareCode("floppy");
    private static final HardwareCode TAPE = new HardwareCode("tape");
    private static final Map<String, HardwareCode> BY_EXTENSION =
            Map.of("fdd", FLOPPY, "rom", TAPE, "r0m", TAPE, "wav", TAPE);

    private VektorStorage() {}

    public static Set<HardwareCode> of(List<String> archiveFiles) {
        Set<HardwareCode> storage = new LinkedHashSet<>();
        for (String file : archiveFiles) {
            int dot = file.lastIndexOf('.');
            HardwareCode code =
                    dot < 0 ? null : BY_EXTENSION.get(file.substring(dot + 1).toLowerCase(Locale.ROOT));
            if (code != null) {
                storage.add(code);
            }
        }
        return Set.copyOf(storage);
    }
}
