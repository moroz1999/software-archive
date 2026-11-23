package com.archive.backend.group.app;

import static com.archive.backend.group.app.GroupFixtures.CREW;
import static com.archive.backend.group.app.GroupFixtures.CRW;
import static com.archive.backend.group.app.GroupFixtures.crew;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.BY_OTHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.geo.domain.Locations;
import com.archive.backend.group.domain.GroupChanged;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.LabelAdoptedByGroup;
import com.archive.backend.label.app.InMemoryLabelRepository;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.OwnLabel;
import com.archive.backend.label.domain.OwnerOfLabelsChanged;
import com.archive.backend.shared.domain.error.ConflictException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttachLabelToGroupHandlerTest {

    private static final LabelId SOL = new LabelId(UUID.fromString("00000000-0000-0000-0000-000000000051"));

    private final InMemoryGroupRepository groups = new InMemoryGroupRepository();
    private final InMemoryLabelRepository labels = new InMemoryLabelRepository();
    private final List<Object> events = new ArrayList<>();
    private final AttachLabelToGroupHandler handler = new AttachLabelToGroupHandler(groups, labels, events::add);

    @BeforeEach
    void storeCrewAndALabelOfNobody() {
        groups.save(crew());
        labels.save(Label.restore(SOL, new LabelName("Sol"), new Locations(List.of()), BY_OTHER));
    }

    @Test
    void givesTheLabelToTheGroupAndTellsItsWorksChanged() {
        handler.handle(new AttachLabelToGroupCommand(ADMIN, SOL, CREW));

        assertThat(groups.findById(CREW))
                .hasValueSatisfying(group -> assertThat(group.getLabels().all())
                        .containsExactly(
                                new OwnLabel(CRW, new LabelName("CRW"), BY_OTHER),
                                new OwnLabel(SOL, new LabelName("Sol"), BY_OTHER)));
        assertThat(events)
                .containsExactlyInAnyOrder(
                        new LabelAdoptedByGroup(SOL, CREW),
                        new OwnerOfLabelsChanged(Set.of(SOL)),
                        new GroupChanged(CREW));
    }

    @Test
    void refusesALabelSomebodyHasMeanwhile() {
        LabelId owned = new LabelId(UUID.randomUUID());
        labels.own(owned);

        assertThatThrownBy(() -> handler.handle(new AttachLabelToGroupCommand(ADMIN, owned, CREW)))
                .isInstanceOfSatisfying(
                        ConflictException.class, e -> assertThat(e.code()).isEqualTo("label.owned"));
    }

    @Test
    void refusesAGroupNotInTheArchive() {
        assertThatThrownBy(
                        () -> handler.handle(new AttachLabelToGroupCommand(ADMIN, SOL, new GroupId(UUID.randomUUID()))))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("label.owner-unknown"));
    }
}
