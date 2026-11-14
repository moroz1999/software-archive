package com.archive.backend.importer.app;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.archive.backend.importer.domain.ImportRunningException;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.vektor.VektorSection;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.SignInRequiredException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;

class StartVektorImportHandlerTest {

    private static final VektorSection LOADERS = new VektorSection("zagruzchiki");

    /** Runs nothing until the test lets the queued work run. */
    private final Deque<Runnable> background = new ArrayDeque<>();

    private final Executor executor = background::add;
    private final ImportVektorHandler importer = mock(ImportVektorHandler.class);
    private final StartVektorImportHandler handler = new StartVektorImportHandler(importer, executor);

    @Test
    void startsTheImportInTheBackgroundAndReturnsAtOnce() {
        when(importer.handle(new ImportVektorCommand(LOADERS))).thenReturn(new ImportSummary(1, 0, 0, 0));

        handler.handle(new StartVektorImportCommand(ADMIN, LOADERS));

        verify(importer, never()).handle(any());
        assertThat(background).hasSize(1);
        background.poll().run();
        verify(importer).handle(new ImportVektorCommand(LOADERS));
    }

    @Test
    void importsTheWholeSiteWhenNoSectionIsGiven() {
        when(importer.handle(new ImportVektorCommand(null))).thenReturn(new ImportSummary(0, 0, 0, 0));

        handler.handle(new StartVektorImportCommand(ADMIN, null));
        background.poll().run();

        verify(importer).handle(new ImportVektorCommand(null));
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
