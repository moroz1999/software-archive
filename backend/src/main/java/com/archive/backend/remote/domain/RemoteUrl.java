package com.archive.backend.remote.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;

/** An address of another site given from outside: absolute, {@code http} or {@code https}, with a host. */
@ValueObject
public record RemoteUrl(String value) {

    public RemoteUrl {
        value = Objects.requireNonNull(value, "value").strip();
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!(scheme.equals("http") || scheme.equals("https")) || uri.getHost() == null) {
                throw invalid(value);
            }
        } catch (URISyntaxException e) {
            throw invalid(value);
        }
    }

    private static InvalidValueException invalid(String value) {
        return new InvalidValueException("remote.url-invalid", "Not an http or https address: " + value);
    }
}
