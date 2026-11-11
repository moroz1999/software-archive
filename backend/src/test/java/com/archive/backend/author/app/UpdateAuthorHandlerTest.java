package com.archive.backend.author.app;

import static com.archive.backend.author.app.AuthorFixtures.NORWAY;
import static com.archive.backend.author.app.AuthorFixtures.ZED;
import static com.archive.backend.author.app.AuthorFixtures.ZED_LABEL;
import static com.archive.backend.author.app.AuthorFixtures.places;
import static com.archive.backend.author.app.AuthorFixtures.zed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.author.domain.AuthorNotes;
import com.archive.backend.author.domain.RealName;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.Location;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.geo.domain.Place;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.OwnLabel;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UpdateAuthorHandlerTest {

    private final InMemoryAuthorRepository repository = new InMemoryAuthorRepository();
    private final UpdateAuthorHandler handler = new UpdateAuthorHandler(repository, places());

    @Test
    void storesTheEditedAuthor() {
        repository.save(zed());

        handler.handle(new UpdateAuthorCommand(
                ZED,
                new RealName("John Doe"),
                new AuthorNotes("Retired"),
                List.of(
                        new LabelDraft(ZED_LABEL, new LabelName("Zed II"), false),
                        new LabelDraft(null, new LabelName("Axe"), true)),
                List.of(new LocationDraft(NORWAY, null, true))));

        assertThat(repository.findById(ZED)).hasValueSatisfying(author -> {
            assertThat(author.getRealName()).isEqualTo(new RealName("John Doe"));
            assertThat(author.getNotes()).isEqualTo(new AuthorNotes("Retired"));
            assertThat(author.getLabels().all())
                    .extracting(OwnLabel::name)
                    .containsExactly(new LabelName("Zed II"), new LabelName("Axe"));
            assertThat(author.getLabels().all().get(0).id()).isEqualTo(ZED_LABEL);
            assertThat(author.getLocations().all()).containsExactly(new Location(Place.restore(NORWAY, null), true));
        });
    }

    @Test
    void failsWhenTheAuthorDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(
                        new UpdateAuthorCommand(ZED, new RealName("John Doe"), null, List.of(), List.of())))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("author.not-found"));
    }

    @Test
    void refusesALabelOfSomebodyElseAndKeepsTheAuthor() {
        repository.save(zed());

        assertThatThrownBy(() -> handler.handle(new UpdateAuthorCommand(
                        ZED,
                        null,
                        null,
                        List.of(new LabelDraft(new LabelId(UUID.randomUUID()), new LabelName("Bob"), true)),
                        List.of())))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("label.not-own"));
        assertThat(repository.findById(ZED))
                .hasValueSatisfying(author -> assertThat(author.getLabels().all())
                        .extracting(OwnLabel::id)
                        .containsExactly(ZED_LABEL));
    }

    @Test
    void refusesAnUnknownCountryAndKeepsTheAuthor() {
        repository.save(zed());

        assertThatThrownBy(() -> handler.handle(new UpdateAuthorCommand(
                        ZED,
                        null,
                        null,
                        List.of(new LabelDraft(ZED_LABEL, new LabelName("Zed"), true)),
                        List.of(new LocationDraft(new CountryId(UUID.randomUUID()), null, true)))))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("location.unknown"));
        assertThat(repository.findById(ZED))
                .hasValueSatisfying(
                        author -> assertThat(author.getLocations().all()).isEmpty());
    }

    @Test
    void refusesToLeaveTheAuthorWithoutAnyName() {
        repository.save(zed());

        assertThatThrownBy(() -> handler.handle(new UpdateAuthorCommand(ZED, null, null, List.of(), List.of())))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("author.nameless"));
    }
}
