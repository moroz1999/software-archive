package com.archive.backend.author.app;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.AuthorNotes;
import com.archive.backend.author.domain.RealName;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record UpdateAuthorCommand(
        Actor actor,
        AuthorId id,
        @Nullable RealName realName,
        @Nullable AuthorNotes notes,
        List<LabelDraft> labels,
        List<LocationDraft> locations) {}
