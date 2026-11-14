package com.archive.backend.importer.app;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.archive.backend.importer.domain.ImportRunningException;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunRepository;
import com.archive.backend.importer.domain.run.ImportRunState;
import com.archive.backend.importer.domain.run.ImportScope;
import com.archive.backend.importer.domain.vektor.VektorSection;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.SignInRequiredException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.Executor;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class StartVektorImportHandlerTest {

    private static final VektorSection LOADERS = new VektorSection("zagruzchiki");

    /** Runs nothing until the test lets the queued work run. */
    private final Deque<Runnable> background = new ArrayDeque<>();

    private final Executor executor = background::add;
    private final ImportVektorHandler importer = mock(ImportVektorHandler.class);
    private final ImportRunRepository runs = mock(ImportRunRepository.class);
    private final EndImportRunHandler endRun = mock(EndImportRunHandler.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-14T10:00:00Z"), ZoneOffset.UTC);
    private final StartVektorImportHandler handler =
            new StartVektorImportHandler(importer, runs, endRun, clock, executor);

    private static ImportVektorCommand of(@Nullable VektorSection section) {
        return argThat(command -> Objects.equals(command.section(), section));
    }

    @Test
    void writesDownTheRunBeforeItStarts() {
        handler.handle(new StartVektorImportCommand(ADMIN, LOADERS));

        ArgumentCaptor<ImportRun> saved = ArgumentCaptor.forClass(ImportRun.class);
        verify(runs).save(saved.capture());
        assertThat(saved.getValue().getState()).isEqualTo(ImportRunState.RUNNING);
        assertThat(saved.getValue().getScope()).isEqualTo(new ImportScope("vektor-06c.ru", LOADERS));
        assertThat(saved.getValue().getStartedAt()).isEqualTo(clock.instant());
        background.poll().run();
        verify(importer).handle(new ImportVektorCommand(saved.getValue().getId(), LOADERS));
    }

    @Test
    void endsTheRunStoppedWhenTheWholeImportFails() {
        when(importer.handle(any())).thenThrow(new IllegalStateException("The site is down"));
        handler.handle(new StartVektorImportCommand(ADMIN, LOADERS));
        ArgumentCaptor<ImportRun> saved = ArgumentCaptor.forClass(ImportRun.class);
        verify(runs).save(saved.capture());

        background.poll().run();

        verify(endRun).handle(new EndImportRunCommand(saved.getValue().getId(), ImportRunState.STOPPED));
    }

    @Test
    void startsTheImportInTheBackgroundAndReturnsAtOnce() {
        when(importer.handle(of(LOADERS))).thenReturn(new ImportSummary(1, 0, 0, 0));

        handler.handle(new StartVektorImportCommand(ADMIN, LOADERS));

        verify(importer, never()).handle(any());
        assertThat(background).hasSize(1);
        background.poll().run();
        verify(importer).handle(of(LOADERS));
    }

    @Test
    void importsTheWholeSiteWhenNoSectionIsGiven() {
        when(importer.handle(of(null))).thenReturn(new ImportSummary(0, 0, 0, 0));

        handler.handle(new StartVektorImportCommand(ADMIN, null));
        background.poll().run();

        verify(importer).handle(of(null));
    }

    @Test
    void refusesASecondImportWhileOneRuns() {
        handler.handle(new StartVektorImportCommand(ADMIN, LOADERS));

        assertThatThrownBy(() -> handler.handle(new StartVektorImportCommand(ADMIN, null)))
                .isInstanceOfSatisfying(
                        ImportRunningException.class, e -> assertThat(e.code()).isEqualTo("import.running"));
        assertThat(background).hasSize(1);
    }

    @Test
    void letsTheNextImportStartOnceARunIsOver() {
        when(importer.handle(any())).thenReturn(new ImportSummary(0, 0, 0, 0));
        handler.handle(new StartVektorImportCommand(ADMIN, LOADERS));
        background.poll().run();

        handler.handle(new StartVektorImportCommand(ADMIN, LOADERS));

        assertThat(background).hasSize(1);
    }

    @Test
    void letsTheNextImportStartOnceARunHasFailed() {
        when(importer.handle(any())).thenThrow(new IllegalStateException("The site is down"));
        handler.handle(new StartVektorImportCommand(ADMIN, LOADERS));
        background.poll().run();

        handler.handle(new StartVektorImportCommand(ADMIN, LOADERS));

        assertThat(background).hasSize(1);
    }

    @Test
    void refusesAMemberWhoIsNotAnAdministrator() {
        assertThatThrownBy(() -> handler.handle(new StartVektorImportCommand(MEMBER, LOADERS)))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThat(background).isEmpty();
    }

    @Test
    void refusesAPublicVisitor() {
        assertThatThrownBy(() -> handler.handle(new StartVektorImportCommand(Actor.anonymous(), LOADERS)))
                .isInstanceOf(SignInRequiredException.class);
        assertThat(background).isEmpty();
    }
}
