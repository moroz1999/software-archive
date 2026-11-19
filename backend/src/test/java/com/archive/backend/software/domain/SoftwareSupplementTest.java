package com.archive.backend.software.domain;

import static com.archive.backend.user.TestActors.BY_OTHER;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryLine;
import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.PlatformCode;
import java.time.Year;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

/** An import adds what the software lacks and never rewrites what it has (docs/domain/imports.md). */
class SoftwareSupplementTest {

    private static final HardwareSet NO_HARDWARE = HardwareSet.restore(new PlatformCode("vector"), Set.of());
    private static final SoftwareReference IVAN = reference(1);
    private static final SoftwareReference ZORK = reference(2);
    private static final CategorySet GAMES = CategorySet.restore(List.of(new CategoryLine(category(11), List.of())));
    private static final CategorySet GAMES_AND_DEMOS = CategorySet.restore(
            List.of(new CategoryLine(category(11), List.of()), new CategoryLine(category(12), List.of())));

    @Test
    void fillsTheDescriptionAndTheDateOnlyWhileTheyAreEmpty() {
        Software described = software(new SoftwareDescription("Ours"), ReleaseDate.ofYear(Year.of(1987)));
        Software empty = software(null, null);

        described.supplement(
                new SoftwareDescription("Theirs"), ReleaseDate.ofYear(Year.of(1990)), GAMES, SoftwareCredits.EMPTY);
        empty.supplement(
                new SoftwareDescription("Theirs"), ReleaseDate.ofYear(Year.of(1990)), GAMES, SoftwareCredits.EMPTY);

        assertThat(described.getDescription()).isEqualTo(new SoftwareDescription("Ours"));
        assertThat(described.getReleaseDate()).isEqualTo(ReleaseDate.ofYear(Year.of(1987)));
        assertThat(empty.getDescription()).isEqualTo(new SoftwareDescription("Theirs"));
        assertThat(empty.getReleaseDate()).isEqualTo(ReleaseDate.ofYear(Year.of(1990)));
    }

    @Test
    void creditsOnlyWhoIsNotCreditedYetAndKeepsTheRolesThere() {
        Software software = software(null, null);

        software.supplement(
                null,
                null,
                GAMES_AND_DEMOS,
                SoftwareCredits.restore(List.of(
                        new SoftwareCreditDraft(IVAN, List.of("unknown")),
                        new SoftwareCreditDraft(ZORK, List.of("unknown")))));

        assertThat(software.getCredits().all())
                .containsExactly(
                        new SoftwareCreditDraft(IVAN, List.of("code")),
                        new SoftwareCreditDraft(ZORK, List.of("unknown")));
        assertThat(software.getCategories()).isEqualTo(GAMES_AND_DEMOS);
    }

    private static Software software(@Nullable SoftwareDescription description, @Nullable ReleaseDate date) {
        Software software = Software.create(
                new SoftwareId(UUID.randomUUID()),
                new SoftwareTitle("Elite"),
                description,
                date,
                NO_HARDWARE,
                GAMES,
                SoftwareCredits.restore(List.of(new SoftwareCreditDraft(IVAN, List.of("code")))),
                BY_OTHER);
        software.pullEvents();
        return software;
    }

    private static SoftwareReference reference(int n) {
        return new SoftwareReference(
                SoftwareReferenceType.AUTHOR_LABEL,
                new SoftwareReferenceId(UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n))));
    }

    private static CategoryId category(int n) {
        return new CategoryId(UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n)));
    }
}
