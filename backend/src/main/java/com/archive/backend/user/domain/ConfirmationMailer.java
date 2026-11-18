package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.Language;

/**
 * Sends the confirmation link of a new account to its email.
 */
public interface ConfirmationMailer {

    void send(Email to, UserName name, ConfirmationToken token, Language language);
}
