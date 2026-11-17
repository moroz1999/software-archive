package com.archive.backend.label.app;

import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.user.domain.Actor;
import java.util.List;

/** Adds a label of nobody under its name, with its locations (docs/domain/authors.md#label). */
public record CreateLabelCommand(Actor actor, LabelName name, List<LocationDraft> locations) {}
