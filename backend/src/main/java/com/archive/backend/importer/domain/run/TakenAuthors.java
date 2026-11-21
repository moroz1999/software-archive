package com.archive.backend.importer.domain.run;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.List;

/** The authors one program made a run take for the first time, and the places it added for them. */
@ValueObject
public record TakenAuthors(List<TakenAuthor> authors, List<AddedPlace> places) {

    public static final TakenAuthors NONE = new TakenAuthors(List.of(), List.of());

    public TakenAuthors {
        authors = List.copyOf(authors);
        places = List.copyOf(places);
    }
}
