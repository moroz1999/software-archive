package com.archive.backend.importer.domain.vektor;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/**
 * A section of vektor-06c.ru by the code in its address, such as {@code zagruzchiki}.
 */
@ValueObject
public record VektorSection(String code) {

    public VektorSection {
        Objects.requireNonNull(code, "code");
    }
}
