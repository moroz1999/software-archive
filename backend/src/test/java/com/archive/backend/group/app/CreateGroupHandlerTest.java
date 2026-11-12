package com.archive.backend.group.app;

import static com.archive.backend.group.app.GroupFixtures.DEMOGROUP;
import static com.archive.backend.group.app.GroupFixtures.LABEL;
import static com.archive.backend.group.app.GroupFixtures.NORWAY;
import static com.archive.backend.group.app.GroupFixtures.groupTypes;
import static com.archive.backend.group.app.GroupFixtures.places;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.geo.domain.Location;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.geo.domain.Place;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupName;
import com.archive.backend.group.domain.GroupNotes;
import com.archive.backend.group.domain.GroupTypeCode;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.OwnLabel;
import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CreateGroupHandlerTest {

    private final InMemoryGroupRepository repository = new InMemoryGroupRepository();
    private final CreateGroupHandler handler = new CreateGroupHandler(repository, groupTypes(), places(), event -> {});

    @Test
    void storesANewGroupUnderANewId() {
        GroupId id = handler.handle(new CreateGroupCommand(
                new GroupName("Crew Ltd"),
                new GroupNotes("Since 1990"),
                Set.of(DEMOGROUP, LABEL),
                List.of(new LabelDraft(null, new LabelName("CRW"), true)),
                List.of(new LocationDraft(NORWAY, null, true))));

        assertThat(repository.findById(id)).hasValueSatisfying(group -> {
            assertThat(group.getName()).isEqualTo(new GroupName("Crew Ltd"));
            assertThat(group.getNotes()).isEqualTo(new GroupNotes("Since 1990"));
            assertThat(group.getTypes().codes()).containsExactlyInAnyOrder(DEMOGROUP, LABEL);
            assertThat(group.getLabels().all()).extracting(OwnLabel::name).containsExactly(new LabelName("CRW"));
            assertThat(group.getLocations().all()).containsExactly(new Location(Place.restore(NORWAY, null), true));
        });
    }

    @Test
    void givesEveryGroupItsOwnId() {
        CreateGroupCommand command =
                new CreateGroupCommand(new GroupName("Crew Ltd"), null, Set.of(DEMOGROUP), List.of(), List.of());

        assertThat(handler.handle(command)).isNotEqualTo(handler.handle(command));
    }

    @Test
    void refusesATypeOutsideTheReferenceBookAndStoresNothing() {
        assertThatThrownBy(() -> handler.handle(new CreateGroupCommand(
                        new GroupName("Crew Ltd"), null, Set.of(new GroupTypeCode("orchestra")), List.of(), List.of())))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("group.type-unknown"));
        assertThat(repository.count()).isZero();
    }

    @Test
    void refusesAGroupWithoutTypesAndStoresNothing() {
        assertThatThrownBy(() -> handler.handle(
                        new CreateGroupCommand(new GroupName("Crew Ltd"), null, Set.of(), List.of(), List.of())))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("group.types-required"));
        assertThat(repository.count()).isZero();
    }
}
