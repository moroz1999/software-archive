package com.archive.backend.search.app;

import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

/**
 * Keeps the worker and the rebuild apart: the worker waits while the collections are built anew
 * (docs/features/search.md#initial-load-and-rebuild).
 */
@Component
public class SearchWorkerPause {

    private final ReentrantLock lock = new ReentrantLock();

    public void run(Runnable work) {
        lock.lock();
        try {
            work.run();
        } finally {
            lock.unlock();
        }
    }
}
