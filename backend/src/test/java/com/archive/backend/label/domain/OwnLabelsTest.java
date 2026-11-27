package com.archive.backend.label.domain;

import static com.archive.backend.user.TestActors.BY_OTHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OwnLabelsTest {

    private static final LabelId ZED = new LabelId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final LabelId AXE = new LabelId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    private static final LabelId BOB = new LabelId(UUID.fromString("00000000-0000-0000-0000-000000000003"));

    private static final OwnLabels ZED_AND_AXE = OwnLabels.restore(
            List.of(
                    new OwnLabel(ZED, new LabelName("Zed"), BY_OTHER),
                    new OwnLabel(AXE, new LabelName("Axe"), BY_OTHER)),
            ZED);

    @Test
    void givesNewLabelsIdsOfTheirOwn() {
        OwnLabels labels = OwnLabels.of(
                List.of(
                        new LabelDraft(null, new LabelName("Zed"), true),
                        new LabelDraft(null, new LabelName("Axe"), false)),
                BY_OTHER);

        assertThat(labels.all()).extracting(OwnLabel::name).containsExactly(new LabelName("Zed"), new LabelName("Axe"));
        assertThat(labels.all().get(0).id()).isNotEqualTo(labels.all().get(1).id());
        assertThat(labels.display()).isEqualTo(labels.all().get(0).id());
    }

    @Test
    void hasADisplayNameOnlyWithLabels() {
        assertThat(OwnLabels.of(List.of(), BY_OTHER).display()).isNull();
    }

    @Test
    void allowsLabelsWithoutADisplayName() {
        OwnLabels labels = OwnLabels.of(List.of(new LabelDraft(null, new LabelName("Zed"), false)), BY_OTHER);

        assertThat(labels.all()).extracting(OwnLabel::name).containsExactly(new LabelName("Zed"));
        assertThat(labels.display()).isNull();
    }

    @Test
    void clearsTheDisplayNameWhileKeepingAllLabels() {
        OwnLabels labels = ZED_AND_AXE.replace(
                List.of(
                        new LabelDraft(ZED, new LabelName("Zed"), false),
                        new LabelDraft(AXE, new LabelName("Axe"), false)),
                BY_OTHER);

        assertThat(labels.all()).containsExactlyElementsOf(ZED_AND_AXE.all());
        assertThat(labels.display()).isNull();
    }

    @Test
    void renamesAndMarksItsOwnLabels() {
        OwnLabels labels = ZED_AND_AXE.replace(
                List.of(
                        new LabelDraft(ZED, new LabelName("Zed II"), false),
                        new LabelDraft(AXE, new LabelName("Axe"), true)),
                BY_OTHER);

        assertThat(labels.all())
                .containsExactly(
                        new OwnLabel(ZED, new LabelName("Zed II"), BY_OTHER),
                        new OwnLabel(AXE, new LabelName("Axe"), BY_OTHER));
        assertThat(labels.display()).isEqualTo(AXE);
    }

    @Test
    void dropsTheLabelsTheDraftsNoLongerHave() {
        OwnLabels labels = ZED_AND_AXE.replace(List.of(new LabelDraft(AXE, new LabelName("Axe"), true)), BY_OTHER);

        assertThat(labels.all()).containsExactly(new OwnLabel(AXE, new LabelName("Axe"), BY_OTHER));
        assertThat(labels.display()).isEqualTo(AXE);
    }

    @Test
    void refusesALabelThatIsNotItsOwn() {
        assertThatThrownBy(() -> ZED_AND_AXE.replace(
                        List.of(new LabelDraft(new LabelId(UUID.randomUUID()), new LabelName("Bob"), true)), BY_OTHER))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("label.not-own");
                    assertThat(e.field()).isEqualTo("labels");
                });
    }

    @Test
    void hasOneDisplayNameOnly() {
        assertThatThrownBy(() -> OwnLabels.of(
                        List.of(
                                new LabelDraft(null, new LabelName("Zed"), true),
                                new LabelDraft(null, new LabelName("Axe"), true)),
                        BY_OTHER))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("label.display-one");
                    assertThat(e.field()).isEqualTo("labels");
                });
    }

    @Test
    void cannotMarkALabelItDoesNotHave() {
        assertThatThrownBy(() -> OwnLabels.restore(List.of(new OwnLabel(ZED, new LabelName("Zed"), BY_OTHER)), AXE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void restoresLabelsWithoutADisplayName() {
        OwnLabel zed = new OwnLabel(ZED, new LabelName("Zed"), BY_OTHER);
        OwnLabels labels = OwnLabels.restore(List.of(zed), null);

        assertThat(labels.all()).containsExactly(zed);
        assertThat(labels.display()).isNull();
    }

    @Test
    void adoptsALabelOfNobodyBesideItsDisplayName() {
        OwnLabel bob = new OwnLabel(BOB, new LabelName("Bob"), BY_OTHER);

        OwnLabels labels = ZED_AND_AXE.adopt(bob, false);

        assertThat(labels.all())
                .containsExactly(
                        new OwnLabel(ZED, new LabelName("Zed"), BY_OTHER),
                        new OwnLabel(AXE, new LabelName("Axe"), BY_OTHER),
                        bob);
        assertThat(labels.display()).isEqualTo(ZED);
    }

    @Test
    void adoptsALabelOfNobodyAsItsDisplayNameWhenToldTo() {
        OwnLabels labels = ZED_AND_AXE.adopt(new OwnLabel(BOB, new LabelName("Bob"), BY_OTHER), true);

        assertThat(labels.all()).hasSize(3);
        assertThat(labels.display()).isEqualTo(BOB);
    }

    @Test
    void adoptsItsFirstLabelWithoutADisplayName() {
        OwnLabel bob = new OwnLabel(BOB, new LabelName("Bob"), BY_OTHER);

        OwnLabels labels = OwnLabels.of(List.of(), BY_OTHER).adopt(bob, false);

        assertThat(labels.all()).containsExactly(bob);
        assertThat(labels.display()).isNull();
    }

    @Test
    void knowsWhetherItHasAnyLabel() {
        assertThat(OwnLabels.of(List.of(), BY_OTHER).isEmpty()).isTrue();
        assertThat(ZED_AND_AXE.isEmpty()).isFalse();
    }

    @Test
    void releasesALabelBesideTheDisplayName() {
        OwnLabels labels = ZED_AND_AXE.release(AXE);

        assertThat(labels.all()).containsExactly(new OwnLabel(ZED, new LabelName("Zed"), BY_OTHER));
        assertThat(labels.display()).isEqualTo(ZED);
    }

    @Test
    void releasesItsOnlyLabelLeavingNoDisplayName() {
        OwnLabels labels = OwnLabels.restore(List.of(new OwnLabel(ZED, new LabelName("Zed"), BY_OTHER)), ZED)
                .release(ZED);

        assertThat(labels.isEmpty()).isTrue();
        assertThat(labels.display()).isNull();
    }

    @Test
    void releasesItsDisplayNameWithoutMarkingAnotherLabel() {
        OwnLabels labels = ZED_AND_AXE.release(ZED);

        assertThat(labels.all()).containsExactly(new OwnLabel(AXE, new LabelName("Axe"), BY_OTHER));
        assertThat(labels.display()).isNull();
    }
}
