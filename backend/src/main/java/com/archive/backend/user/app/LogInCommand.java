package com.archive.backend.user.app;

import com.archive.backend.user.domain.Login;
import com.archive.backend.user.domain.Password;
import com.archive.backend.user.domain.session.RememberMe;

public record LogInCommand(Login login, Password password, RememberMe remember) {}
