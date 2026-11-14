package com.archive.backend.importer.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunId;
import com.archive.backend.importer.domain.run.ImportRunRepository;
import com.archive.backend.importer.domain.run.ImportRunState;
import com.archive.backend.importer.domain.run.ImportScope;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StopUnfinishedImportRunsHandlerTest {

    private static final Instant NOW = Instant.parse("2026-11-14T12:00:00Z");

    private final ImportRunRepository runs = mock(ImportRunRepository.class);
    private final StopUnfinishedImportRunsHandler handler =
            new StopUnfinishedImportRunsHandler(runs, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void stopsTheRunsTheRestartCutShort() {
        ImportRun cut = ImportRun.start(
                new ImportRunId(UUID.randomUUID()),
                new ImportScope("vektor-06c.ru", null),
                Instant.parse("2026-11-14T10:00:00Z"));
        when(runs.findRunning()).thenReturn(List.of(cut));

        handler.on();

        verify(runs).save(cut);
        assertThat(cut.getState()).isEqualTo(ImportRunState.STOPPED);
        assertThat(cut.getEndedAt()).isEqualTo(NOW);
    }
}
