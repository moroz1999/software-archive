package com.archive.backend.user.app;

import com.archive.backend.user.domain.ConfirmationLinkIssued;
import com.archive.backend.user.domain.ConfirmationMailer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Mails a confirmation link as soon as an account gets it, inside the change: a letter that cannot be sent leaves the
 * account as it was.
 */
@Service
@RequiredArgsConstructor
public class MailConfirmationLinkHandler {

    private final ConfirmationMailer mailer;

    @EventListener
    public void on(ConfirmationLinkIssued event) {
        mailer.send(event.to(), event.name(), event.token(), event.language());
    }
}
