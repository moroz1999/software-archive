package com.archive.backend.importer.app;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.importer.read.ImportRunReport;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.SignInRequiredException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetLastImportRunHandlerTest {

    private static final ImportRunReport REPORT = new ImportRunReport(
            UUID.randomUUID(),
            "vektor-06c.ru",
            null,
            "DONE",
            Instant.EPOCH,
            Instant.EPOCH,
            0,
            0,
            0,
            0,
            List.of(),
            List.of());

    private final GetLastImportRunHandler handler = new GetLastImportRunHandler(() -> Optional.of(REPORT));

    @Test
    void givesAnAdministratorTheReportOfTheLastRun() {
        assertThat(handler.handle(new GetLastImportRunQuery(ADMIN))).contains(REPORT);
    }

    @Test
    void refusesAMemberWhoIsNotAnAdministrator() {
        assertThatThrownBy(() -> handler.handle(new GetLastImportRunQuery(MEMBER)))
                .isInstanceOf(ChangeForbiddenException.class);
    }

    @Test
    void asksAPublicVisitorToSignIn() {
        assertThatThrownBy(() -> handler.handle(new GetLastImportRunQuery(Actor.anonymous())))
                .isInstanceOf(SignInRequiredException.class);
    }
}
