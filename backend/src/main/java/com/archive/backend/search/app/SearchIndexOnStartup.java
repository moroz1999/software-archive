package com.archive.backend.search.app;

import com.archive.backend.search.domain.SearchEntityType;
import com.archive.backend.search.domain.SearchIndex;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Builds the search collections on start when there are none yet, or when the start asks for it with
 * {@code search.rebuild=true} (docs/features/search.md#initial-load-and-rebuild). The archive starts without search
 * when the engine is down.
 */
@Component
@RequiredArgsConstructor
public class SearchIndexOnStartup {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexOnStartup.class);

    private final SearchIndex index;
    private final RebuildSearchIndexHandler rebuild;
    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        try {
            if (environment.getProperty("search.rebuild", Boolean.class, false)
                    || !index.exists(SearchEntityType.SOFTWARE)) {
                rebuild.handle();
            }
        } catch (RuntimeException e) {
            log.error("Search collections not checked on start", e);
        }
    }
}
