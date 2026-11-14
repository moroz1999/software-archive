package com.archive.backend.importer.domain.run;

import com.archive.backend.importer.domain.vektor.VektorSection;
import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** What a run imports: a source archive, whole or one section of it. */
@ValueObject
public record ImportScope(String archive, @Nullable VektorSection section) {

    public ImportScope {
        Objects.requireNonNull(archive, "archive");
    }
}
