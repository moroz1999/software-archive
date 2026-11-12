package com.archive.backend.search.read;

import org.jspecify.annotations.Nullable;

/** The quick search over software, people and groups. Fails with an unchecked exception when the engine does not answer. */
public interface QuickSearch {

    /** At most {@code limit} first matches of the keyword in every section, of the platform when one is given. */
    QuickSearchResult find(String keyword, @Nullable String platform, int limit);
}
