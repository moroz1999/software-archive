package com.archive.backend.label.app;

import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.GeoCatalog;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.geo.domain.LocationUnknownException;
import com.archive.backend.geo.domain.Places;
import com.archive.backend.label.domain.LabelCreated;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.SignInRequiredException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateLabelHandlerTest {

    private static final CountryId FINLAND = new CountryId(UUID.fromString("00000000-0000-0000-0002-000000000001"));

    private final InMemoryLabelRepository repository = new InMemoryLabelRepository();
    private final List<Object> events = new ArrayList<>();
    private final CreateLabelHandler handler = new CreateLabelHandler(repository, places(), events::add);

    @Test
    void locatesTheLabel() {
        LabelId id = handler.handle(
                new CreateLabelCommand(MEMBER, new LabelName("Zed"), List.of(new LocationDraft(FINLAND, null, true))));

        assertThat(repository.findById(id))
                .hasValueSatisfying(label -> assertThat(label.getLocations().all())
                        .extracting(location -> location.place().country())
                        .containsExactly(FINLAND));
    }

    @Test
    void refusesAPlaceNotInTheArchiveAndStoresNothing() {
        CountryId nowhere = new CountryId(UUID.fromString("00000000-0000-0000-0002-000000000099"));

        assertThatThrownBy(() -> handler.handle(new CreateLabelCommand(
                        MEMBER, new LabelName("Zed"), List.of(new LocationDraft(nowhere, null, true)))))
                .isInstanceOf(LocationUnknownException.class);
        assertThat(repository.count()).isZero();
    }

    /** Finland only, without cities. */
    private static Places places() {
        return new Places(new GeoCatalog() {
            @Override
            public boolean countryExists(CountryId country) {
                return country.equals(FINLAND);
            }

            @Override
            public Optional<CountryId> countryOf(CityId city) {
                return Optional.empty();
            }
        });
    }

    @Test
    void storesALabelOfNobodyUnderANewId() {
        LabelId id = handler.handle(new CreateLabelCommand(MEMBER, new LabelName("Городецкий И. /ivagor"), List.of()));

        assertThat(repository.findById(id))
                .hasValueSatisfying(
                        label -> assertThat(label.getName()).isEqualTo(new LabelName("Городецкий И. /ivagor")));
    }

    @Test
    void tellsTheLabelWasAdded() {
        LabelId id = handler.handle(new CreateLabelCommand(MEMBER, new LabelName("Zed"), List.of()));

        assertThat(events).containsExactly(new LabelCreated(id));
    }

    @Test
    void givesEveryLabelItsOwnId() {
        CreateLabelCommand command = new CreateLabelCommand(MEMBER, new LabelName("Zed"), List.of());

        assertThat(handler.handle(command)).isNotEqualTo(handler.handle(command));
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void refusesAPublicVisitorAndStoresNothing() {
        assertThatThrownBy(() ->
                        handler.handle(new CreateLabelCommand(Actor.anonymous(), new LabelName("Zed"), List.of())))
                .isInstanceOf(SignInRequiredException.class);
        assertThat(repository.count()).isZero();
        assertThat(events).isEmpty();
    }
}
