package com.archive.backend.search.app;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/** Runs the search worker in the background, unless {@code search.worker.enabled=false} (the tests drive it). */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "search.worker.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class SearchWorkerSchedule {

    private final ProcessSearchOutboxHandler worker;

    @Scheduled(fixedDelayString = "${search.worker.delay:1s}")
    public void run() {
        worker.handle();
    }
}
