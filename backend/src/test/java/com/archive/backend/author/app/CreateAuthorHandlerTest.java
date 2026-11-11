package com.archive.backend.author.app;

import static com.archive.backend.author.app.AuthorFixtures.FINLAND;
import static com.archive.backend.author.app.AuthorFixtures.HELSINKI;
import static com.archive.backend.author.app.AuthorFixtures.NORWAY;
import static com.archive.backend.author.app.AuthorFixtures.places;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.AuthorNotes;
import com.archive.backend.author.domain.RealName;
import com.archive.backend.geo.domain.Location;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.geo.domain.Place;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.OwnLabel;
import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateAuthorHandlerTest {

    private final InMemoryAuthorRepository repository = new InMemoryAuthorRepository();
    private final CreateAuthorHandler handler = new CreateAuthorHandler(repository, places());

    @Test
    void storesANewAuthorUnderANewId() {
        AuthorId id = handler.handle(new CreateAuthorCommand(
                new RealName("John Doe"),
                new AuthorNotes("Coder since 1984"),
                List.of(new LabelDraft(null, new LabelName("Zed"), true)),
                List.of(new LocationDraft(FINLAND, HELSINKI, true), new LocationDraft(NORWAY, null, false))));

        assertThat(repository.findById(id)).hasValueSatisfying(author -> {
            assertThat(author.getRealName()).isEqualTo(new RealName("John Doe"));
            assertThat(author.getNotes()).isEqualTo(new AuthorNotes("Coder since 1984"));
            assertThat(author.getLabels().all()).extracting(OwnLabel::name).containsExactly(new LabelName("Zed"));
            assertThat(author.getLocations().all())
                    .containsExactly(
                            new Location(Place.restore(FINLAND, HELSINKI), true),
                            new Location(Place.restore(NORWAY, null), false));
        });
    }

    @Test
    void givesEveryAuthorItsOwnId() {
        CreateAuthorCommand command = new CreateAuthorCommand(new RealName("John Doe"), null, List.of(), List.of());

        assertThat(handler.handle(command)).isNotEqualTo(handler.handle(command));
    }

    @Test
    void refusesAnAuthorWithoutAnyNameAndStoresNothing() {
        assertThatThrownBy(() -> handler.handle(new CreateAuthorCommand(null, null, List.of(), List.of())))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("author.nameless"));
        assertThat(repository.count()).isZero();
    }

    @Test
    void refusesACityOfAnotherCountryAndStoresNothing() {
        assertThatThrownBy(() -> handler.handle(new CreateAuthorCommand(
                        new RealName("John Doe"), null, List.of(), List.of(new LocationDraft(NORWAY, HELSINKI, true)))))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("location.unknown");
                    assertThat(e.field()).isEqualTo("locations");
                });
        assertThat(repository.count()).isZero();
    }
}
