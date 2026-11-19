package com.archive.backend.group.app;

import static com.archive.backend.group.app.GroupFixtures.CREW;
import static com.archive.backend.group.app.GroupFixtures.CRW;
import static com.archive.backend.group.app.GroupFixtures.DEMOGROUP;
import static com.archive.backend.group.app.GroupFixtures.LABEL;
import static com.archive.backend.group.app.GroupFixtures.NORWAY;
import static com.archive.backend.group.app.GroupFixtures.crew;
import static com.archive.backend.group.app.GroupFixtures.groupTypes;
import static com.archive.backend.group.app.GroupFixtures.places;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.Location;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.geo.domain.Place;
import com.archive.backend.group.domain.GroupMemberSets;
import com.archive.backend.group.domain.GroupName;
import com.archive.backend.group.domain.GroupNotes;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.OwnLabel;
import com.archive.backend.label.domain.UnmarkedLabelDraft;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import com.archive.backend.user.domain.ChangeForbiddenException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UpdateGroupHandlerTest {

    private final InMemoryGroupRepository repository = new InMemoryGroupRepository();
    private final UpdateGroupHandler handler = new UpdateGroupHandler(
            repository, groupTypes(), places(), new GroupMemberSets(reference -> true), event -> {});

    @Test
    void storesTheEditedGroup() {
        repository.save(crew());

        handler.handle(new UpdateGroupCommand(
                ADMIN,
                CREW,
                new GroupName("Crew"),
                new GroupNotes("Disbanded"),
                Set.of(LABEL),
                List.of(
                        new UnmarkedLabelDraft(CRW, new LabelName("CRW!")),
                        new UnmarkedLabelDraft(null, new LabelName("Crew"))),
                List.of(new LocationDraft(NORWAY, null, true))));

        assertThat(repository.findById(CREW)).hasValueSatisfying(group -> {
            assertThat(group.getName()).isEqualTo(new GroupName("Crew"));
            assertThat(group.getNotes()).isEqualTo(new GroupNotes("Disbanded"));
            assertThat(group.getTypes().codes()).containsExactly(LABEL);
            assertThat(group.getLabels().all())
                    .extracting(OwnLabel::name)
                    .containsExactly(new LabelName("CRW!"), new LabelName("Crew"));
            assertThat(group.getLabels().all().get(0).id()).isEqualTo(CRW);
            assertThat(group.getLocations().all()).containsExactly(new Location(Place.restore(NORWAY, null), true));
        });
    }

    @Test
    void failsWhenTheGroupDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new UpdateGroupCommand(
                        ADMIN, CREW, new GroupName("Crew"), null, Set.of(DEMOGROUP), List.of(), List.of())))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("group.not-found"));
    }

    @Test
    void refusesAnUnknownCountryAndKeepsTheGroup() {
        repository.save(crew());

        assertThatThrownBy(() -> handler.handle(new UpdateGroupCommand(
                        ADMIN,
                        CREW,
                        new GroupName("Crew"),
                        null,
                        Set.of(DEMOGROUP),
                        List.of(new UnmarkedLabelDraft(CRW, new LabelName("CRW"))),
                        List.of(new LocationDraft(new CountryId(UUID.randomUUID()), null, true)))))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("location.unknown"));
        assertThat(repository.findById(CREW))
                .hasValueSatisfying(group -> assertThat(group.getName()).isEqualTo(new GroupName("Crew Ltd")));
    }

    @Test
    void leavesTheGroupOfNoKnownType() {
        repository.save(crew());

        handler.handle(
                new UpdateGroupCommand(ADMIN, CREW, new GroupName("Crew"), null, Set.of(), List.of(), List.of()));

        assertThat(repository.findById(CREW))
                .hasValueSatisfying(
                        group -> assertThat(group.getTypes().codes()).isEmpty());
    }

    @Test
    void refusesSomeoneWhoMayNotChangeTheGroupAndKeepsIt() {
        repository.save(crew());

        assertThatThrownBy(() -> handler.handle(new UpdateGroupCommand(
                        MEMBER, CREW, new GroupName("Crew"), null, Set.of(LABEL), List.of(), List.of())))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThat(repository.findById(CREW))
                .hasValueSatisfying(group -> assertThat(group.getName()).isNotEqualTo(new GroupName("Crew")));
    }
}
