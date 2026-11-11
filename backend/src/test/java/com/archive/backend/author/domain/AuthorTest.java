package com.archive.backend.author.domain;

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
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthorTest {

    private static final AuthorId ID = new AuthorId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final LabelId ZED = new LabelId(UUID.fromString("00000000-0000-0000-0000-000000000011"));
    private static final Locations NOWHERE = new Locations(List.of());
    private static final Locations NORWAY =
            new Locations(List.of(new Location(Place.restore(new CountryId(UUID.randomUUID()), null), true)));

    private static Author johnDoe() {
        return Author.create(ID, new RealName("John Doe"), null, List.of(), NOWHERE);
    }

    private static Author zed() {
        return new Author(
                ID, null, null, OwnLabels.restore(List.of(new OwnLabel(ZED, new LabelName("Zed"))), ZED), NOWHERE);
    }

    @Test
    void isCreatedWithItsRealNameNotesLabelsAndLocations() {
        Author author = Author.create(
                ID,
                new RealName("John Doe"),
                new AuthorNotes("Coder since 1984"),
                List.of(new LabelDraft(null, new LabelName("Zed"), true)),
                NORWAY);

        assertThat(author.getId()).isEqualTo(ID);
        assertThat(author.getRealName()).isEqualTo(new RealName("John Doe"));
        assertThat(author.getNotes()).isEqualTo(new AuthorNotes("Coder since 1984"));
        assertThat(author.getLabels().all()).extracting(OwnLabel::name).containsExactly(new LabelName("Zed"));
        assertThat(author.getLocations()).isEqualTo(NORWAY);
    }

    @Test
    void isKnownByTheRealNameAlone() {
        assertThat(johnDoe().getLabels().isEmpty()).isTrue();
    }

    @Test
    void isKnownByALabelAlone() {
        assertThat(zed().getRealName()).isNull();
    }

    @Test
    void cannotBeCreatedWithoutAnyName() {
        assertThatThrownBy(() -> Author.create(ID, null, null, List.of(), NOWHERE))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("author.nameless");
                    assertThat(e.field()).isEqualTo("realName");
                });
    }

    @Test
    void editingReplacesEverything() {
        Author author = zed();

        author.edit(
                new RealName("John Doe"),
                new AuthorNotes("Retired"),
                List.of(new LabelDraft(null, new LabelName("Axe"), true)),
                NORWAY);

        assertThat(author.getRealName()).isEqualTo(new RealName("John Doe"));
        assertThat(author.getNotes()).isEqualTo(new AuthorNotes("Retired"));
        assertThat(author.getLabels().all()).extracting(OwnLabel::name).containsExactly(new LabelName("Axe"));
        assertThat(author.getLabels().display())
                .isEqualTo(author.getLabels().all().get(0).id());
        assertThat(author.getLocations()).isEqualTo(NORWAY);
    }

    @Test
    void keepsTheIdsOfTheLabelsItRenames() {
        Author author = zed();

        author.edit(null, null, List.of(new LabelDraft(ZED, new LabelName("Zed II"), true)), NOWHERE);

        assertThat(author.getLabels().all()).containsExactly(new OwnLabel(ZED, new LabelName("Zed II")));
    }

    @Test
    void refusesALabelOfSomebodyElse() {
        Author author = zed();

        assertThatThrownBy(() -> author.edit(
                        null,
                        null,
                        List.of(new LabelDraft(new LabelId(UUID.randomUUID()), new LabelName("Bob"), true)),
                        NOWHERE))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("label.not-own"));
        assertThat(author.getLabels().all()).extracting(OwnLabel::id).containsExactly(ZED);
    }

    @Test
    void cannotBeEditedIntoHavingNoName() {
        Author author = zed();

        assertThatThrownBy(() -> author.edit(null, new AuthorNotes("Gone"), List.of(), NORWAY))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("author.nameless"));
        assertThat(author.getLabels().all()).extracting(OwnLabel::id).containsExactly(ZED);
        assertThat(author.getNotes()).isNull();
        assertThat(author.getLocations()).isEqualTo(NOWHERE);
    }
}
