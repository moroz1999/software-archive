package com.archive.backend.importer.domain;

import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserId;
import java.util.Set;
import java.util.UUID;

/**
 * The fixed user of the archive who adds whatever an import adds (docs/domain/imports.md); the schema creates the
 * account.
 */
public final class Importer {

    public static final UserId ID = new UserId(UUID.fromString("00000000-0000-0000-0000-00000000a001"));
    public static final Actor ACTOR = new Actor(ID, Set.of(UserGroup.MEMBER));

    private Importer() {}
}
