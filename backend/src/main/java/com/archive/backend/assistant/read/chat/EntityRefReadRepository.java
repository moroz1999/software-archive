package com.archive.backend.assistant.read.chat;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Entries of the archive as links, found by their ids alone: ids are unique across the archive. */
public interface EntityRefReadRepository {

    /** Those of the ids that are entries; an id of nothing is left out. */
    List<EntityRef> byIds(Collection<UUID> ids);

    /** The hardware of the codes there is. */
    List<EntityRef> hardware(Collection<String> codes);
}
