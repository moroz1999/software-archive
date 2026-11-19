package com.archive.backend.software.web.mapper;

import com.archive.backend.software.app.AddScreenshotsCommand;
import com.archive.backend.software.app.DeleteScreenshotCommand;
import com.archive.backend.software.app.ListScreenshotsQuery;
import com.archive.backend.software.app.ReorderScreenshotsCommand;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ScreenshotFileName;
import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.read.SoftwareScreenshot;
import com.archive.backend.software.web.dto.ScreenshotDto;
import com.archive.backend.software.web.dto.ScreenshotOrderDto;
import com.archive.backend.user.domain.Actor;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** Screenshots between the requests and answers of the API and the use cases. */
@Component
public class ScreenshotWebMapper {

    /** Where the picture of a screenshot is served. */
    public static String url(UUID id) {
        return "/api/screenshots/" + id;
    }

    public ScreenshotDto toDto(UUID id) {
        return new ScreenshotDto(id, url(id));
    }

    public List<ScreenshotDto> toDtos(List<SoftwareScreenshot> screenshots) {
        return screenshots.stream().map(screenshot -> toDto(screenshot.id())).toList();
    }

    public List<ScreenshotDto> toAdded(List<ScreenshotId> ids) {
        return ids.stream().map(id -> toDto(id.value())).toList();
    }

    public ListScreenshotsQuery toList(UUID softwareId, @Nullable UUID releaseId) {
        return new ListScreenshotsQuery(new SoftwareId(softwareId), release(releaseId));
    }

    /** Every file is checked before any is added: an upload is taken whole or not at all. */
    public AddScreenshotsCommand toAdd(
            Actor actor, UUID softwareId, @Nullable UUID releaseId, List<MultipartFile> files) {
        return new AddScreenshotsCommand(
                actor,
                new SoftwareId(softwareId),
                release(releaseId),
                files.stream().map(ScreenshotWebMapper::image).toList());
    }

    public ReorderScreenshotsCommand toReorder(
            Actor actor, UUID softwareId, @Nullable UUID releaseId, ScreenshotOrderDto order) {
        return new ReorderScreenshotsCommand(
                actor,
                new SoftwareId(softwareId),
                release(releaseId),
                order.order().stream().map(ScreenshotId::new).toList());
    }

    public DeleteScreenshotCommand toDelete(Actor actor, UUID softwareId, @Nullable UUID releaseId, UUID screenshotId) {
        return new DeleteScreenshotCommand(
                actor, new SoftwareId(softwareId), release(releaseId), new ScreenshotId(screenshotId));
    }

    private static @Nullable ReleaseId release(@Nullable UUID releaseId) {
        return releaseId == null ? null : new ReleaseId(releaseId);
    }

    /**
     * The upload judged by its first bytes and its size; its content is streamed from the part the server keeps, never
     * read into memory whole.
     */
    private static ScreenshotImage image(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return ScreenshotImage.of(
                    in.readNBytes(ScreenshotImage.HEAD_BYTES),
                    file.getSize(),
                    new ScreenshotFileName(Objects.requireNonNullElse(file.getOriginalFilename(), "")),
                    file::getInputStream);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the uploaded file " + file.getOriginalFilename(), e);
        }
    }
}
