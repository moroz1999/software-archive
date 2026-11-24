package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.software.app.DeleteScreenshotHandler;
import com.archive.backend.software.app.ListScreenshotsHandler;
import com.archive.backend.software.domain.ScreenshotNotFoundException;
import com.archive.backend.software.read.SoftwareScreenshot;
import com.archive.backend.software.web.mapper.ScreenshotWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Deletes a screenshot of software or of its release. */
@Component
@RequiredArgsConstructor
public class DeleteScreenshotTool implements WriteTool<DeleteScreenshotArguments> {

    private final ListScreenshotsHandler screenshots;
    private final DeleteScreenshotHandler handler;
    private final ScreenshotWebMapper mapper;

    @Override
    public String name() {
        return "delete_screenshot";
    }

    @Override
    public String description() {
        return "Deletes a screenshot of software, or of its release when releaseId is given.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "softwareId": {"type": "string", "format": "uuid"},
                    "releaseId": {"type": ["string", "null"], "format": "uuid", "description": "Of a release; none: of the software itself."},
                    "screenshotId": {"type": "string", "format": "uuid"}
                  },
                  "required": ["softwareId", "screenshotId"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<DeleteScreenshotArguments> argumentsType() {
        return DeleteScreenshotArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.DELETE_FILE;
    }

    @Override
    public String entityType() {
        return "software";
    }

    @Override
    public SoftwareScreenshot state(ToolContext context, DeleteScreenshotArguments a) {
        return screenshots.handle(mapper.toList(a.softwareId(), a.releaseId())).stream()
                .filter(screenshot -> screenshot.id().equals(a.screenshotId()))
                .findFirst()
                .orElseThrow(ScreenshotNotFoundException::new);
    }

    @Override
    public Written write(ToolContext context, DeleteScreenshotArguments a) {
        handler.handle(mapper.toDelete(context.actor(), a.softwareId(), a.releaseId(), a.screenshotId()));
        return Written.of(a.softwareId());
    }
}
