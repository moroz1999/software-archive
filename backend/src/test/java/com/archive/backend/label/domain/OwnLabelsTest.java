package com.archive.backend.label.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OwnLabelsTest {

    private static final LabelId ZED = new LabelId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final LabelId AXE = new LabelId(UUID.fromString("00000000-0000-0000-0000-000000000002"));

    private static final OwnLabels ZED_AND_AXE = OwnLabels.restore(
            List.of(new OwnLabel(ZED, new LabelName("Zed")), new OwnLabel(AXE, new LabelName("Axe"))), ZED);

    @Test
    void givesNewLabelsIdsOfTheirOwn() {
        OwnLabels labels = OwnLabels.of(List.of(
                new LabelDraft(null, new LabelName("Zed"), true), new LabelDraft(null, new LabelName("Axe"), false)));

        assertThat(labels.all()).extracting(OwnLabel::name).containsExactly(new LabelName("Zed"), new LabelName("Axe"));
        assertThat(labels.all().get(0).id()).isNotEqualTo(labels.all().get(1).id());
        assertThat(labels.display()).isEqualTo(labels.all().get(0).id());
    }

    @Test
    void hasADisplayNameOnlyWithLabels() {
        assertThat(OwnLabels.of(List.of()).display()).isNull();
    }

    @Test
    void needsADisplayName() {
        assertThatThrownBy(() -> OwnLabels.of(List.of(new LabelDraft(null, new LabelName("Zed"), false))))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("label.display-one");
                    assertThat(e.field()).isEqualTo("labels");
                });
    }

    @Test
    void renamesAndMarksItsOwnLabels() {
        OwnLabels labels = ZED_AND_AXE.replace(List.of(
                new LabelDraft(ZED, new LabelName("Zed II"), false), new LabelDraft(AXE, new LabelName("Axe"), true)));

        assertThat(labels.all())
                .containsExactly(new OwnLabel(ZED, new LabelName("Zed II")), new OwnLabel(AXE, new LabelName("Axe")));
        assertThat(labels.display()).isEqualTo(AXE);
    }

    @Test
    void dropsTheLabelsTheDraftsNoLongerHave() {
        OwnLabels labels = ZED_AND_AXE.replace(List.of(new LabelDraft(AXE, new LabelName("Axe"), true)));

        assertThat(labels.all()).containsExactly(new OwnLabel(AXE, new LabelName("Axe")));
        assertThat(labels.display()).isEqualTo(AXE);
    }

    @Test
    void refusesALabelThatIsNotItsOwn() {
        assertThatThrownBy(() -> ZED_AND_AXE.replace(
                        List.of(new LabelDraft(new LabelId(UUID.randomUUID()), new LabelName("Bob"), true))))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("label.not-own");
                    assertThat(e.field()).isEqualTo("labels");
                });
    }

    @Test
    void hasOneDisplayNameOnly() {
        assertThatThrownBy(() -> OwnLabels.of(List.of(
                        new LabelDraft(null, new LabelName("Zed"), true),
                        new LabelDraft(null, new LabelName("Axe"), true))))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("label.display-one");
                    assertThat(e.field()).isEqualTo("labels");
                });
    }

    @Test
    void cannotMarkALabelItDoesNotHave() {
        assertThatThrownBy(() -> OwnLabels.restore(List.of(new OwnLabel(ZED, new LabelName("Zed"))), AXE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cannotBeRestoredWithoutTheDisplayName() {
        assertThatThrownBy(() -> OwnLabels.restore(List.of(new OwnLabel(ZED, new LabelName("Zed"))), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void knowsWhetherItHasAnyLabel() {
        assertThat(OwnLabels.of(List.of()).isEmpty()).isTrue();
        assertThat(ZED_AND_AXE.isEmpty()).isFalse();
    }
}
