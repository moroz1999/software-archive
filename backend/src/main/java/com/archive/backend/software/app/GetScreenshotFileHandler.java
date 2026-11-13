package com.archive.backend.software.app;

import com.archive.backend.software.domain.ScreenshotNotFoundException;
import com.archive.backend.software.read.ScreenshotFile;
import com.archive.backend.software.read.ScreenshotFileSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetScreenshotFileHandler {

    private final ScreenshotFileSource files;

    @Transactional(readOnly = true)
    public ScreenshotFile handle(GetScreenshotFileQuery q) {
        return files.find(q.id().value()).orElseThrow(ScreenshotNotFoundException::new);
    }
}
