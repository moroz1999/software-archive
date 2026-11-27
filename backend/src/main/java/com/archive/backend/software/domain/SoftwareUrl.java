package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** An absolute web address linked from the software's page. */
@ValueObject
public record SoftwareUrl(String value) {

    public SoftwareUrl {
        Objects.requireNonNull(value, "value");
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                throw invalid();
            }
        } catch (URISyntaxException e) {
            throw invalid();
        }
    }

    public static @Nullable SoftwareUrl ofNullable(@Nullable String value) {
        return value == null ? null : new SoftwareUrl(value);
    }

    private static InvalidValueException invalid() {
        return new InvalidValueException(
                "software.page-invalid", "A homepage must be an absolute HTTP or HTTPS address.");
    }
}
