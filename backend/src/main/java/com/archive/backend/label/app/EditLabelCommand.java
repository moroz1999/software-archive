package com.archive.backend.label.app;

import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.LabelOwnerChoice;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Gives a label its name, its owner and its locations (docs/domain/authors.md#editing-a-label); no owner leaves it to
 * nobody.
 */
public record EditLabelCommand(
        Actor actor, LabelId id, LabelName name, @Nullable LabelOwnerChoice owner, List<LocationDraft> locations) {}
