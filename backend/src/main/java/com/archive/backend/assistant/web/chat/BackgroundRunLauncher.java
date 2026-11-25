package com.archive.backend.assistant.web.chat;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** The runs go to the application's pool of background threads, as imports do. */
@Component
public class BackgroundRunLauncher implements RunLauncher {

    private final Executor executor;

    public BackgroundRunLauncher(@Qualifier("applicationTaskExecutor") Executor executor) {
        this.executor = executor;
    }

    @Override
    public void launch(Runnable run) {
        executor.execute(run);
    }
}
