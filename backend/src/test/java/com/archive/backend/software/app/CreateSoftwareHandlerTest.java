package com.archive.backend.software.app;

import static com.archive.backend.software.app.ReleaseFixtures.ARCADE;
import static com.archive.backend.software.app.ReleaseFixtures.AY;
import static com.archive.backend.software.app.ReleaseFixtures.BK0011M;
import static com.archive.backend.software.app.ReleaseFixtures.DEMOSCENE;
import static com.archive.backend.software.app.ReleaseFixtures.GAMES;
import static com.archive.backend.software.app.ReleaseFixtures.GONE;
import static com.archive.backend.software.app.ReleaseFixtures.VECTOR;
import static com.archive.backend.software.app.ReleaseFixtures.categorySets;
import static com.archive.backend.software.app.ReleaseFixtures.hardwareSets;
import static com.archive.backend.software.app.ReleaseFixtures.in;
import static com.archive.backend.software.app.ReleaseFixtures.vector;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CreateSoftwareHandlerTest {

    private final InMemorySoftwareRepository repository = new InMemorySoftwareRepository();
    private final CreateSoftwareHandler handler =
            new CreateSoftwareHandler(repository, hardwareSets(), categorySets(), event -> {});

    @Test
    void storesNewSoftwareUnderANewId() {
        SoftwareId id = handler.handle(new CreateSoftwareCommand(
                new SoftwareTitle("Elite"),
                new SoftwareDescription("Space trading"),
                LocalDate.of(1985, 9, 20),
                VECTOR,
                Set.of(),
                Set.of()));

        assertThat(repository.findById(id)).hasValueSatisfying(software -> {
            assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite"));
            assertThat(software.getDescription()).isEqualTo(new SoftwareDescription("Space trading"));
            assertThat(software.getReleaseDate()).isEqualTo(LocalDate.of(1985, 9, 20));
            assertThat(software.getPlatform()).isEqualTo(VECTOR);
        });
    }

    @Test
    void givesEverySoftwareItsOwnId() {
        SoftwareId first = handler.handle(
                new CreateSoftwareCommand(new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(), Set.of()));
        SoftwareId second = handler.handle(
                new CreateSoftwareCommand(new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(), Set.of()));

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void refusesAnUnknownPlatformAndStoresNothing() {
        assertThatThrownBy(() -> handler.handle(new CreateSoftwareCommand(
                        new SoftwareTitle("Elite"), null, null, new PlatformCode("zx"), Set.of(), Set.of())))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("software.platform-unknown"));
        assertThat(repository.count()).isZero();
    }

    @Test
    void storesTheHardwareOfTheSoftware() {
        SoftwareId id = handler.handle(
                new CreateSoftwareCommand(new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(AY), Set.of()));

        assertThat(repository.findById(id))
                .hasValueSatisfying(
                        software -> assertThat(software.getHardware()).isEqualTo(vector(AY)));
    }

    @Test
    void refusesHardwareOfAnotherPlatformAndStoresNothing() {
        assertThatThrownBy(() -> handler.handle(new CreateSoftwareCommand(
                        new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(BK0011M), Set.of())))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("hardware.unknown");
                    assertThat(e.field()).isEqualTo("hardware");
                });
        assertThat(repository.count()).isZero();
    }

    @Test
    void storesTheCategoriesOfTheSoftware() {
        SoftwareId id = handler.handle(new CreateSoftwareCommand(
                new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(), Set.of(ARCADE, DEMOSCENE)));

        assertThat(repository.findById(id))
                .hasValueSatisfying(
                        software -> assertThat(software.getCategories()).isEqualTo(in(ARCADE, DEMOSCENE)));
    }

    @Test
    void takesTheSoftwareOutOfACategoryAboveAnotherOfIts() {
        SoftwareId id = handler.handle(new CreateSoftwareCommand(
                new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(), Set.of(GAMES, ARCADE)));

        assertThat(repository.findById(id))
                .hasValueSatisfying(
                        software -> assertThat(software.getCategories()).isEqualTo(in(ARCADE)));
    }

    @Test
    void storesSoftwareInNoCategory() {
        SoftwareId id = handler.handle(
                new CreateSoftwareCommand(new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(), Set.of()));

        assertThat(repository.findById(id))
                .hasValueSatisfying(
                        software -> assertThat(software.getCategories()).isEqualTo(CategorySet.EMPTY));
    }

    @Test
    void refusesACategoryNotInTheArchiveAndStoresNothing() {
        assertThatThrownBy(() -> handler.handle(new CreateSoftwareCommand(
                        new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(), Set.of(GAMES, GONE))))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("category.unknown");
                    assertThat(e.field()).isEqualTo("categories");
                });
        assertThat(repository.count()).isZero();
    }
}
