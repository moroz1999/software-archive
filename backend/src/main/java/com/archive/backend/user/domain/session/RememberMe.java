package com.archive.backend.user.domain.session;

import com.archive.backend.shared.domain.ddd.ValueObject;

/**
 * Whether the session outlives the browser.
 */
@ValueObject
public record RememberMe(boolean value) {}
