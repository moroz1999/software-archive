package com.archive.backend.software.app;

import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.ScreenshotPictureRepository;
import com.archive.backend.software.domain.ScreenshotsDiscarded;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;

/** The storage of pictures and the events the screenshot use case tests look at. */
final class ScreenshotFixtures {

    private ScreenshotFixtures() {}

    /** Keeps what was stored and what was discarded. */
    static final class RecordingScreenshotFiles implements ScreenshotPictureRepository {

        final Map<ScreenshotId, ScreenshotImage> stored = new LinkedHashMap<>();
        final List<ScreenshotId> discarded = new ArrayList<>();

        @Override
        public void store(ScreenshotId id, ScreenshotImage image) {
            stored.put(id, image);
        }

        @Override
        public void discard(List<ScreenshotId> ids) {
            discarded.addAll(ids);
        }
    }

    /** Remembers every event published and hands the discarded screenshots to the handler deleting their files. */
    static final class RecordingEvents implements ApplicationEventPublisher {

        final List<Object> published = new ArrayList<>();
        private final DiscardScreenshotFilesHandler discard;

        RecordingEvents(ScreenshotPictureRepository files) {
            this.discard = new DiscardScreenshotFilesHandler(files);
        }

        @Override
        public void publishEvent(Object event) {
            published.add(event);
            if (event instanceof ScreenshotsDiscarded discarded) {
                discard.on(discarded);
            }
        }
    }
}
