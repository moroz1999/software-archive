package com.archive.backend.author.infra;

import static com.archive.backend.geo.GeoTestRows.city;
import static com.archive.backend.geo.GeoTestRows.country;
import static com.archive.backend.user.TestActors.BY_OTHER;
import static com.archive.backend.user.TestActors.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.AuthorNotes;
import com.archive.backend.author.domain.RealName;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.Location;
import com.archive.backend.geo.domain.Locations;
import com.archive.backend.geo.domain.Place;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.OwnLabel;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.AddedBy;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
class JpaAuthorRepositoryIT {

    private static final AuthorId ID = new AuthorId(id(1));
    private static final CountryId FINLAND = new CountryId(id(21));
    private static final CountryId NORWAY = new CountryId(id(22));
    private static final CityId HELSINKI = new CityId(id(31));
    // A city whose country is not known.
    private static final CityId KOVROV = new CityId(id(32));
    private static final UUID CREW = id(41);
    private static final UUID ELITE = id(51);

    private static final Locations HELSINKI_AND_NORWAY = new Locations(List.of(
            new Location(Place.restore(FINLAND, HELSINKI), true), new Location(Place.restore(NORWAY, null), false)));

    @Autowired
    private JpaAuthorRepository repository;

    @Autowired
    private SpringDataAuthorRepository springDataAuthorRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void insertPlaces() {
        TestActors.store(jdbc);
        country(jdbc, FINLAND.value(), "Финляндия", "Finland");
        country(jdbc, NORWAY.value(), "Норвегия", "Norway");
        city(jdbc, HELSINKI.value(), FINLAND.value(), "Хельсинки", "Helsinki");
        city(jdbc, KOVROV.value(), null, "Ковров", null);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew')", CREW);
        jdbc.update("INSERT INTO software (id, title, platform_code) VALUES (?, 'Elite', 'vector')", ELITE);
    }

    /** Stores Zed with the labels Zed (display name) and Axe, and reads Zed back from an empty persistence context. */
    private Author storeZed() {
        repository.save(Author.create(
                ID,
                new RealName("John Doe"),
                new AuthorNotes("Coder since 1984"),
                List.of(
                        new LabelDraft(null, new LabelName("Zed"), true),
                        new LabelDraft(null, new LabelName("Axe"), false)),
                HELSINKI_AND_NORWAY,
                BY_OTHER));
        return reload();
    }

    private Author reload() {
        springDataAuthorRepository.flush();
        // Every request starts with an empty persistence context.
        entityManager.clear();
        return repository.findById(ID).orElseThrow();
    }

    private LabelId label(Author author, String name) {
        return author.getLabels().all().stream()
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
    void writesAtOnceForPlainSqlInTheSameTransaction() {
        repository.save(Author.create(ID, new RealName("John Doe"), null, List.of(), HELSINKI_AND_NORWAY, BY_OTHER));
        assertThat(count("SELECT count(*) FROM person WHERE id = ?", ID.value()))
                .isEqualTo(1);

        entityManager.clear();
        repository.delete(repository.findById(ID).orElseThrow());
        assertThat(count("SELECT count(*) FROM person WHERE id = ?", ID.value()))
                .isZero();
    }

    @Test
    void storesALocationInACityOfNoKnownCountryWithNoCountry() {
        Locations kovrov = new Locations(List.of(new Location(Place.restore(null, KOVROV), true)));
        repository.save(Author.create(ID, new RealName("John Doe"), null, List.of(), kovrov, BY_OTHER));
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.findById(ID).orElseThrow().getLocations()).isEqualTo(kovrov);
        assertThat(jdbc.queryForObject(
                        "SELECT country_id FROM entity_location WHERE entity_type = 'PERSON' AND entity_id = ?",
                        UUID.class,
                        ID.value()))
                .isNull();
    }

    @Test
    void storesANewAuthorWithItsLabelsAndLocations() {
        Author author = storeZed();

        assertThat(author.getRealName()).isEqualTo(new RealName("John Doe"));
        assertThat(author.getNotes()).isEqualTo(new AuthorNotes("Coder since 1984"));
        assertThat(author.getLabels().all())
                .extracting(OwnLabel::name)
                .containsExactlyInAnyOrder(new LabelName("Zed"), new LabelName("Axe"));
        assertThat(author.getLabels().display()).isEqualTo(label(author, "Zed"));
        assertThat(author.getLocations().all()).containsExactlyInAnyOrderElementsOf(HELSINKI_AND_NORWAY.all());
        assertThat(count(
                        "SELECT count(*) FROM entity_location WHERE entity_type = 'PERSON' AND entity_id = ?",
                        ID.value()))
                .isEqualTo(2);
    }

    @Test
    void storesEdits() {
        Author author = storeZed();
        LabelId zed = label(author, "Zed");

        author.edit(
                null,
                null,
                List.of(
                        new LabelDraft(zed, new LabelName("Zed II"), false),
                        new LabelDraft(null, new LabelName("Bob"), true)),
                new Locations(List.of(new Location(Place.restore(NORWAY, null), true))),
                BY_OTHER);
        repository.save(author);
        Author edited = reload();

        assertThat(edited.getRealName()).isNull();
        assertThat(edited.getNotes()).isNull();
        assertThat(label(edited, "Zed II")).isEqualTo(zed);
        assertThat(edited.getLabels().display()).isEqualTo(label(edited, "Bob"));
        assertThat(edited.getLocations().all()).containsExactly(new Location(Place.restore(NORWAY, null), true));
    }

    @Test
    void leavesALabelTakenAwayAsALabelOfNobodyWithItsCreditsAndMemberships() {
        Author author = storeZed();
        LabelId axe = label(author, "Axe");
        jdbc.update("INSERT INTO group_member (group_id, label_id) VALUES (?, ?)", CREW, axe.value());
        jdbc.update("INSERT INTO software_contribution (software_id, label_id) VALUES (?, ?)", ELITE, axe.value());

        author.edit(
                author.getRealName(),
                null,
                List.of(new LabelDraft(label(author, "Zed"), new LabelName("Zed"), true)),
                HELSINKI_AND_NORWAY,
                BY_OTHER);
        repository.save(author);
        reload();

        assertThat(count(
                        "SELECT count(*) FROM label WHERE id = ? AND person_id IS NULL AND group_id IS NULL",
                        axe.value()))
                .isOne();
        assertThat(count("SELECT count(*) FROM group_member WHERE label_id = ?", axe.value()))
                .isOne();
        assertThat(count("SELECT count(*) FROM software_contribution WHERE label_id = ?", axe.value()))
                .isOne();
    }

    @Test
    void deletesTheAuthorAndItsLocationsLeavingItsLabelsAsLabelsOfNobody() {
        Author author = storeZed();
        LabelId zed = label(author, "Zed");
        jdbc.update("INSERT INTO software_contribution (software_id, label_id) VALUES (?, ?)", ELITE, zed.value());
        jdbc.update(
                "INSERT INTO entity_location (entity_type, entity_id, country_id) VALUES ('GROUP', ?, ?)",
                ID.value(),
                NORWAY.value());

        repository.delete(author);
        springDataAuthorRepository.flush();

        assertThat(count("SELECT count(*) FROM person WHERE id = ?", ID.value()))
                .isZero();
        assertThat(count("SELECT count(*) FROM label WHERE person_id IS NULL AND name IN ('Zed', 'Axe')"))
                .isEqualTo(2);
        assertThat(count("SELECT count(*) FROM software_contribution WHERE label_id = ?", zed.value()))
                .isOne();
        assertThat(count(
                        "SELECT count(*) FROM entity_location WHERE entity_type = 'PERSON' AND entity_id = ?",
                        ID.value()))
                .isZero();
        assertThat(count(
                        "SELECT count(*) FROM entity_location WHERE entity_type = 'GROUP' AND entity_id = ?",
                        ID.value()))
                .isOne();
    }

    @Test
    void marksTheMainLabelAndAPrimaryLocationOfARecordFromBeforeTheRule() {
        jdbc.update("INSERT INTO person (id) VALUES (?)", ID.value());
        jdbc.update(
                "INSERT INTO label (id, name, person_id) VALUES (?, 'Zed', ?), (?, 'axe', ?)",
                id(61),
                ID.value(),
                id(62),
                ID.value());
        jdbc.update(
                "INSERT INTO entity_location (entity_type, entity_id, country_id) VALUES ('PERSON', ?, ?)",
                ID.value(),
                NORWAY.value());

        Author author = repository.findById(ID).orElseThrow();

        assertThat(author.getLabels().display()).isEqualTo(new LabelId(id(62)));
        assertThat(author.getLocations().all()).containsExactly(new Location(Place.restore(NORWAY, null), true));
    }

    @Test
    void readsANamedPersonOnly() {
        jdbc.update("INSERT INTO person (id) VALUES (?)", ID.value());

        assertThat(repository.findById(ID)).isEmpty();
    }

    @Test
    void findsNothingForAnUnknownId() {
        assertThat(repository.findById(ID)).isEmpty();
    }

    private static UUID id(int n) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n));
    }

    @Test
    void remembersWhoAddedThePerson() {
        repository.save(Author.create(
                ID, new RealName("John Doe"), null, List.of(), HELSINKI_AND_NORWAY, new AddedBy(MEMBER_ID)));

        assertThat(reload().getAddedBy()).isEqualTo(new AddedBy(MEMBER_ID));
    }
}
