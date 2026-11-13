package com.archive.backend.importer.domain.vektor;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.shared.domain.ddd.ValueObject;
import org.jspecify.annotations.Nullable;

/**
 * Where a section of vektor-06c.ru goes in the archive: whether its programs are taken at all, and the category they
 * are put in, if any.
 */
@ValueObject
public record VektorPlacement(boolean imported, @Nullable CategoryId category) {}
