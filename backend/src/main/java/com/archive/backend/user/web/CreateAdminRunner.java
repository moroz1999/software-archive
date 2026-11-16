package com.archive.backend.user.web;

import com.archive.backend.user.app.CreateAdministratorHandler;
import java.io.Console;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Runs {@link CreateAdminCommandLine} when the application is started with the {@code create-admin} argument, and
 * exits before the tasks of a started archive run (docs/features/accounts.md#first-administrator).
 */
@Component
@RequiredArgsConstructor
public class CreateAdminRunner implements ApplicationRunner {

    public static final String COMMAND = "create-admin";

    private final CreateAdministratorHandler handler;
    private final ApplicationContext context;

    @Override
    @SuppressWarnings("SystemConsoleNull") // the image runs Java 21, where it is null without a terminal
    public void run(ApplicationArguments args) {
        if (!args.getNonOptionArgs().contains(COMMAND)) {
            return;
        }
        Console console = System.console();
        int code;
        if (console == null) {
            System.err.println("create-admin asks in a terminal: run it with docker compose run, which opens one.");
            code = 1;
        } else {
            code = new CreateAdminCommandLine(handler).run(new ConsoleTerminal(console));
        }
        System.exit(SpringApplication.exit(context, () -> code));
    }

    private record ConsoleTerminal(Console console) implements CreateAdminCommandLine.Terminal {

        @Override
        public String readLine(String prompt) {
            String line = console.readLine("%s", prompt);
            return line == null ? "" : line;
        }

        @Override
        public String readPassword(String prompt) {
            char[] password = console.readPassword("%s", prompt);
            return password == null ? "" : new String(password);
        }

        @Override
        public void println(String line) {
            console.printf("%s%n", line);
        }
    }
}
