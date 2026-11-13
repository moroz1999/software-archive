package com.archive.backend.user.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.archive.backend.shared.config.AuthProperties;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.InterfaceLanguage;
import com.archive.backend.user.domain.UserName;
import java.time.Duration;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class SmtpConfirmationMailerTest {

    private static final AuthProperties PROPERTIES = new AuthProperties(
            "a-test-secret-of-at-least-32-bytes-long",
            Duration.ofMinutes(15),
            "https://archive.example",
            "no-reply@archive.example",
            true);

    private final JavaMailSender sender = mock(JavaMailSender.class);
    private final SmtpConfirmationMailer mailer = new SmtpConfirmationMailer(sender, PROPERTIES);

    @Test
    void mailsTheLinkToTheConfirmationPage() {
        SimpleMailMessage letter = send(InterfaceLanguage.EN);

        assertThat(letter.getTo()).containsExactly("zed@example.com");
        assertThat(letter.getFrom()).isEqualTo("no-reply@archive.example");
        assertThat(letter.getSubject()).isNotBlank();
        assertThat(letter.getText())
                .contains("Zed")
                .contains("https://archive.example/verify-email?token=secret-token")
                .contains("7 days");
    }

    @Test
    void writesInTheLanguageOfTheInterface() {
        SimpleMailMessage letter = send(InterfaceLanguage.RU);

        assertThat(Objects.requireNonNull(letter.getSubject())).containsPattern("[а-яА-Я]");
        assertThat(letter.getText())
                .contains("https://archive.example/verify-email?token=secret-token")
                .contains("7 дней");
    }

    private SimpleMailMessage send(InterfaceLanguage language) {
        mailer.send(new Email("zed@example.com"), new UserName("Zed"), new ConfirmationToken("secret-token"), language);
        ArgumentCaptor<SimpleMailMessage> letter = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(letter.capture());
        return letter.getValue();
    }
}
