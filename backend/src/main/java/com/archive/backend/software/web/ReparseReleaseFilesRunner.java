package com.archive.backend.software.web;

import com.archive.backend.software.app.ReparseReleaseFilesHandler;
import com.archive.backend.software.domain.ReleaseFilesReparsed;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/** Runs the explicit command that rebuilds every stored release file tree. */
@Component
@RequiredArgsConstructor
public class ReparseReleaseFilesRunner implements ApplicationRunner {

    public static final String COMMAND = "reparse-release-files";

    private final ReparseReleaseFilesHandler handler;
    private final ApplicationContext context;

    @Override
    public void run(ApplicationArguments args) {
        if (!args.getNonOptionArgs().contains(COMMAND)) {
            return;
        }
        int code = reparse();
        System.exit(SpringApplication.exit(context, () -> code));
    }

    private int reparse() {
        try {
            ReleaseFilesReparsed result = handler.handle();
            System.out.printf(
                    "Release files reparsed: %d, failed: %d (the warnings above name them).%n",
                    result.parsed(), result.failed());
            return result.failed() == 0 ? 0 : 1;
        } catch (RuntimeException e) {
            System.err.println("Release files could not be reparsed: " + e.getMessage());
            return 1;
        }
    }
}
