package com.archive.backend.label.app;

import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.label.domain.LabelCreated;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.SignInRequiredException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateLabelHandlerTest {

    private final InMemoryLabelRepository repository = new InMemoryLabelRepository();
    private final List<Object> events = new ArrayList<>();
    private final CreateLabelHandler handler = new CreateLabelHandler(repository, events::add);

    @Test
    void storesALabelOfNobodyUnderANewId() {
        LabelId id = handler.handle(new CreateLabelCommand(MEMBER, new LabelName("Городецкий И. /ivagor")));

        assertThat(repository.findById(id))
                .hasValueSatisfying(
                        label -> assertThat(label.getName()).isEqualTo(new LabelName("Городецкий И. /ivagor")));
    }

    @Test
    void tellsTheLabelWasAdded() {
        LabelId id = handler.handle(new CreateLabelCommand(MEMBER, new LabelName("Zed")));

        assertThat(events).containsExactly(new LabelCreated(id));
    }

    @Test
    void givesEveryLabelItsOwnId() {
        CreateLabelCommand command = new CreateLabelCommand(MEMBER, new LabelName("Zed"));

        assertThat(handler.handle(command)).isNotEqualTo(handler.handle(command));
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void refusesAPublicVisitorAndStoresNothing() {
        assertThatThrownBy(() -> handler.handle(new CreateLabelCommand(Actor.anonymous(), new LabelName("Zed"))))
                .isInstanceOf(SignInRequiredException.class);
        assertThat(repository.count()).isZero();
        assertThat(events).isEmpty();
    }
}
