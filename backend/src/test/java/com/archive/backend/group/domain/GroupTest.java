package com.archive.backend.group.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.Location;
import com.archive.backend.geo.domain.Locations;
import com.archive.backend.geo.domain.Place;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.OwnLabel;
import com.archive.backend.label.domain.OwnLabels;
import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GroupTest {

    private static final GroupId ID = new GroupId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final LabelId CRW = new LabelId(UUID.fromString("00000000-0000-0000-0000-000000000011"));
    private static final GroupTypeSet DEMOGROUP = GroupTypeSet.restore(Set.of(new GroupTypeCode("demogroup")));
    private static final GroupTypeSet LABEL = GroupTypeSet.restore(Set.of(new GroupTypeCode("label")));
    private static final Locations NOWHERE = new Locations(List.of());
    private static final Locations NORWAY =
            new Locations(List.of(new Location(Place.restore(new CountryId(UUID.randomUUID()), null), true)));

    private static Group crew() {
        return new Group(
                ID,
                new GroupName("Crew Ltd"),
                null,
                DEMOGROUP,
                OwnLabels.restore(List.of(new OwnLabel(CRW, new LabelName("CRW"))), CRW),
                NOWHERE);
    }

    @Test
    void isCreatedWithItsNameNotesTypesLabelsAndLocations() {
        Group group = Group.create(
                ID,
                new GroupName("Crew Ltd"),
                new GroupNotes("Since 1990"),
                DEMOGROUP,
                List.of(new LabelDraft(null, new LabelName("CRW"), true)),
                NORWAY);

        assertThat(group.getId()).isEqualTo(ID);
        assertThat(group.getName()).isEqualTo(new GroupName("Crew Ltd"));
        assertThat(group.getNotes()).isEqualTo(new GroupNotes("Since 1990"));
        assertThat(group.getTypes()).isEqualTo(DEMOGROUP);
        assertThat(group.getLabels().all()).extracting(OwnLabel::name).containsExactly(new LabelName("CRW"));
        assertThat(group.getLocations()).isEqualTo(NORWAY);
    }

    @Test
    void mayHaveNoLabels() {
        assertThat(Group.create(ID, new GroupName("Crew Ltd"), null, DEMOGROUP, List.of(), NOWHERE)
                        .getLabels()
                        .isEmpty())
                .isTrue();
    }

    @Test
    void editingReplacesEverythingKeepingTheIdsOfItsLabels() {
        Group group = crew();

        group.edit(
                new GroupName("Crew"),
                new GroupNotes("Disbanded"),
                LABEL,
                List.of(new LabelDraft(CRW, new LabelName("CRW!"), true)),
                NORWAY);

        assertThat(group.getName()).isEqualTo(new GroupName("Crew"));
        assertThat(group.getNotes()).isEqualTo(new GroupNotes("Disbanded"));
        assertThat(group.getTypes()).isEqualTo(LABEL);
        assertThat(group.getLabels().all()).containsExactly(new OwnLabel(CRW, new LabelName("CRW!")));
        assertThat(group.getLocations()).isEqualTo(NORWAY);
    }

    @Test
    void refusesALabelOfSomebodyElseAndKeepsItsState() {
        Group group = crew();

        assertThatThrownBy(() -> group.edit(
                        new GroupName("Crew"),
                        null,
                        LABEL,
                        List.of(new LabelDraft(new LabelId(UUID.randomUUID()), new LabelName("Bob"), true)),
                        NORWAY))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("label.not-own"));
        assertThat(group.getName()).isEqualTo(new GroupName("Crew Ltd"));
        assertThat(group.getTypes()).isEqualTo(DEMOGROUP);
        assertThat(group.getLabels().all()).extracting(OwnLabel::id).containsExactly(CRW);
    }
}
