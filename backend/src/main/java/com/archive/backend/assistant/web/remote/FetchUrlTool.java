package com.archive.backend.assistant.web.remote;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.remote.app.FetchPageHandler;
import com.archive.backend.remote.app.FetchPageQuery;
import com.archive.backend.remote.domain.RemoteUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** A page of another site, as it came. */
@Component
@RequiredArgsConstructor
public class FetchUrlTool implements ReadTool<UrlArguments> {

    /** An http or https address, as the schemas of tools that take one have it. */
    public static final String URL = "{\"type\": \"string\", \"pattern\": \"^[hH][tT][tT][pP][sS]?://\"}";

    private final FetchPageHandler handler;

    @Override
    public String name() {
        return "fetch_url";
    }

    @Override
    public String description() {
        return "A page of another site: the address it came from after redirects, its status (an error page is read"
                + " too), its Content-Type and its body in UTF-8, cut at 200 KB (truncated then true). Content that is"
                + " not text gives only its type and size. Addresses inside the archive's own network are refused.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {"url": %s},
                  "required": ["url"],
                  "additionalProperties": false
                }
                """.formatted(URL);
    }

    @Override
    public Class<UrlArguments> argumentsType() {
        return UrlArguments.class;
    }

    @Override
    public Object read(ToolContext context, UrlArguments arguments) {
        return handler.handle(new FetchPageQuery(new RemoteUrl(arguments.url())));
    }
}
