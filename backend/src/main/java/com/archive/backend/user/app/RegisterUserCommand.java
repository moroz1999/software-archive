package com.archive.backend.user.app;

import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.InterfaceLanguage;
import com.archive.backend.user.domain.Password;
import com.archive.backend.user.domain.UserName;

public record RegisterUserCommand(UserName name, Email email, Password password, InterfaceLanguage language) {}
