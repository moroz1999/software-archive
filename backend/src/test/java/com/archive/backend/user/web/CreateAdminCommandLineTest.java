package com.archive.backend.user.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.archive.backend.user.app.CreateAdministratorCommand;
import com.archive.backend.user.app.CreateAdministratorHandler;
import com.archive.backend.user.domain.AdministratorExistsException;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.Password;
import com.archive.backend.user.domain.UserEmailTakenException;
import com.archive.backend.user.domain.UserName;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateAdminCommandLineTest {

    private final CreateAdministratorHandler handler = mock(CreateAdministratorHandler.class);
    private final CreateAdminCommandLine command = new CreateAdminCommandLine(handler);

    @Test
    void createsTheAdministratorItWasToldOnceConfirmed() {
        ScriptedTerminal terminal = new ScriptedTerminal("Zed", "zed@example.com", "password1", "password1", "y");

        assertThat(command.run(terminal)).isZero();

        verify(handler)
                .handle(new CreateAdministratorCommand(
                        new UserName("Zed"), new Email("zed@example.com"), new Password("password1")));
        assertThat(terminal.prompts)
                .containsExactly(
                        "User name: ",
                        "Email: ",
                        "Password: ",
                        "Repeat the password: ",
                        "Create the administrator? [y/N] ");
        assertThat(terminal.passwordPrompts).containsExactly("Password: ", "Repeat the password: ");
        assertThat(terminal.output)
                .containsExactly("User name: Zed", "Email: zed@example.com", "Administrator Zed created.");
    }

    @Test
    void createsNothingUnlessConfirmed() {
        ScriptedTerminal terminal = new ScriptedTerminal("Zed", "zed@example.com", "password1", "password1", "");

        assertThat(command.run(terminal)).isOne();

        verify(handler, never()).handle(any());
        assertThat(terminal.output).containsExactly("User name: Zed", "Email: zed@example.com", "Nothing created.");
    }

    @Test
    void showsTheEmailAsItIsKept() {
        ScriptedTerminal terminal = new ScriptedTerminal("Zed", " ZED@Example.com ", "password1", "password1", "n");

        assertThat(command.run(terminal)).isOne();

        assertThat(terminal.output).contains("Email: zed@example.com");
    }

    @Test
    void refusesPasswordsThatDoNotMatch() {
        ScriptedTerminal terminal = new ScriptedTerminal("Zed", "zed@example.com", "password1", "password2");

        assertThat(command.run(terminal)).isOne();

        verify(handler, never()).handle(any());
        assertThat(terminal.output).containsExactly("The passwords do not match.");
    }

    @Test
    void tellsWhatIsWrongWithAValue() {
        ScriptedTerminal terminal = new ScriptedTerminal("Zed", "zed@example.com", "short", "short");

        assertThat(command.run(terminal)).isOne();

        verify(handler, never()).handle(any());
        assertThat(terminal.output).containsExactly("A password must be at least 8 characters long.");
    }

    @Test
    void tellsWhyTheArchiveRefused() {
        doThrow(new AdministratorExistsException()).when(handler).handle(any());

        ScriptedTerminal terminal = new ScriptedTerminal("Zed", "zed@example.com", "password1", "password1", "y");

        assertThat(command.run(terminal)).isOne();
        assertThat(terminal.output).last().isEqualTo("The archive already has an administrator.");
    }

    @Test
    void tellsATakenEmail() {
        doThrow(new UserEmailTakenException()).when(handler).handle(any());

        ScriptedTerminal terminal = new ScriptedTerminal("Zed", "zed@example.com", "password1", "password1", "y");

        assertThat(command.run(terminal)).isOne();
        assertThat(terminal.output).last().isEqualTo(new UserEmailTakenException().getMessage());
    }

    /** Answers the prompts in order and records what the command printed. */
    private static final class ScriptedTerminal implements CreateAdminCommandLine.Terminal {

        private final Deque<String> answers;
        final List<String> prompts = new ArrayList<>();
        final List<String> passwordPrompts = new ArrayList<>();
        final List<String> output = new ArrayList<>();

        ScriptedTerminal(String... answers) {
            this.answers = new ArrayDeque<>(List.of(answers));
        }

        @Override
        public String readLine(String prompt) {
            prompts.add(prompt);
            return answers.removeFirst();
        }

        @Override
        public String readPassword(String prompt) {
            prompts.add(prompt);
            passwordPrompts.add(prompt);
            return answers.removeFirst();
        }

        @Override
        public void println(String line) {
            output.add(line);
        }
    }
}
