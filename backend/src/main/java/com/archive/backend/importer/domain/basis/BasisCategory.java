package com.archive.backend.importer.domain.basis;

import com.archive.backend.importer.domain.run.ImportSection;
import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.List;
import java.util.Objects;

/** A category of "Базис" by the code in its address, such as {@code arcade}. */
@ValueObject
public record BasisCategory(String code) implements ImportSection {

    public BasisCategory {
        Objects.requireNonNull(code, "code");
    }

    /** The locally known source categories whose cards can be software of the archive. */
    public static List<BasisCategory> importable() {
        return BasisCategories.SOURCE_CATEGORIES.stream()
                .filter(category -> !BasisCategories.LEFT_OUT.contains(category))
                .map(BasisCategory::new)
                .toList();
    }
}
