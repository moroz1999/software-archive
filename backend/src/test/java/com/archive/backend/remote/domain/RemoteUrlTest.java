package com.archive.backend.remote.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** An address of another site the archive may be asked to read. */
class RemoteUrlTest {

    @ParameterizedTest
    @ValueSource(strings = {"http://example.com/page", "https://example.com/files/game.zip?x=1", "HTTPS://Example.com"})
    void takesAnAbsoluteWebAddress(String url) {
        assertThat(new RemoteUrl(" " + url + " ").value()).isEqualTo(url);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "ftp://example.com/game.zip",
                "file:///etc/passwd",
                "gopher://example.com",
                "/files/game.zip",
                "example.com/page",
                "http://",
                "http://exa mple.com/"
            })
    void refusesAnythingElse(String url) {
        assertThatThrownBy(() -> new RemoteUrl(url))
                .isInstanceOf(InvalidValueException.class)
                .hasMessageContaining(url.strip());
    }
}
