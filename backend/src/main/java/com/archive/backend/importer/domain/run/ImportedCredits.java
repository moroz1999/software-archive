package com.archive.backend.importer.domain.run;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import java.util.List;

/** The credits of the authors of a program, and the authors and places the import took for them the first time. */
@ValueObject
public record ImportedCredits(List<SoftwareCreditDraft> credits, TakenAuthors taken) {

    public ImportedCredits {
        credits = List.copyOf(credits);
    }
}
