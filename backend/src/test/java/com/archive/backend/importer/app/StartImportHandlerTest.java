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
import com.archive.backend.importer.domain.basis.BasisCategory;
import com.archive.backend.importer.domain.bkcatalog.BkCatalogSection;
import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunRepository;
import com.archive.backend.importer.domain.run.ImportRunState;
import com.archive.backend.importer.domain.run.ImportScope;
import com.archive.backend.importer.domain.run.ImportSource;
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

class StartImportHandlerTest {

    private static final VektorSection LOADERS = new VektorSection("zagruzchiki");

    /** Runs nothing until the test lets the queued work run. */
    private final Deque<Runnable> background = new ArrayDeque<>();

    private final Executor executor = background::add;
    private final ImportVektorHandler importer = mock(ImportVektorHandler.class);
    private final ImportBasisHandler basisImporter = mock(ImportBasisHandler.class);
    private final ImportBkCatalogHandler bkCatalogImporter = mock(ImportBkCatalogHandler.class);
    private final ImportRunRepository runs = mock(ImportRunRepository.class);
    private final EndImportRunHandler endRun = mock(EndImportRunHandler.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-14T10:00:00Z"), ZoneOffset.UTC);
    private final StartImportHandler handler =
            new StartImportHandler(importer, basisImporter, bkCatalogImporter, runs, endRun, clock, executor);

    private static ImportVektorCommand of(@Nullable VektorSection section) {
        return argThat(command -> Objects.equals(command.section(), section));
    }

    @Test
    void writesDownTheRunBeforeItStarts() {
        handler.handle(new StartImportCommand(ADMIN, ImportSource.VEKTOR, LOADERS));

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
        handler.handle(new StartImportCommand(ADMIN, ImportSource.VEKTOR, LOADERS));
        ArgumentCaptor<ImportRun> saved = ArgumentCaptor.forClass(ImportRun.class);
        verify(runs).save(saved.capture());

        background.poll().run();

        verify(endRun).handle(new EndImportRunCommand(saved.getValue().getId(), ImportRunState.STOPPED));
    }

    @Test
    void startsTheImportInTheBackgroundAndReturnsAtOnce() {
        when(importer.handle(of(LOADERS))).thenReturn(new ImportSummary(1, 0, 0, 0));

        handler.handle(new StartImportCommand(ADMIN, ImportSource.VEKTOR, LOADERS));

        verify(importer, never()).handle(any());
        assertThat(background).hasSize(1);
        background.poll().run();
        verify(importer).handle(of(LOADERS));
    }

    @Test
    void importsTheWholeSiteWhenNoSectionIsGiven() {
        when(importer.handle(of(null))).thenReturn(new ImportSummary(0, 0, 0, 0));

        handler.handle(new StartImportCommand(ADMIN, ImportSource.VEKTOR, null));
        background.poll().run();

        verify(importer).handle(of(null));
    }

    @Test
    void refusesASecondImportWhileOneRuns() {
        handler.handle(new StartImportCommand(ADMIN, ImportSource.VEKTOR, LOADERS));

        assertThatThrownBy(() -> handler.handle(new StartImportCommand(ADMIN, ImportSource.VEKTOR, null)))
                .isInstanceOfSatisfying(
                        ImportRunningException.class, e -> assertThat(e.code()).isEqualTo("import.running"));
        assertThat(background).hasSize(1);
    }

    @Test
    void letsTheNextImportStartOnceARunIsOver() {
        when(importer.handle(any())).thenReturn(new ImportSummary(0, 0, 0, 0));
        handler.handle(new StartImportCommand(ADMIN, ImportSource.VEKTOR, LOADERS));
        background.poll().run();

        handler.handle(new StartImportCommand(ADMIN, ImportSource.VEKTOR, LOADERS));

        assertThat(background).hasSize(1);
    }

    @Test
    void letsTheNextImportStartOnceARunHasFailed() {
        when(importer.handle(any())).thenThrow(new IllegalStateException("The site is down"));
        handler.handle(new StartImportCommand(ADMIN, ImportSource.VEKTOR, LOADERS));
        background.poll().run();

        handler.handle(new StartImportCommand(ADMIN, ImportSource.VEKTOR, LOADERS));

        assertThat(background).hasSize(1);
    }

    @Test
    void refusesAMemberWhoIsNotAnAdministrator() {
        assertThatThrownBy(() -> handler.handle(new StartImportCommand(MEMBER, ImportSource.VEKTOR, LOADERS)))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThat(background).isEmpty();
    }

    @Test
    void refusesAPublicVisitor() {
        assertThatThrownBy(
                        () -> handler.handle(new StartImportCommand(Actor.anonymous(), ImportSource.VEKTOR, LOADERS)))
                .isInstanceOf(SignInRequiredException.class);
        assertThat(background).isEmpty();
    }

    @Test
    void importsTheWholeCardIndexOfBasis() {
        handler.handle(new StartImportCommand(ADMIN, ImportSource.BASIS, null));

        ArgumentCaptor<ImportRun> saved = ArgumentCaptor.forClass(ImportRun.class);
        verify(runs).save(saved.capture());
        assertThat(saved.getValue().getScope()).isEqualTo(new ImportScope("caglrc.cc", null));
        background.poll().run();
        verify(basisImporter).handle(new ImportBasisCommand(saved.getValue().getId(), null));
        verify(importer, never()).handle(any());
    }

    @Test
    void importsOneCategoryOfBasisAndWritesItInTheRunScope() {
        BasisCategory arcade = new BasisCategory("arcade");

        handler.handle(new StartImportCommand(ADMIN, ImportSource.BASIS, arcade));

        ArgumentCaptor<ImportRun> saved = ArgumentCaptor.forClass(ImportRun.class);
        verify(runs).save(saved.capture());
        assertThat(saved.getValue().getScope()).isEqualTo(new ImportScope("caglrc.cc", arcade));
        background.poll().run();
        verify(basisImporter).handle(new ImportBasisCommand(saved.getValue().getId(), arcade));
        verify(importer, never()).handle(any());
    }

    @Test
    void importsOneSectionOfBkCatalogAndWritesItInTheRunScope() {
        handler.handle(new StartImportCommand(ADMIN, ImportSource.BK_CATALOG, BkCatalogSection.GAMES));

        ArgumentCaptor<ImportRun> saved = ArgumentCaptor.forClass(ImportRun.class);
        verify(runs).save(saved.capture());
        assertThat(saved.getValue().getScope()).isEqualTo(new ImportScope("bk-catalog", BkCatalogSection.GAMES));
        background.poll().run();
        verify(bkCatalogImporter)
                .handle(new ImportBkCatalogCommand(saved.getValue().getId(), BkCatalogSection.GAMES));
        verify(importer, never()).handle(any());
    }

    @Test
    void refusesAnImportOfOneSourceWhileAnotherRuns() {
        handler.handle(new StartImportCommand(ADMIN, ImportSource.VEKTOR, null));

        assertThatThrownBy(() -> handler.handle(new StartImportCommand(ADMIN, ImportSource.BASIS, null)))
                .isInstanceOf(ImportRunningException.class);
        assertThat(background).hasSize(1);
    }
}
