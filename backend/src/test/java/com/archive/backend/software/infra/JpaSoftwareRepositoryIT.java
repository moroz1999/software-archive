package com.archive.backend.software.infra;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.ADMIN_ID;
import static com.archive.backend.user.TestActors.BY_OTHER;
import static com.archive.backend.user.TestActors.MEMBER_ID;
import static com.archive.backend.user.TestActors.OTHER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryLine;
import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.category.infra.JdbcCategoryCatalog;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.domain.Release;
import com.archive.backend.software.domain.ReleaseArchive;
import com.archive.backend.software.domain.ReleaseArchiveId;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.ReleaseDate;
import com.archive.backend.software.domain.ReleaseDescription;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ReleaseTitle;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.ReleaseVersion;
import com.archive.backend.software.domain.Screenshot;
import com.archive.backend.software.domain.ScreenshotFormat;
import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import com.archive.backend.software.domain.SoftwareCredits;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareReference;
import com.archive.backend.software.domain.SoftwareReferenceId;
import com.archive.backend.software.domain.SoftwareReferenceType;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.AddedBy;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.Year;
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
@Import({
    TestcontainersConfiguration.class,
    JpaSoftwareRepository.class,
    SoftwareMapperImpl.class,
    JdbcCategoryCatalog.class
})
class JpaSoftwareRepositoryIT {

    private static final SoftwareId ID = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final PlatformCode VECTOR = new PlatformCode("vector");
    private static final PlatformCode BK = new PlatformCode("bk");
    private static final ReleaseId ORIGINAL = new ReleaseId(UUID.fromString("00000000-0000-0000-0000-000000000011"));
    private static final ReleaseId AY_MOD = new ReleaseId(UUID.fromString("00000000-0000-0000-0000-000000000012"));
    private static final ReleaseContent AY_MOD_CONTENT = new ReleaseContent(
            new ReleaseTitle("Elite AY"),
            new ReleaseVersion("1.1"),
            ReleaseType.MOD,
            ReleaseDate.ofDay(LocalDate.of(1990, 3, 1)),
            new ReleaseDescription("With AY music"));
    private static final ReleaseContent ORIGINAL_CONTENT =
            new ReleaseContent(null, null, ReleaseType.ORIGINAL, null, null);
    /** Categories of the tree the schema brings. */
    private static final CategoryId GAMES = new CategoryId(UUID.fromString("081bf3a1-782b-5a8b-8d12-3b155e0a58cb"));

    private static final CategoryId INTRO_4K = new CategoryId(UUID.fromString("e9d495af-b0d1-5b0f-a22f-6955b423ab8d"));
    /** Games → Action. */
    private static final CategoryId ACTION = new CategoryId(UUID.fromString("93c89b73-d81b-5b8f-9318-6a52895305b3"));

    private static final CategoryLine GAMES_LINE = new CategoryLine(GAMES, List.of());
    private static final CategoryLine INTRO_4K_LINE = new CategoryLine(
            INTRO_4K,
            List.of(
                    new CategoryId(UUID.fromString("47301d2f-eb74-5c1c-90c3-090b4f33e498")),
                    new CategoryId(UUID.fromString("c35b7f9b-44a8-5570-9406-9e6dcb30b607"))));
    private static final HardwareSet NO_HARDWARE = HardwareSet.restore(VECTOR, Set.of());
    private static final HardwareSet AY_AND_JOYSTICK =
            HardwareSet.restore(VECTOR, Set.of(new HardwareCode("ay8910"), new HardwareCode("joystick")));

    @Autowired
    private SpringDataSoftwareRepository springDataSoftwareRepository;

    @Autowired
    private JpaSoftwareRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void storeActors() {
        TestActors.store(jdbc);
    }

    @Autowired
    private EntityManager entityManager;

    @Test
    void writesAtOnceForPlainSqlInTheSameTransaction() {
        repository.save(Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software WHERE id = ?", Integer.class, ID.value()))
                .isEqualTo(1);

        repository.delete(repository.findById(ID).orElseThrow());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software WHERE id = ?", Integer.class, ID.value()))
                .isZero();
    }

    @Test
    void storesNewSoftwareUnderItsId() {
        repository.save(Software.create(
                ID,
                new SoftwareTitle("Elite"),
                new SoftwareDescription("Space trading"),
                ReleaseDate.ofDay(LocalDate.of(1985, 9, 20)),
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER));
        springDataSoftwareRepository.flush();

        assertThat(repository.findById(ID)).hasValueSatisfying(software -> {
            assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite"));
            assertThat(software.getDescription()).isEqualTo(new SoftwareDescription("Space trading"));
            assertThat(software.getReleaseDate()).isEqualTo(ReleaseDate.ofDay(LocalDate.of(1985, 9, 20)));
            assertThat(software.getPlatform()).isEqualTo(VECTOR);
        });
        assertThat(jdbc.queryForObject(
                        "SELECT created_at IS NOT NULL FROM software WHERE id = ?", Boolean.class, ID.value()))
                .isTrue();
    }

    @Test
    void storesTheYearOfSoftwareAndOfItsReleaseWhenOnlyTheYearIsKnown() {
        ReleaseContent release =
                new ReleaseContent(null, null, ReleaseType.ORIGINAL, ReleaseDate.ofYear(Year.of(2026)), null);
        Software software = Software.create(
                ID,
                new SoftwareTitle("Joe Blade"),
                null,
                ReleaseDate.ofYear(Year.of(1987)),
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER);
        software.addRelease(ORIGINAL, release, NO_HARDWARE, BY_OTHER);
        repository.save(software);
        springDataSoftwareRepository.flush();
        entityManager.clear();

        Software loaded = repository.findById(ID).orElseThrow();
        assertThat(loaded.getReleaseDate()).isEqualTo(ReleaseDate.ofYear(Year.of(1987)));
        assertThat(loaded.getReleases()).extracting(Release::content).containsExactly(release);
    }

    @Test
    void storesEdits() {
        repository.save(Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER));
        springDataSoftwareRepository.flush();
        Software software = repository.findById(ID).orElseThrow();

        software.edit(
                new SoftwareTitle("Elite Plus"),
                new SoftwareDescription("Enhanced"),
                ReleaseDate.ofDay(LocalDate.of(1987, 1, 1)),
                HardwareSet.restore(BK, Set.of()),
                CategorySet.EMPTY);
        repository.save(software);
        springDataSoftwareRepository.flush();

        assertThat(jdbc.queryForObject("SELECT title FROM software WHERE id = ?", String.class, ID.value()))
                .isEqualTo("Elite Plus");
        assertThat(jdbc.queryForObject("SELECT platform_code FROM software WHERE id = ?", String.class, ID.value()))
                .isEqualTo("bk");
    }

    @Test
    void findsNothingForAnUnknownId() {
        assertThat(repository.findById(ID)).isEmpty();
    }

    @Test
    void deletesSoftwareWithEverythingThatBelongsToIt() {
        repository.save(Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER));
        springDataSoftwareRepository.flush();
        jdbc.update("INSERT INTO software_alternative_titles (title, software_id) VALUES ('Элита', ?)", ID.value());
        jdbc.update("INSERT INTO software_languages (software_id, language) VALUES (?, 'en')", ID.value());
        jdbc.update(
                "INSERT INTO software_links (url, type, software_id) VALUES ('https://example.com', 'homepage', ?)",
                ID.value());
        jdbc.update("INSERT INTO software_hardware (software_id, hardware_code) VALUES (?, 'vector06c')", ID.value());
        UUID release = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO software_release (id, software_id, type) VALUES (?, ?, 'original')", release, ID.value());
        jdbc.update("INSERT INTO software_release_hardware (release_id, hardware_code) VALUES (?, 'ay8910')", release);

        repository.delete(repository.findById(ID).orElseThrow());
        springDataSoftwareRepository.flush();

        assertThat(jdbc.queryForObject("SELECT count(*) FROM software WHERE id = ?", Integer.class, ID.value()))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software_alternative_titles", Integer.class))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software_languages", Integer.class))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software_links", Integer.class))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software_hardware", Integer.class))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software_release", Integer.class))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software_release_hardware", Integer.class))
                .isZero();
    }

    private static Screenshot shot(int n, ScreenshotFormat format) {
        return new Screenshot(new ScreenshotId(UUID.fromString("00000000-0000-0000-0055-%012d".formatted(n))), format);
    }

    @Test
    void storesTheScreenshotsOfTheSoftwareAndOfItsReleasesInTheirOrder() {
        Software software = Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER);
        software.addRelease(AY_MOD, AY_MOD_CONTENT, NO_HARDWARE, BY_OTHER);
        software.addScreenshots(null, List.of(shot(1, ScreenshotFormat.PNG), shot(2, ScreenshotFormat.GIF)), ADMIN);
        software.addScreenshots(AY_MOD, List.of(shot(3, ScreenshotFormat.WEBP)), ADMIN);
        repository.save(software);
        springDataSoftwareRepository.flush();
        entityManager.clear();

        Software loaded = repository.findById(ID).orElseThrow();
        assertThat(loaded.screenshots(null).all())
                .containsExactly(shot(1, ScreenshotFormat.PNG), shot(2, ScreenshotFormat.GIF));
        assertThat(loaded.screenshots(AY_MOD).all()).containsExactly(shot(3, ScreenshotFormat.WEBP));
    }

    @Test
    void storesTheOneArchiveOfARelease() {
        ReleaseArchive archive = new ReleaseArchive(new ReleaseArchiveId(UUID.randomUUID()), "elite.zip", 7);
        Software software = Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER);
        software.addRelease(AY_MOD, AY_MOD_CONTENT, NO_HARDWARE, BY_OTHER, archive);

        repository.save(software);
        springDataSoftwareRepository.flush();
        entityManager.clear();

        assertThat(repository
                        .findById(ID)
                        .orElseThrow()
                        .release(AY_MOD)
                        .orElseThrow()
                        .archive())
                .isEqualTo(archive);
    }

    @Test
    void storesReorderedAndRemovedScreenshots() {
        Software software = Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER);
        software.addRelease(AY_MOD, AY_MOD_CONTENT, NO_HARDWARE, BY_OTHER);
        software.addScreenshots(
                null,
                List.of(shot(1, ScreenshotFormat.PNG), shot(2, ScreenshotFormat.PNG), shot(3, ScreenshotFormat.PNG)),
                ADMIN);
        software.addScreenshots(AY_MOD, List.of(shot(4, ScreenshotFormat.PNG), shot(5, ScreenshotFormat.PNG)), ADMIN);
        repository.save(software);
        springDataSoftwareRepository.flush();
        entityManager.clear();

        Software loaded = repository.findById(ID).orElseThrow();
        loaded.reorderScreenshots(
                null,
                List.of(
                        shot(3, ScreenshotFormat.PNG).id(),
                        shot(1, ScreenshotFormat.PNG).id(),
                        shot(2, ScreenshotFormat.PNG).id()),
                ADMIN);
        loaded.removeScreenshot(null, shot(1, ScreenshotFormat.PNG).id(), ADMIN);
        loaded.reorderScreenshots(
                AY_MOD,
                List.of(
                        shot(5, ScreenshotFormat.PNG).id(),
                        shot(4, ScreenshotFormat.PNG).id()),
                ADMIN);
        repository.save(loaded);
        springDataSoftwareRepository.flush();
        entityManager.clear();

        Software stored = repository.findById(ID).orElseThrow();
        assertThat(stored.screenshots(null).all())
                .containsExactly(shot(3, ScreenshotFormat.PNG), shot(2, ScreenshotFormat.PNG));
        assertThat(stored.screenshots(AY_MOD).all())
                .containsExactly(shot(5, ScreenshotFormat.PNG), shot(4, ScreenshotFormat.PNG));
    }

    @Test
    void deletesTheScreenshotsWithTheirSoftwareAndRelease() {
        Software software = Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER);
        software.addRelease(AY_MOD, AY_MOD_CONTENT, NO_HARDWARE, BY_OTHER);
        software.addRelease(ORIGINAL, ORIGINAL_CONTENT, NO_HARDWARE, BY_OTHER);
        software.addScreenshots(null, List.of(shot(1, ScreenshotFormat.PNG)), ADMIN);
        software.addScreenshots(AY_MOD, List.of(shot(2, ScreenshotFormat.PNG)), ADMIN);
        software.addScreenshots(ORIGINAL, List.of(shot(3, ScreenshotFormat.PNG)), ADMIN);
        repository.save(software);
        springDataSoftwareRepository.flush();
        entityManager.clear();

        Software loaded = repository.findById(ID).orElseThrow();
        loaded.removeRelease(AY_MOD, ADMIN);
        repository.save(loaded);
        springDataSoftwareRepository.flush();
        assertThat(jdbc.queryForList("SELECT id FROM software_release_screenshot", UUID.class))
                .containsExactly(shot(3, ScreenshotFormat.PNG).id().value());

        repository.delete(repository.findById(ID).orElseThrow());
        springDataSoftwareRepository.flush();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software_screenshot", Integer.class))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software_release_screenshot", Integer.class))
                .isZero();
    }

    @Test
    void storesReleasesWithTheirHardware() {
        Software software = Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER);
        software.addRelease(ORIGINAL, ORIGINAL_CONTENT, NO_HARDWARE, BY_OTHER);
        software.addRelease(AY_MOD, AY_MOD_CONTENT, AY_AND_JOYSTICK, BY_OTHER);
        repository.save(software);
        springDataSoftwareRepository.flush();

        assertThat(repository.findById(ID).orElseThrow().getReleases())
                .extracting(Release::id, Release::content, Release::hardware)
                .containsExactlyInAnyOrder(
                        tuple(ORIGINAL, ORIGINAL_CONTENT, NO_HARDWARE), tuple(AY_MOD, AY_MOD_CONTENT, AY_AND_JOYSTICK));
    }

    @Test
    void storesEditedAndRemovedReleases() {
        Software software = Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER);
        software.addRelease(ORIGINAL, ORIGINAL_CONTENT, NO_HARDWARE, BY_OTHER);
        software.addRelease(AY_MOD, AY_MOD_CONTENT, AY_AND_JOYSTICK, BY_OTHER);
        repository.save(software);
        springDataSoftwareRepository.flush();

        Software loaded = repository.findById(ID).orElseThrow();
        loaded.removeRelease(ORIGINAL, ADMIN);
        loaded.editRelease(AY_MOD, ORIGINAL_CONTENT, NO_HARDWARE, ADMIN);
        repository.save(loaded);
        springDataSoftwareRepository.flush();

        assertThat(repository.findById(ID).orElseThrow().getReleases())
                .singleElement()
                .satisfies(release -> {
                    assertThat(release.id()).isEqualTo(AY_MOD);
                    assertThat(release.content()).isEqualTo(ORIGINAL_CONTENT);
                    assertThat(release.hardware()).isEqualTo(NO_HARDWARE);
                });
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software_release_hardware", Integer.class))
                .isZero();
    }

    @Test
    void storesAndReplacesTheHardwareOfTheSoftware() {
        repository.save(Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                HardwareSet.restore(VECTOR, Set.of(new HardwareCode("vector06c"))),
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER));
        springDataSoftwareRepository.flush();
        // Every request starts with an empty persistence context.
        entityManager.clear();

        Software software = repository.findById(ID).orElseThrow();
        assertThat(software.getHardware())
                .isEqualTo(HardwareSet.restore(VECTOR, Set.of(new HardwareCode("vector06c"))));
        software.edit(
                new SoftwareTitle("Elite"),
                null,
                null,
                HardwareSet.restore(VECTOR, Set.of(new HardwareCode("ay8910"))),
                CategorySet.EMPTY);
        repository.save(software);
        springDataSoftwareRepository.flush();
        entityManager.clear();

        assertThat(repository.findById(ID).orElseThrow().getHardware())
                .isEqualTo(HardwareSet.restore(VECTOR, Set.of(new HardwareCode("ay8910"))));
    }

    @Test
    void storesAndRestoresTheCategoriesOfTheSoftware() {
        repository.save(Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.restore(List.of(GAMES_LINE, INTRO_4K_LINE)),
                SoftwareCredits.EMPTY,
                BY_OTHER));
        springDataSoftwareRepository.flush();
        entityManager.clear();

        assertThat(repository.findById(ID))
                .hasValueSatisfying(software -> assertThat(software.getCategories())
                        .isEqualTo(CategorySet.restore(List.of(GAMES_LINE, INTRO_4K_LINE))));
    }

    @Test
    void storesEditedCategories() {
        repository.save(Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.restore(List.of(GAMES_LINE, INTRO_4K_LINE)),
                SoftwareCredits.EMPTY,
                BY_OTHER));
        springDataSoftwareRepository.flush();
        entityManager.clear();
        Software software = repository.findById(ID).orElseThrow();
        software.edit(new SoftwareTitle("Elite"), null, null, NO_HARDWARE, CategorySet.restore(List.of(INTRO_4K_LINE)));
        repository.save(software);
        springDataSoftwareRepository.flush();

        assertThat(jdbc.queryForList(
                        "SELECT category_id FROM software_category WHERE software_id = ?", UUID.class, ID.value()))
                .containsExactly(INTRO_4K.value());
    }

    @Test
    void leavesTheSoftwareWhenItsCategoryIsDeleted() {
        repository.save(Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.restore(List.of(GAMES_LINE, INTRO_4K_LINE)),
                SoftwareCredits.EMPTY,
                BY_OTHER));
        springDataSoftwareRepository.flush();
        entityManager.clear();

        jdbc.update("DELETE FROM category WHERE id = ?", INTRO_4K.value());

        assertThat(repository.findById(ID))
                .hasValueSatisfying(software ->
                        assertThat(software.getCategories()).isEqualTo(CategorySet.restore(List.of(GAMES_LINE))));
    }

    @Test
    void restoresNoCategoryAboveAnotherOfTheSoftware() {
        repository.save(Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.restore(List.of(GAMES_LINE)),
                SoftwareCredits.EMPTY,
                BY_OTHER));
        springDataSoftwareRepository.flush();
        // Stored before the rule, or put there by a change of the tree.
        jdbc.update(
                "INSERT INTO software_category (software_id, category_id) VALUES (?, ?)", ID.value(), ACTION.value());
        entityManager.clear();

        assertThat(repository.findById(ID))
                .hasValueSatisfying(
                        software -> assertThat(software.getCategories().ids()).containsExactly(ACTION));
    }

    @Test
    void remembersWhoAddedTheSoftwareAndEachRelease() {
        Software software = Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                new AddedBy(MEMBER_ID));
        software.addRelease(ORIGINAL, ORIGINAL_CONTENT, NO_HARDWARE, new AddedBy(OTHER_ID));
        software.addRelease(AY_MOD, AY_MOD_CONTENT, NO_HARDWARE, new AddedBy(ADMIN_ID));
        repository.save(software);
        springDataSoftwareRepository.flush();
        entityManager.clear();

        Software stored = repository.findById(ID).orElseThrow();
        assertThat(stored.getAddedBy()).isEqualTo(new AddedBy(MEMBER_ID));
        assertThat(stored.getReleases())
                .extracting(Release::id, Release::addedBy)
                .containsExactlyInAnyOrder(
                        tuple(ORIGINAL, new AddedBy(OTHER_ID)), tuple(AY_MOD, new AddedBy(ADMIN_ID)));
    }

    @Test
    void forgetsWhoAddedTheSoftwareOnceTheirAccountIsGone() {
        Software software = Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                new AddedBy(MEMBER_ID));
        software.addRelease(ORIGINAL, ORIGINAL_CONTENT, NO_HARDWARE, new AddedBy(MEMBER_ID));
        repository.save(software);
        springDataSoftwareRepository.flush();
        jdbc.update("DELETE FROM app_user WHERE id = ?", MEMBER_ID.value());
        entityManager.clear();

        Software stored = repository.findById(ID).orElseThrow();
        assertThat(stored.getAddedBy()).isEqualTo(AddedBy.ACCOUNT_DELETED);
        assertThat(stored.getReleases()).extracting(Release::addedBy).containsExactly(AddedBy.ACCOUNT_DELETED);
    }

    @Test
    void savesSoftwareWithItsCreditsOfPeopleGroupsAndTheirLabelsAgain() {
        UUID person = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
        UUID label = UUID.fromString("00000000-0000-0000-0000-0000000000a2");
        UUID group = UUID.fromString("00000000-0000-0000-0000-0000000000a3");
        UUID groupLabel = UUID.fromString("00000000-0000-0000-0000-0000000000a4");
        jdbc.update("INSERT INTO person (id) VALUES (?)", person);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew')", group);
        jdbc.update(
                "INSERT INTO label (id, name, person_id, group_id) VALUES (?, 'Zed', ?, NULL), (?, 'CRW', NULL, ?)",
                label,
                person,
                groupLabel,
                group);
        repository.save(Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.restore(List.of(
                        credit(SoftwareReferenceType.AUTHOR_LABEL, label, "code"),
                        credit(SoftwareReferenceType.GROUP, group, "developer"),
                        credit(SoftwareReferenceType.GROUP_LABEL, groupLabel, "publisher"))),
                BY_OTHER));
        springDataSoftwareRepository.flush();
        entityManager.clear();

        // Any change of the software saves its credits as they are, such as a screenshot added.
        Software loaded = repository.findById(ID).orElseThrow();
        repository.save(loaded);
        springDataSoftwareRepository.flush();
        entityManager.clear();

        assertThat(repository.findById(ID).orElseThrow().getCredits().all())
                .containsExactlyInAnyOrder(
                        credit(SoftwareReferenceType.AUTHOR_LABEL, label, "code"),
                        credit(SoftwareReferenceType.GROUP, group, "developer"),
                        credit(SoftwareReferenceType.GROUP_LABEL, groupLabel, "publisher"));
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM software_contribution WHERE software_id = ?", Integer.class, ID.value()))
                .isEqualTo(3);
    }

    private static SoftwareCreditDraft credit(SoftwareReferenceType type, UUID id, String role) {
        return new SoftwareCreditDraft(new SoftwareReference(type, new SoftwareReferenceId(id)), List.of(role));
    }
}
