package com.archive.backend.shared.read;

import org.jspecify.annotations.Nullable;

/**
 * An import an entry came from (docs/domain/imports.md#origin): the source archive, the id the entry has there — or
 * the name it is known by there, when the source gives it no id — and its page there, if it has one.
 */
public record ExternalOrigin(
        String origin, String externalId, @Nullable String url) {}
