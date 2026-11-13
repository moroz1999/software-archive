package com.archive.backend.importer.cli;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.archive.backend.importer.app.ImportVektorCommand;
import com.archive.backend.importer.app.ImportVektorHandler;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.vektor.VektorSection;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class ImportVektorRunnerTest {

    private final ImportVektorHandler handler = mock(ImportVektorHandler.class);
    private final ImportVektorRunner runner = new ImportVektorRunner(handler);

    @Test
    void importsTheWholeSiteByDefault() {
        when(handler.handle(new ImportVektorCommand(null))).thenReturn(new ImportSummary(0, 0, 0, 0));

        runner.run(new DefaultApplicationArguments());

        verify(handler).handle(new ImportVektorCommand(null));
    }

    @Test
    void importsOneSectionWhenTheCategoryIsGiven() {
        ImportVektorCommand command = new ImportVektorCommand(new VektorSection("zagruzchiki"));
        when(handler.handle(command)).thenReturn(new ImportSummary(0, 0, 0, 0));

        runner.run(new DefaultApplicationArguments("--category=zagruzchiki"));

        verify(handler).handle(command);
    }
}
