package com.archive.backend.importer.domain.vektor;

import com.archive.backend.shared.domain.ddd.ValueObject;

/**
 * A program in a list of vektor-06c.ru: its id on the site and the absolute address of its page.
 */
@ValueObject
public record VektorListing(String id, String url) {}
