package com.archive.backend.importer.read;

import org.jspecify.annotations.Nullable;

/**
 * Where a run is.
 *
 * @param position the program it goes to, counted from 1; 0 while it reads the list
 * @param total how many programs there are, when the source tells
 * @param url the page of that program in the source
 */
public record ImportRunProgressEntry(
        int position, @Nullable Integer total, @Nullable String url) {}
