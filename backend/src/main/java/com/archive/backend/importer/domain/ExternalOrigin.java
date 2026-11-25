package com.archive.backend.importer.domain;

import com.archive.backend.label.domain.LabelName;
import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;

/**
 * Where an imported entry came from: the source archive, the kind of the entry and the id it has there; a name that
 * has no id there is known by the name itself (docs/domain/imports.md).
 */
@ValueObject
public record ExternalOrigin(String archive, ExternalKind kind, String externalId) {

    public ExternalOrigin {
        Objects.requireNonNull(archive, "archive");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(externalId, "externalId");
    }

    public static ExternalOrigin software(String archive, String id) {
        return new ExternalOrigin(archive, ExternalKind.SOFTWARE, id);
    }

    /**
     * Software taken from a page of another site: the site is the archive, known by its host without "www.", and the
     * address of the page is its id there.
     */
    public static ExternalOrigin softwarePage(ExternalPage page) {
        String host;
        try {
            host = new URI(page.url()).getHost();
        } catch (URISyntaxException e) {
            host = null;
        }
        if (host == null || host.isBlank()) {
            throw new InvalidValueException("software.page-invalid", "The address names no site.");
        }
        String site = host.toLowerCase(Locale.ROOT);
        return software(site.startsWith("www.") ? site.substring(4) : site, page.url());
    }

    public static ExternalOrigin label(String archive, LabelName name) {
        return new ExternalOrigin(archive, ExternalKind.LABEL, name.value());
    }

    public static ExternalOrigin author(String archive, LabelName name) {
        return new ExternalOrigin(archive, ExternalKind.AUTHOR, name.value());
    }
}
