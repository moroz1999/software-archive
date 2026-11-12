package com.archive.backend.search.read;

import java.util.Set;
import org.jspecify.annotations.Nullable;

/** The full search over the kinds asked. Fails with an unchecked exception when the engine does not answer. */
public interface FullSearch {

    /** Page {@code number}, of {@code size} matches, of the keyword over the kinds, of the platform when one is given. */
    FullSearchResult find(String keyword, @Nullable String platform, Set<FoundType> kinds, int number, int size);
}
