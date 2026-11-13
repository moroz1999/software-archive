package com.archive.backend.importer.cli;

import com.archive.backend.importer.app.ImportVektorCommand;
import com.archive.backend.importer.app.ImportVektorHandler;
import com.archive.backend.importer.domain.vektor.VektorSection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Runs the import of vektor-06c.ru on start with the {@code import-vektor} profile; {@code --category=<code>} limits
 * it to one section of the site. The profile starts no web server and no search worker, so the application stops
 * when the import is done.
 */
@Component
@Profile("import-vektor")
@RequiredArgsConstructor
public class ImportVektorRunner implements ApplicationRunner {

    private final ImportVektorHandler handler;

    @Override
    public void run(ApplicationArguments args) {
        List<String> category = args.getOptionValues("category");
        VektorSection section = category == null || category.isEmpty() ? null : new VektorSection(category.getFirst());
        handler.handle(new ImportVektorCommand(section));
    }
}
