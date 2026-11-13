package com.archive.backend.user.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.user.domain.Password;
import com.archive.backend.user.domain.PasswordHash;
import org.junit.jupiter.api.Test;

class BcryptPasswordHasherTest {

    private final BcryptPasswordHasher hasher = new BcryptPasswordHasher();

    @Test
    void keepsABcryptHashOnly() {
        PasswordHash hash = hasher.hash(new Password("password1"));

        assertThat(hash.value()).startsWith("$2").doesNotContain("password1");
        assertThat(hasher.hash(new Password("password1"))).isNotEqualTo(hash);
    }

    @Test
    void checksAPasswordAgainstItsHash() {
        PasswordHash hash = hasher.hash(new Password("password1"));

        assertThat(hasher.matches(new Password("password1"), hash)).isTrue();
        assertThat(hasher.matches(new Password("password2"), hash)).isFalse();
    }
}
