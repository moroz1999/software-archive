package com.archive.backend.label.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** The labels of a group: its alternative names, none of them its display name (docs/domain/authors.md). */
class UnmarkedLabelsTest {

    private static final LabelId CREW = new LabelId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final LabelId CRW = new LabelId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    private static final LabelId SOL = new LabelId(UUID.fromString("00000000-0000-0000-0000-000000000003"));

    private static final UnmarkedLabels CREW_AND_CRW = UnmarkedLabels.restore(
            List.of(new OwnLabel(CREW, new LabelName("Crew")), new OwnLabel(CRW, new LabelName("CRW"))));

    @Test
    void givesNewLabelsIdsOfTheirOwn() {
        UnmarkedLabels labels = UnmarkedLabels.of(List.of(
                new UnmarkedLabelDraft(null, new LabelName("Crew")),
                new UnmarkedLabelDraft(null, new LabelName("CRW"))));

        assertThat(labels.all())
                .extracting(OwnLabel::name)
                .containsExactly(new LabelName("Crew"), new LabelName("CRW"));
        assertThat(labels.all().get(0).id()).isNotEqualTo(labels.all().get(1).id());
    }

    @Test
    void renamesItsOwnLabelsAndDropsTheOnesTheDraftsNoLongerHave() {
        UnmarkedLabels labels = CREW_AND_CRW.replace(List.of(new UnmarkedLabelDraft(CRW, new LabelName("CRW!"))));

        assertThat(labels.all()).containsExactly(new OwnLabel(CRW, new LabelName("CRW!")));
    }

    @Test
    void refusesALabelThatIsNotItsOwn() {
        assertThatThrownBy(() -> CREW_AND_CRW.replace(
                        List.of(new UnmarkedLabelDraft(new LabelId(UUID.randomUUID()), new LabelName("Bob")))))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("label.not-own");
                    assertThat(e.field()).isEqualTo("labels");
                });
    }

    @Test
    void adoptsALabelOfNobodyEvenAsTheFirstOne() {
        UnmarkedLabels labels = UnmarkedLabels.of(List.of()).adopt(new OwnLabel(SOL, new LabelName("Sol")));

        assertThat(labels.all()).containsExactly(new OwnLabel(SOL, new LabelName("Sol")));
    }

    @Test
    void knowsWhetherItHasAnyLabel() {
        assertThat(UnmarkedLabels.of(List.of()).isEmpty()).isTrue();
        assertThat(CREW_AND_CRW.isEmpty()).isFalse();
    }

    @Test
    void releasesALabel() {
        assertThat(CREW_AND_CRW.release(CREW).all()).containsExactly(new OwnLabel(CRW, new LabelName("CRW")));
    }
}
