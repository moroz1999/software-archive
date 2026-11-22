package com.archive.backend.importer.read;

import org.jspecify.annotations.Nullable;

/**
 * A page of the list of programs a run read.
 *
 * @param number the number of the page, from 1
 * @param pages how many pages the list has, when the source tells
 * @param programs how many programs the page lists
 */
public record ImportRunListReadEntry(
        String url, int number, @Nullable Integer pages, int programs) implements ImportRunLogEntry {}
