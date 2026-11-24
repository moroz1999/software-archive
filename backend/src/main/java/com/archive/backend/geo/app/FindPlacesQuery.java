package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.PlaceNamePhrase;
import com.archive.backend.shared.domain.Language;

/** Countries and cities by a name of theirs in any language, named in the language given. */
public record FindPlacesQuery(PlaceNamePhrase name, Language language) {}
