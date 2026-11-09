package com.archive.backend.software.app;

import org.springframework.data.domain.Pageable;

/** The most recently added software; only the page size of the pageable counts. */
public record ListLatestSoftwareQuery(Pageable pageable) {
}
