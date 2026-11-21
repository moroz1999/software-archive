package com.archive.backend.importer.domain.run;

import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.shared.domain.ddd.ValueObject;
import org.jspecify.annotations.Nullable;

/**
 * Where a run is: at which program of the source, counted from 1, of how many when the source tells, and its page;
 * before the first one, still reading the list.
 */
@ValueObject
public record ImportProgress(
        int position, @Nullable Integer total, @Nullable ExternalPage page) {

    public static final ImportProgress READING_LIST = new ImportProgress(0, null, null);
}
