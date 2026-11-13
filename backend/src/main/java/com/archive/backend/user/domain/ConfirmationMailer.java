package com.archive.backend.user.domain;

/**
 * Sends the confirmation link of a new account to its email.
 */
public interface ConfirmationMailer {

    void send(Email to, UserName name, ConfirmationToken token, InterfaceLanguage language);
}
