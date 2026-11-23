package com.archive.backend.software.app;

import com.archive.backend.label.domain.LabelId;

/** The releases a label is credited on, for its page. */
public record ListLabelReleasesQuery(LabelId label) {}
