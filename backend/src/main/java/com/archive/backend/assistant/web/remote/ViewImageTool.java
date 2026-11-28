package com.archive.backend.assistant.web.remote;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolArgumentsException;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.remote.app.ViewImageHandler;
import com.archive.backend.remote.app.ViewImageQuery;
import com.archive.backend.remote.domain.RemoteRequestRefusedException;
import com.archive.backend.remote.domain.RemoteUrl;
import com.archive.backend.remote.read.ViewedImage;
import com.archive.backend.software.app.GetScreenshotFileHandler;
import com.archive.backend.software.app.GetScreenshotFileQuery;
import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.read.ScreenshotFile;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** A picture to look at: of another site, or a screenshot of the archive. */
@Component
@RequiredArgsConstructor
public class ViewImageTool implements ReadTool<ViewImageArguments> {

    private final ViewImageHandler remote;
    private final GetScreenshotFileHandler screenshots;

    @Override
    public String name() {
        return "view_image";
    }

    @Override
    public String description() {
        return "Looks at a picture: of another site by its address (a PNG, JPEG, GIF, WebP or BMP of at most 5 MB), or a"
                + " screenshot of the archive by its id. Give one of them. Use it when unsure what a picture shows.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "url": {"type": ["string", "null"], "pattern": "^[hH][tT][tT][pP][sS]?://"},
                    "screenshotId": {"type": ["string", "null"], "format": "uuid"}
                  },
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<ViewImageArguments> argumentsType() {
        return ViewImageArguments.class;
    }

    @Override
    public Object read(ToolContext context, ViewImageArguments arguments) {
        String url = arguments.url();
        UUID screenshot = arguments.screenshotId();
        if (url != null && screenshot == null) {
            return forModel(remote.handle(new ViewImageQuery(new RemoteUrl(url))));
        }
        if (url != null || screenshot == null) {
            throw new ToolArgumentsException("Give url or screenshotId: one of them.");
        }
        ScreenshotFile file = screenshots.handle(new GetScreenshotFileQuery(new ScreenshotId(screenshot)));
        try {
            return forModel(new ViewedImage(
                    file.mediaType(), Base64.getEncoder().encodeToString(Files.readAllBytes(file.path()))));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the screenshot " + screenshot, e);
        }
    }

    /** The picture as the model can see it: it takes no BMP, so a BMP is shown as a PNG. */
    private static ViewedImage forModel(ViewedImage image) {
        if (!image.mediaType().equals("image/bmp")) {
            return image;
        }
        try {
            BufferedImage picture =
                    ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(image.data())));
            if (picture == null) {
                throw new RemoteRequestRefusedException("The BMP picture cannot be read.");
            }
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            ImageIO.write(picture, "png", png);
            return new ViewedImage("image/png", Base64.getEncoder().encodeToString(png.toByteArray()));
        } catch (IOException e) {
            throw new RemoteRequestRefusedException("The BMP picture cannot be read.");
        }
    }
}
