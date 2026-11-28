package com.archive.backend.importer.app;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.importer.domain.run.ImportLogSeq;
import com.archive.backend.importer.read.ImportRunProgressEntry;
import com.archive.backend.importer.read.ImportRunReport;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.SignInRequiredException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
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
            0,
            new ImportRunProgressEntry(0, null, null),
            List.of());

    private final GetLastImportRunHandler handler = new GetLastImportRunHandler(after -> Optional.of(REPORT));

    @Test
    void givesAnAdministratorTheReportOfTheLastRun() {
        assertThat(handler.handle(new GetLastImportRunQuery(ADMIN, null))).contains(REPORT);
    }

    @Test
    void asksOnlyForTheLinesOfTheLogAfterTheOneGiven() {
        List<@Nullable Long> asked = new ArrayList<>();
        GetLastImportRunHandler reading = new GetLastImportRunHandler(after -> {
            asked.add(after);
            return Optional.of(REPORT);
        });

        reading.handle(new GetLastImportRunQuery(ADMIN, new ImportLogSeq(41)));
        reading.handle(new GetLastImportRunQuery(ADMIN, null));

        assertThat(asked).containsExactly(41L, null);
    }

    @Test
    void refusesAMemberWhoIsNotAnAdministrator() {
        assertThatThrownBy(() -> handler.handle(new GetLastImportRunQuery(MEMBER, null)))
                .isInstanceOf(ChangeForbiddenException.class);
    }

    @Test
    void asksAPublicVisitorToSignIn() {
        assertThatThrownBy(() -> handler.handle(new GetLastImportRunQuery(Actor.anonymous(), null)))
                .isInstanceOf(SignInRequiredException.class);
    }
}
