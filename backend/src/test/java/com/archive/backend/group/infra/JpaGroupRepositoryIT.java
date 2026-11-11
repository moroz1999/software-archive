package com.archive.backend.group.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.Location;
import com.archive.backend.geo.domain.Locations;
import com.archive.backend.geo.domain.Place;
import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupName;
import com.archive.backend.group.domain.GroupNotes;
import com.archive.backend.group.domain.GroupTypeCode;
import com.archive.backend.group.domain.GroupTypeSet;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.OwnLabel;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaGroupRepository.class, GroupMapperImpl.class})
class JpaGroupRepositoryIT {

    private static final GroupId ID = new GroupId(id(1));
    private static final CountryId NORWAY = new CountryId(id(22));
    private static final UUID OTHER = id(41);
    private static final UUID ELITE = id(51);
    private static final GroupTypeSet DEMOGROUP_AND_LABEL =
            GroupTypeSet.restore(Set.of(new GroupTypeCode("demogroup"), new GroupTypeCode("label")));
    private static final Locations IN_NORWAY = new Locations(List.of(new Location(Place.restore(NORWAY, null), true)));

    @Autowired
    private JpaGroupRepository repository;

    @Autowired
    private SpringDataGroupRepository springDataGroupRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void insertAround() {
        jdbc.update("INSERT INTO country (id, name) VALUES (?, 'Norway')", NORWAY.value());
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Other')", OTHER);
        jdbc.update("INSERT INTO software (id, title, platform_code) VALUES (?, 'Elite', 'vector')", ELITE);
    }

    /** Stores Crew Ltd with the labels Crew (display name) and CRW, and reads it back from an empty persistence context. */
    private Group storeCrew() {
        repository.save(Group.create(
                ID,
                new GroupName("Crew Ltd"),
                new GroupNotes("Since 1990"),
                DEMOGROUP_AND_LABEL,
                List.of(
                        new LabelDraft(null, new LabelName("Crew"), true),
                        new LabelDraft(null, new LabelName("CRW"), false)),
                IN_NORWAY));
        return reload();
    }

    private Group reload() {
        springDataGroupRepository.flush();
        // Every request starts with an empty persistence context.
        entityManager.clear();
        return repository.findById(ID).orElseThrow();
    }

    private LabelId label(Group group, String name) {
        return group.getLabels().all().stream()
                .filter(label -> label.name().value().equals(name))
                .findFirst()
                .orElseThrow()
                .id();
    }

    private int count(String sql, Object... args) {
        Integer count = jdbc.queryForObject(sql, Integer.class, args);
        return count == null ? 0 : count;
    }

    @Test
    void storesANewGroupWithItsTypesLabelsAndLocations() {
        Group group = storeCrew();

        assertThat(group.getName()).isEqualTo(new GroupName("Crew Ltd"));
        assertThat(group.getNotes()).isEqualTo(new GroupNotes("Since 1990"));
        assertThat(group.getTypes()).isEqualTo(DEMOGROUP_AND_LABEL);
        assertThat(group.getLabels().all())
                .extracting(OwnLabel::name)
                .containsExactlyInAnyOrder(new LabelName("Crew"), new LabelName("CRW"));
        assertThat(group.getLabels().display()).isEqualTo(label(group, "Crew"));
        assertThat(group.getLocations()).isEqualTo(IN_NORWAY);
        assertThat(count(
                        "SELECT count(*) FROM entity_location WHERE entity_type = 'GROUP' AND entity_id = ?",
                        ID.value()))
                .isOne();
    }

    @Test
    void storesEdits() {
        Group group = storeCrew();
        LabelId crw = label(group, "CRW");

        group.edit(
                new GroupName("Crew"),
                null,
                GroupTypeSet.restore(Set.of(new GroupTypeCode("crack group"))),
                List.of(new LabelDraft(crw, new LabelName("CRW!"), true)),
                new Locations(List.of()));
        repository.save(group);
        Group edited = reload();

        assertThat(edited.getName()).isEqualTo(new GroupName("Crew"));
        assertThat(edited.getNotes()).isNull();
        assertThat(edited.getTypes().codes()).containsExactly(new GroupTypeCode("crack group"));
        assertThat(edited.getLabels().all()).containsExactly(new OwnLabel(crw, new LabelName("CRW!")));
        assertThat(edited.getLocations().all()).isEmpty();
    }

    @Test
    void leavesALabelTakenAwayAsALabelOfNobodyWithItsCreditsAndMemberships() {
        Group group = storeCrew();
        LabelId crw = label(group, "CRW");
        jdbc.update("INSERT INTO group_member (group_id, label_id) VALUES (?, ?)", OTHER, crw.value());
        jdbc.update("INSERT INTO software_contribution (software_id, label_id) VALUES (?, ?)", ELITE, crw.value());

        group.edit(
                group.getName(),
                null,
                group.getTypes(),
                List.of(new LabelDraft(label(group, "Crew"), new LabelName("Crew"), true)),
                IN_NORWAY);
        repository.save(group);
        reload();

        assertThat(count(
                        "SELECT count(*) FROM label WHERE id = ? AND person_id IS NULL AND group_id IS NULL",
                        crw.value()))
                .isOne();
        assertThat(count("SELECT count(*) FROM group_member WHERE label_id = ?", crw.value()))
                .isOne();
        assertThat(count("SELECT count(*) FROM software_contribution WHERE label_id = ?", crw.value()))
                .isOne();
    }

    @Test
    void deletesTheGroupWithItsTypesLocationsAndMembershipsLeavingItsLabelsAsLabelsOfNobody() {
        Group group = storeCrew();
        LabelId crew = label(group, "Crew");
        jdbc.update("INSERT INTO software_contribution (software_id, label_id) VALUES (?, ?)", ELITE, crew.value());
        jdbc.update(
                "INSERT INTO group_member_group (group_id, member_group_id) VALUES (?, ?), (?, ?)",
                ID.value(),
                OTHER,
                OTHER,
                ID.value());
        jdbc.update(
                "INSERT INTO entity_location (entity_type, entity_id, country_id) VALUES ('PERSON', ?, ?)",
                ID.value(),
                NORWAY.value());

        repository.delete(group);
        springDataGroupRepository.flush();

        assertThat(count("SELECT count(*) FROM \"group\" WHERE id = ?", ID.value()))
                .isZero();
        assertThat(count("SELECT count(*) FROM group_group_type WHERE group_id = ?", ID.value()))
                .isZero();
        assertThat(
                        count(
                                "SELECT count(*) FROM label WHERE group_id IS NULL AND person_id IS NULL AND name IN ('Crew', 'CRW')"))
                .isEqualTo(2);
        assertThat(count("SELECT count(*) FROM software_contribution WHERE label_id = ?", crew.value()))
                .isOne();
        assertThat(count("SELECT count(*) FROM group_member_group WHERE ? IN (group_id, member_group_id)", ID.value()))
                .isZero();
        assertThat(count(
                        "SELECT count(*) FROM entity_location WHERE entity_type = 'GROUP' AND entity_id = ?",
                        ID.value()))
                .isZero();
        assertThat(count(
                        "SELECT count(*) FROM entity_location WHERE entity_type = 'PERSON' AND entity_id = ?",
                        ID.value()))
                .isOne();
    }

    @Test
    void marksTheMainLabelAndAPrimaryLocationOfARecordFromBeforeTheRule() {
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew Ltd')", ID.value());
        jdbc.update("INSERT INTO group_group_type (group_id, group_type_code) VALUES (?, 'demogroup')", ID.value());
        jdbc.update(
                "INSERT INTO label (id, name, group_id) VALUES (?, 'Zeta', ?), (?, 'alpha', ?)",
                id(61),
                ID.value(),
                id(62),
                ID.value());
        jdbc.update(
                "INSERT INTO entity_location (entity_type, entity_id, country_id) VALUES ('GROUP', ?, ?)",
                ID.value(),
                NORWAY.value());

        Group group = repository.findById(ID).orElseThrow();

        assertThat(group.getLabels().display()).isEqualTo(new LabelId(id(62)));
        assertThat(group.getLocations()).isEqualTo(IN_NORWAY);
    }

    @Test
    void findsNothingForAnUnknownId() {
        assertThat(repository.findById(ID)).isEmpty();
    }

    private static UUID id(int n) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n));
    }
}
