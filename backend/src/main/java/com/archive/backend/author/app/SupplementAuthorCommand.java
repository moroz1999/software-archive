package com.archive.backend.author.app;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.RealName;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** What an import found of a person the archive has, to fill what they lack. */
public record SupplementAuthorCommand(
        Actor actor, AuthorId id, @Nullable RealName realName, List<LocationDraft> locations) {}
