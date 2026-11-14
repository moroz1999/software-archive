package com.archive.backend.importer.app;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.SignInRequiredException;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListVektorSectionsHandlerTest {

    private final ListVektorSectionsHandler handler = new ListVektorSectionsHandler(() -> List.of("zagruzchiki"));

    @Test
    void givesAnAdministratorTheSectionsToImport() {
        assertThat(handler.handle(new ListVektorSectionsQuery(ADMIN))).containsExactly("zagruzchiki");
    }

    @Test
    void asksAPublicVisitorToSignIn() {
        assertThatThrownBy(() -> handler.handle(new ListVektorSectionsQuery(Actor.anonymous())))
                .isInstanceOf(SignInRequiredException.class);
    }

    @Test
    void refusesAMemberWhoIsNotAnAdministrator() {
        assertThatThrownBy(() -> handler.handle(new ListVektorSectionsQuery(MEMBER)))
                .isInstanceOf(ChangeForbiddenException.class);
    }
}
