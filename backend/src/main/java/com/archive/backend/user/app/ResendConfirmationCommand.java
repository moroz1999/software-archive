package com.archive.backend.user.app;

import com.archive.backend.user.domain.InterfaceLanguage;
import com.archive.backend.user.domain.Login;
import com.archive.backend.user.domain.Password;

public record ResendConfirmationCommand(Login login, Password password, InterfaceLanguage language) {}
