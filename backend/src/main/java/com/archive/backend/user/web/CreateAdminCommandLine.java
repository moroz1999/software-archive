package com.archive.backend.user.web;

import com.archive.backend.shared.domain.error.BusinessException;
import com.archive.backend.user.app.CreateAdministratorCommand;
import com.archive.backend.user.app.CreateAdministratorHandler;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.Password;
import com.archive.backend.user.domain.UserName;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

/**
 * The {@code create-admin} command: asks the account of the first administrator in the terminal and creates it.
 */
@RequiredArgsConstructor
public class CreateAdminCommandLine {

    /** Where the command talks to whoever runs it. */
    public interface Terminal {

        String readLine(String prompt);

        /** Reads without echoing what is typed. */
        String readPassword(String prompt);

        void println(String line);
    }

    private final CreateAdministratorHandler handler;

    /**
     * The exit code of the command.
     */
    public int run(Terminal terminal) {
        String name = terminal.readLine("User name: ");
        String email = terminal.readLine("Email: ");
        String password = terminal.readPassword("Password: ");
        String repeated = terminal.readPassword("Repeat the password: ");
        if (!password.equals(repeated)) {
            terminal.println("The passwords do not match.");
            return 1;
        }
        try {
            UserName userName = new UserName(name);
            handler.handle(new CreateAdministratorCommand(userName, new Email(email), new Password(password)));
            terminal.println("Administrator " + userName.value() + " created.");
            return 0;
        } catch (BusinessException e) {
            terminal.println(Objects.requireNonNullElse(e.getMessage(), e.code()));
            return 1;
        }
    }
}
