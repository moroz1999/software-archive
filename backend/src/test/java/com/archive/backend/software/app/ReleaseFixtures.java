package com.archive.backend.software.app;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryLine;
import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.category.domain.CategorySets;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.HardwareSets;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ReleaseTitle;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** The software, releases and reference book the software use case tests work on. */
final class ReleaseFixtures {

    static final SoftwareId ELITE = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    static final ReleaseId AY_MOD = new ReleaseId(UUID.fromString("00000000-0000-0000-0000-000000000012"));
    static final ReleaseId UNKNOWN = new ReleaseId(UUID.fromString("00000000-0000-0000-0000-000000000099"));
    static final PlatformCode VECTOR = new PlatformCode("vector");
    static final PlatformCode BK = new PlatformCode("bk");
    static final HardwareCode AY = new HardwareCode("ay8910");
    /** Hardware of another platform only. */
    static final HardwareCode BK0011M = new HardwareCode("bk0011m");

    static final CategoryId GAMES = new CategoryId(UUID.fromString("00000000-0000-0000-0000-000000000021"));
    /** Under Games. */
    static final CategoryId ARCADE = new CategoryId(UUID.fromString("00000000-0000-0000-0000-000000000022"));

    static final CategoryId DEMOSCENE = new CategoryId(UUID.fromString("00000000-0000-0000-0000-000000000023"));
    /** A category the archive does not have. */
    static final CategoryId GONE = new CategoryId(UUID.fromString("00000000-0000-0000-0000-000000000029"));

    static final ReleaseContent AY_MOD_CONTENT =
            new ReleaseContent(new ReleaseTitle("Elite AY"), null, ReleaseType.MOD, null, null);
    static final ReleaseContent BUGFIX_CONTENT = new ReleaseContent(null, null, ReleaseType.BUGFIX, null, null);

    private ReleaseFixtures() {}

    /** A set the reference book has already checked. */
    static HardwareSet vector(HardwareCode... codes) {
        return HardwareSet.restore(VECTOR, Set.of(codes));
    }

    static Software elite() {
        return Software.create(ELITE, new SoftwareTitle("Elite"), null, null, vector(), CategorySet.EMPTY);
    }

    /** Elite with its AY mod, which has the AY chip. */
    static Software eliteWithAyMod() {
        Software software = elite();
        software.addRelease(AY_MOD, AY_MOD_CONTENT, vector(AY));
        return software;
    }

    /** Vector with the AY chip, BK with the BK-0011M. */
    static HardwareSets hardwareSets() {
        return new HardwareSets(new InMemoryHardwareCatalog().on(VECTOR, AY).on(BK, BK0011M));
    }

    /** Games → Arcade and Demoscene: the tree of the archive. */
    private static final Map<CategoryId, CategoryLine> LINES = Map.of(
            GAMES, new CategoryLine(GAMES, List.of()),
            ARCADE, new CategoryLine(ARCADE, List.of(GAMES)),
            DEMOSCENE, new CategoryLine(DEMOSCENE, List.of()));

    static CategorySets categorySets() {
        return new CategorySets(
                ids -> ids.stream().filter(LINES::containsKey).map(LINES::get).toList());
    }

    /** Categories of the tree of the archive, as storage gives them back. */
    static CategorySet in(CategoryId... ids) {
        return CategorySet.restore(Arrays.stream(ids).map(LINES::get).toList());
    }
}
