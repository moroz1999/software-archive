package com.archive.backend;

import com.archive.backend.user.web.CreateAdminRunner;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ArchiveCatalogApplication {

    /**
     * A command runs without the web server, the search worker and the logs, so only its questions reach the terminal.
     */
    private static final List<String> COMMAND_OPTIONS = List.of(
            "--spring.main.web-application-type=none",
            "--spring.main.banner-mode=off",
            "--search.worker.enabled=false",
            "--logging.level.root=WARN",
            "--spring.jpa.show-sql=false");

    public static void main(String[] args) {
        boolean command = Arrays.asList(args).contains(CreateAdminRunner.COMMAND);
        SpringApplication.run(
                ArchiveCatalogApplication.class,
                command
                        ? Stream.concat(Stream.of(args), COMMAND_OPTIONS.stream())
                                .toArray(String[]::new)
                        : args);
    }
}
