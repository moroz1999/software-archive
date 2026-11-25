package com.archive.backend.assistant.web.chat;

/** Where the runs of the assistant go: on in the background, once the request that started them has answered. */
public interface RunLauncher {

    void launch(Runnable run);
}
