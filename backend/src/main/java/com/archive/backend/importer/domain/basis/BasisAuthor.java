package com.archive.backend.importer.domain.basis;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * An author as their page on "Базис" shows them.
 *
 * @param name the full name of a person, or the name of a team or a firm
 * @param nickname the nickname the page gives apart from the name; many pages give none
 * @param place where the author is from, as the page writes it
 */
@ValueObject
public record BasisAuthor(
        String id,
        String name,
        @Nullable String nickname,
        @Nullable String place) {

    public BasisAuthor {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
    }
}
