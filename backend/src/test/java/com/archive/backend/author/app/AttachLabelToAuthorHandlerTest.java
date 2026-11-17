package com.archive.backend.author.app;

import static com.archive.backend.author.app.AuthorFixtures.ZED;
import static com.archive.backend.author.app.AuthorFixtures.ZED_LABEL;
import static com.archive.backend.author.app.AuthorFixtures.zed;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.BY_OTHER;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.author.domain.AuthorChanged;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.geo.domain.Locations;
import com.archive.backend.label.app.InMemoryLabelRepository;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelAttachment;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.OwnLabel;
import com.archive.backend.label.domain.OwnerOfLabelsChanged;
import com.archive.backend.shared.domain.error.ConflictException;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import com.archive.backend.user.domain.ChangeForbiddenException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttachLabelToAuthorHandlerTest {

    private static final LabelId BOB = new LabelId(UUID.fromString("00000000-0000-0000-0000-000000000051"));

    private final InMemoryAuthorRepository authors = new InMemoryAuthorRepository();
    private final InMemoryLabelRepository labels = new InMemoryLabelRepository();
    private final List<Object> events = new ArrayList<>();
    private final AttachLabelToAuthorHandler handler = new AttachLabelToAuthorHandler(authors, labels, events::add);

    @BeforeEach
    void storeZedAndALabelOfNobody() {
        authors.save(zed());
        labels.save(Label.restore(BOB, new LabelName("Bob"), new Locations(List.of()), BY_OTHER));
    }

    @Test
    void givesTheLabelToThePersonBesideTheirDisplayName() {
        handler.handle(new AttachLabelToAuthorCommand(ADMIN, new LabelAttachment(BOB, false), ZED));

        assertThat(authors.findById(ZED)).hasValueSatisfying(author -> {
            assertThat(author.getLabels().all())
                    .containsExactly(
                            new OwnLabel(ZED_LABEL, new LabelName("Zed"), BY_OTHER),
                            new OwnLabel(BOB, new LabelName("Bob"), BY_OTHER));
            assertThat(author.getLabels().display()).isEqualTo(ZED_LABEL);
        });
    }

    @Test
    void makesTheLabelTheDisplayNameOfThePersonWhenToldTo() {
        handler.handle(new AttachLabelToAuthorCommand(ADMIN, new LabelAttachment(BOB, true), ZED));

        assertThat(authors.findById(ZED))
                .hasValueSatisfying(
                        author -> assertThat(author.getLabels().display()).isEqualTo(BOB));
    }

    @Test
    void tellsTheWorksOfTheLabelAndThePersonChanged() {
        handler.handle(new AttachLabelToAuthorCommand(ADMIN, new LabelAttachment(BOB, false), ZED));

        assertThat(events).containsExactlyInAnyOrder(new OwnerOfLabelsChanged(Set.of(BOB)), new AuthorChanged(ZED));
    }

    @Test
    void refusesALabelSomebodyHasMeanwhile() {
        LabelId owned = new LabelId(UUID.randomUUID());
        labels.own(owned);

        assertThatThrownBy(() ->
                        handler.handle(new AttachLabelToAuthorCommand(ADMIN, new LabelAttachment(owned, false), ZED)))
                .isInstanceOfSatisfying(
                        ConflictException.class, e -> assertThat(e.code()).isEqualTo("label.owned"));
    }

    @Test
    void failsForALabelNotInTheArchive() {
        assertThatThrownBy(() -> handler.handle(new AttachLabelToAuthorCommand(
                        ADMIN, new LabelAttachment(new LabelId(UUID.randomUUID()), false), ZED)))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("label.not-found"));
    }

    @Test
    void refusesAPersonNotInTheArchive() {
        assertThatThrownBy(() -> handler.handle(new AttachLabelToAuthorCommand(
                        ADMIN, new LabelAttachment(BOB, false), new AuthorId(UUID.randomUUID()))))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("label.owner-unknown");
                    assertThat(e.field()).isEqualTo("owner");
                });
    }

    @Test
    void refusesWhoeverMayNotChangeThePerson() {
        assertThatThrownBy(() ->
                        handler.handle(new AttachLabelToAuthorCommand(MEMBER, new LabelAttachment(BOB, false), ZED)))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThat(authors.findById(ZED))
                .hasValueSatisfying(
                        author -> assertThat(author.getLabels().all()).hasSize(1));
    }
}
