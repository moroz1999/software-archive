package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.remote.FetchUrlTool;
import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolArgumentsException;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.remote.app.DownloadFileHandler;
import com.archive.backend.remote.app.DownloadFileQuery;
import com.archive.backend.remote.domain.RemoteByteLimit;
import com.archive.backend.remote.domain.RemoteUrl;
import com.archive.backend.remote.read.DownloadedFile;
import com.archive.backend.software.app.AddScreenshotsCommand;
import com.archive.backend.software.app.AddScreenshotsHandler;
import com.archive.backend.software.app.ListScreenshotsHandler;
import com.archive.backend.software.domain.Gallery;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ScreenshotFileName;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.read.SoftwareScreenshot;
import com.archive.backend.software.web.mapper.ScreenshotWebMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** Adds screenshots of software or of its release from pictures of another site, as the upload of screenshots does. */
@Component
@RequiredArgsConstructor
public class AddScreenshotsTool implements WriteTool<AddScreenshotsArguments> {

    private final ListScreenshotsHandler screenshots;
    private final DownloadFileHandler downloads;
    private final AddScreenshotsHandler handler;
    private final ScreenshotWebMapper mapper;

    @Override
    public String name() {
        return "add_screenshots";
    }

    @Override
    public String description() {
        return "Adds screenshots to software, or to its release when releaseId is given, from pictures of another site: a"
                + " PNG, JPEG, GIF or WebP of at most 10 MB each, all of them or none. The plan shows the screenshots"
                + " there are.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "softwareId": {"type": "string", "format": "uuid"},
                    "releaseId": {"type": ["string", "null"], "format": "uuid", "description": "Of a release; none: of the software itself."},
                    %s
                    "urls": {"type": "array", "minItems": 1, "maxItems": 20, "items": %s}
                  },
                  "required": ["softwareId", "urls"],
                  "additionalProperties": false
                }
                """.formatted(SoftwareSchemas.GALLERY, FetchUrlTool.URL);
    }

    @Override
    public Class<AddScreenshotsArguments> argumentsType() {
        return AddScreenshotsArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.FILES;
    }

    @Override
    public String entityType() {
        return "software";
    }

    @Override
    public List<SoftwareScreenshot> state(ToolContext context, AddScreenshotsArguments a) {
        return screenshots.handle(mapper.toList(a.softwareId(), a.releaseId(), gallery(a.releaseId(), a.gallery())));
    }

    @Override
    public Written write(ToolContext context, AddScreenshotsArguments a) {
        List<DownloadedFile> files = new ArrayList<>();
        try {
            for (String url : a.urls()) {
                files.add(downloads.handle(
                        new DownloadFileQuery(new RemoteUrl(url), new RemoteByteLimit(ScreenshotImage.MAX_BYTES))));
            }
            handler.handle(new AddScreenshotsCommand(
                    context.actor(),
                    new SoftwareId(a.softwareId()),
                    a.releaseId() == null ? null : new ReleaseId(a.releaseId()),
                    files.stream().map(AddScreenshotsTool::image).toList(),
                    gallery(a.releaseId(), a.gallery())));
        } finally {
            files.forEach(DownloadedFile::close);
        }
        UUID release = a.releaseId();
        return Written.of(release == null ? a.softwareId() : release);
    }

    /** The gallery the arguments name; a release has screenshots only. */
    static Gallery gallery(@Nullable UUID release, @Nullable String code) {
        Gallery gallery = ScreenshotWebMapper.gallery(code);
        if (release != null && gallery != Gallery.SCREENSHOTS) {
            throw new ToolArgumentsException("A release has screenshots only: give the gallery without the releaseId.");
        }
        return gallery;
    }

    /** The download judged by its first bytes and its size, streamed from where it lies to where it is kept. */
    private static ScreenshotImage image(DownloadedFile file) {
        try (InputStream in = Files.newInputStream(file.path())) {
            return ScreenshotImage.of(
                    in.readNBytes(ScreenshotImage.HEAD_BYTES),
                    file.size(),
                    new ScreenshotFileName(file.fileName()),
                    () -> Files.newInputStream(file.path()));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the download " + file.fileName(), e);
        }
    }
}
