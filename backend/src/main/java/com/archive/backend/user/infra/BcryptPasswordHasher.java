package com.archive.backend.user.infra;

import com.archive.backend.user.domain.Password;
import com.archive.backend.user.domain.PasswordHash;
import com.archive.backend.user.domain.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public PasswordHash hash(Password password) {
        return new PasswordHash(encoder.encode(password.value()));
    }

    @Override
    public boolean matches(Password password, PasswordHash hash) {
        return encoder.matches(password.value(), hash.value());
    }
}
