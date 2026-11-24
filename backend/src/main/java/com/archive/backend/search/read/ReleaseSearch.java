package com.archive.backend.search.read;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** The releases of the whole archive found by their own facts (docs/domain/ai-assistant.md#release-search). */
public interface ReleaseSearch {

    Page<FoundRelease> find(ReleaseSearchFilter filter, Pageable pageable);
}
