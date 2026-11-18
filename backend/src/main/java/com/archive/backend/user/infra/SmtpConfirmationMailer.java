package com.archive.backend.user.infra;

import com.archive.backend.shared.config.AuthProperties;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.domain.ConfirmationMailer;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.UserName;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * A plain-text letter in the interface language, with the link to the confirmation page of the frontend.
 */
@Component
@RequiredArgsConstructor
public class SmtpConfirmationMailer implements ConfirmationMailer {

    private final JavaMailSender sender;
    private final AuthProperties properties;

    @Override
    public void send(Email to, UserName name, ConfirmationToken token, Language language) {
        String link = properties.publicUrl() + "/verify-email?token="
                + URLEncoder.encode(token.value(), StandardCharsets.UTF_8);
        SimpleMailMessage letter = new SimpleMailMessage();
        letter.setFrom(properties.mailFrom());
        letter.setTo(to.value());
        switch (language) {
            case RU -> {
                letter.setSubject("Подтверждение регистрации");
                letter.setText("Здравствуйте, %s!%n%nЧтобы подтвердить регистрацию, откройте ссылку:%n%s%n%n"
                                .formatted(name.value(), link)
                        + "Ссылка действует 7 дней. Если вы не регистрировались, просто проигнорируйте это письмо.");
            }
            case EN -> {
                letter.setSubject("Confirm your registration");
                letter.setText("Hello, %s!%n%nTo confirm your registration, open the link:%n%s%n%n"
                                .formatted(name.value(), link)
                        + "The link works for 7 days. If you did not register, just ignore this letter.");
            }
        }
        sender.send(letter);
    }
}
