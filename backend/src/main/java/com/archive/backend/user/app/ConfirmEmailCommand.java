package com.archive.backend.user.app;

import com.archive.backend.user.domain.ConfirmationToken;

public record ConfirmEmailCommand(ConfirmationToken token) {}
