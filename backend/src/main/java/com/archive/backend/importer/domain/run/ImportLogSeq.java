package com.archive.backend.importer.domain.run;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** The place of a line among the lines of the logs of every run: a later line has a greater one. */
@ValueObject
public record ImportLogSeq(long value) {}
