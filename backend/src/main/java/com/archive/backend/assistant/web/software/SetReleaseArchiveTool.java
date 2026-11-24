package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.entry.GetReleaseArguments;
import com.archive.backend.assistant.web.entry.GetReleaseTool;
import com.archive.backend.assistant.web.remote.FetchUrlTool;
import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.Changes;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.remote.app.DownloadFileHandler;
import com.archive.backend.remote.app.DownloadFileQuery;
import com.archive.backend.remote.domain.RemoteByteLimit;
import com.archive.backend.remote.domain.RemoteUrl;
import com.archive.backend.remote.read.DownloadedFile;
import com.archive.backend.software.app.UpdateReleaseHandler;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.web.mapper.ReleaseWebMapper;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Gives a release its archive from a file of another site, as the release form does; the rest of the release stays. */
@Component
@RequiredArgsConstructor
public class SetReleaseArchiveTool implements WriteTool<SetReleaseArchiveArguments> {

    private final GetReleaseTool entry;
    private final SoftwareForms forms;
    private final DownloadFileHandler downloads;
    private final UpdateReleaseHandler handler;
    private final ReleaseWebMapper mapper;

    @Override
    public String name() {
        return "set_release_archive";
    }

    @Override
    public String description() {
        return "Gives a release its archive from a file of another site, of at most 100 MB, in place of the one it has;"
                + " the rest of the release stays. Its name is fileName, else the name the site gives the file.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    %s
                    "url": %s,
                    "fileName": {"type": ["string", "null"], "description": "The name to keep it under; none: the site's."}
                  },
                  "required": ["softwareId", "releaseId", "url"],
                  "additionalProperties": false
                }
                """.formatted(SoftwareSchemas.IDS, FetchUrlTool.URL);
    }

    @Override
    public Class<SetReleaseArchiveArguments> argumentsType() {
        return SetReleaseArchiveArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.FILES;
    }

    @Override
    public String entityType() {
        return "release";
    }

    @Override
    public Object state(ToolContext context, SetReleaseArchiveArguments a) {
        return entry.read(context, new GetReleaseArguments(a.softwareId(), a.releaseId()));
    }

    @Override
    public Written write(ToolContext context, SetReleaseArchiveArguments a) {
        ReleaseState current = forms.release(context, a.softwareId(), a.releaseId());
        try (DownloadedFile file = downloads.handle(
                new DownloadFileQuery(new RemoteUrl(a.url()), new RemoteByteLimit(ReleaseArchiveUpload.MAX_BYTES)))) {
            String name = Changes.text(a.fileName());
            ReleaseArchiveUpload archive = new ReleaseArchiveUpload(
                    name == null ? file.fileName() : name, file.size(), () -> Files.newInputStream(file.path()));
            handler.handle(mapper.toUpdateWithArchive(
                    context.actor(),
                    a.softwareId(),
                    a.releaseId(),
                    SoftwareForms.request(current, SoftwareForms.currentCredits(current.credits())),
                    archive,
                    false));
        }
        return Written.of(a.releaseId());
    }
}
