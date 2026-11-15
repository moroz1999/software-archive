package com.archive.backend.software.app;

import com.archive.backend.label.domain.LabelId;

/** The software a label is credited on, for the page of the label. */
public record ListLabelSoftwareQuery(LabelId label) {}
