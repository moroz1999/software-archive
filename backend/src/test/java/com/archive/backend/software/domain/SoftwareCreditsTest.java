package com.archive.backend.software.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SoftwareCreditsTest {

    private static final SoftwareReference CREW = new SoftwareReference(
            SoftwareReferenceType.GROUP,
            new SoftwareReferenceId(UUID.fromString("00000000-0000-0000-0000-000000000001")));
    private static final SoftwareReference CRW = new SoftwareReference(
            SoftwareReferenceType.GROUP_LABEL,
            new SoftwareReferenceId(UUID.fromString("00000000-0000-0000-0000-000000000002")));

    private static SoftwareCreditSets catalogOf(SoftwareReference... known) {
        Set<SoftwareReference> references = Set.of(known);
        return new SoftwareCreditSets(references::contains);
    }

    @Test
    void creditsGroupsAndTheirLabelsAsDevelopersAndPublishers() {
        SoftwareCredits credits = catalogOf(CREW, CRW)
                .of(List.of(
                        new SoftwareCreditDraft(CREW, List.of("developer")),
                        new SoftwareCreditDraft(CRW, List.of("developer", "publisher"))));

        assertThat(credits.all())
                .containsExactly(
                        new SoftwareCreditDraft(CREW, List.of("developer")),
                        new SoftwareCreditDraft(CRW, List.of("developer", "publisher")));
    }

    @Test
    void takesACreditWithoutRolesAsOfAnUnknownRole() {
        assertThat(new SoftwareCreditDraft(CREW, List.of()).roles()).containsExactly("unknown");
    }

    @Test
    void refusesAGroupThatIsNotInTheArchive() {
        SoftwareCreditSets sets = catalogOf(CRW);

        assertThatThrownBy(() -> sets.of(List.of(new SoftwareCreditDraft(CREW, List.of("developer")))))
                .isInstanceOf(SoftwareCreditUnknownException.class);
    }

    @Test
    void creditsAGroupOnlyOnce() {
        SoftwareCreditSets sets = catalogOf(CREW);

        assertThatThrownBy(() -> sets.of(List.of(
                        new SoftwareCreditDraft(CREW, List.of("developer")),
                        new SoftwareCreditDraft(CREW, List.of("publisher")))))
                .isInstanceOf(InvalidValueException.class)
                .hasMessageContaining("only once");
    }
}
