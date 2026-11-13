package com.archive.backend.software.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.RuleViolationException;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Test;

class ReleaseArchiveUploadTest {

    private static final ReleaseArchiveContent UNREAD = () -> new ByteArrayInputStream(new byte[0]);

    @Test
    void acceptsOneHundredMegabytesAndNoMoreWithoutReadingTheFile() {
        assertThat(new ReleaseArchiveUpload("release.zip", ReleaseArchiveUpload.MAX_BYTES, UNREAD).size())
                .isEqualTo(ReleaseArchiveUpload.MAX_BYTES);

        assertThatThrownBy(() -> new ReleaseArchiveUpload("release.zip", ReleaseArchiveUpload.MAX_BYTES + 1, UNREAD))
                .isInstanceOfSatisfying(RuleViolationException.class, error -> {
                    assertThat(error.code()).isEqualTo("release.archive-too-large");
                    assertThat(error.field()).isEqualTo("archive");
                });
    }

    @Test
    void refusesAFileWithoutAName() {
        assertThatThrownBy(() -> new ReleaseArchiveUpload("", 0, UNREAD))
                .isInstanceOfSatisfying(RuleViolationException.class, error -> {
                    assertThat(error.code()).isEqualTo("release.archive-name-invalid");
                    assertThat(error.field()).isEqualTo("archive");
                });
    }
}
