package com.archive.backend.software.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.RuleViolationException;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class ScreenshotImageTest {

    private static final ScreenshotFileName NAME = new ScreenshotFileName("shot.png");

    @Test
    void tellsTheFormatByTheContent() {
        assertThat(ScreenshotImages.image(ScreenshotImages.png()).format()).isEqualTo(ScreenshotFormat.PNG);
        assertThat(ScreenshotImages.image(ScreenshotImages.jpeg()).format()).isEqualTo(ScreenshotFormat.JPEG);
        assertThat(ScreenshotImages.image(ScreenshotImages.gif()).format()).isEqualTo(ScreenshotFormat.GIF);
        assertThat(ScreenshotImages.image(ScreenshotImages.webp()).format()).isEqualTo(ScreenshotFormat.WEBP);
        assertThat(ScreenshotImages.image(ScreenshotImages.bmp()).format()).isEqualTo(ScreenshotFormat.BMP);
    }

    @Test
    void keepsTheBytesAsUploaded() throws IOException {
        try (InputStream content =
                ScreenshotImages.image(ScreenshotImages.gif()).content().open()) {
            assertThat(content.readAllBytes()).isEqualTo(ScreenshotImages.gif());
        }
    }

    @Test
    void refusesWhatIsNoPictureOfTheFormats() {
        assertThatThrownBy(() -> ScreenshotImages.image(ScreenshotImages.text()))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("screenshot.format-unsupported");
                    assertThat(e.field()).isEqualTo("files");
                });
        assertThatThrownBy(() -> ScreenshotImages.image(new byte[0]))
                .isInstanceOf(ScreenshotFormatUnsupportedException.class);
    }

    @Test
    void takesAPictureOfTenMegabytesAndNoMore() {
        assertThat(ScreenshotImages.image(ScreenshotImages.pngOf(ScreenshotImage.MAX_BYTES))
                        .format())
                .isEqualTo(ScreenshotFormat.PNG);
        assertThatThrownBy(() -> ScreenshotImages.image(ScreenshotImages.pngOf(ScreenshotImage.MAX_BYTES + 1)))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("screenshot.too-large");
                    assertThat(e.field()).isEqualTo("files");
                });
    }

    @Test
    void judgesThePictureByItsFirstBytesAndItsSizeWithoutReadingIt() {
        ScreenshotContent unread = () -> {
            throw new AssertionError("the content is read");
        };

        assertThat(ScreenshotImage.of(ScreenshotImages.png(), ScreenshotImage.MAX_BYTES, NAME, unread)
                        .format())
                .isEqualTo(ScreenshotFormat.PNG);
        assertThatThrownBy(
                        () -> ScreenshotImage.of(ScreenshotImages.png(), ScreenshotImage.MAX_BYTES + 1L, NAME, unread))
                .isInstanceOf(ScreenshotTooLargeException.class);
    }
}
