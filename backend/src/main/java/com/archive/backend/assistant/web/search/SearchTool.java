package com.archive.backend.assistant.web.search;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolPages;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.app.FullSearchHandler;
import com.archive.backend.search.app.FullSearchQuery;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.FoundKind;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** The full search of the site: people, groups and software by a word, as its search page shows them. */
@Component
@RequiredArgsConstructor
public class SearchTool implements ReadTool<SearchArguments> {

    private final FullSearchHandler handler;

    @Override
    public String name() {
        return "search";
    }

    @Override
    public String description() {
        return "The site's full search, as its search page shows it: people (by any label or the real name), groups"
                + " (by the name or any label) and software (by its title), best matches first, 50 per page. Labels of"
                + " nobody are not found here: use find_names.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "phrase": {"type": "string", "description": "The word or words to find."},
                    "kinds": {
                      "type": ["array", "null"],
                      "items": {"enum": ["author", "group", "software"]},
                      "description": "The kinds to search; all when not given."
                    },
                    "platform": {"type": ["string", "null"], "description": "Only of this platform (its code)."},
                    "page": {"type": ["integer", "null"], "description": "The page, the first is 1."}
                  },
                  "required": ["phrase"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<SearchArguments> argumentsType() {
        return SearchArguments.class;
    }

    @Override
    public Object read(ToolContext context, SearchArguments arguments) {
        return handler.handle(new FullSearchQuery(
                CatalogKeyword.of(arguments.phrase()),
                arguments.platform() == null ? null : new PlatformCode(arguments.platform()),
                kinds(arguments.kinds()),
                ToolPages.of(arguments.page(), ToolPages.DEFAULT_SIZE)));
    }

    private static Set<FoundKind> kinds(@org.jspecify.annotations.Nullable List<String> codes) {
        return codes == null || codes.isEmpty()
                ? EnumSet.allOf(FoundKind.class)
                : codes.stream().map(FoundKind::of).collect(Collectors.toSet());
    }
}
