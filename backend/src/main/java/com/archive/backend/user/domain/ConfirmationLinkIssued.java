package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.ddd.DomainEvent;

/**
 * An account got a new confirmation link, to be mailed in the language of the interface.
 */
public record ConfirmationLinkIssued(Email to, UserName name, ConfirmationToken token, Language language)
        implements DomainEvent {}
