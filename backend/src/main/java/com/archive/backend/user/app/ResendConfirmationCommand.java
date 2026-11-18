package com.archive.backend.user.app;

import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.domain.Login;
import com.archive.backend.user.domain.Password;

public record ResendConfirmationCommand(Login login, Password password, Language language) {}
