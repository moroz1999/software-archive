package com.archive.backend.assistant.web.tool;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/** The names of a new country, city or item of hardware: the Russian one always, the English one when known. */
public record NamesArguments(String ru, @Nullable String en) {

    public static final String SCHEMA = """
            {
              "type": "object",
              "properties": {
                "ru": {"type": "string", "minLength": 1, "description": "The name in Russian."},
                "en": {"type": ["string", "null"], "description": "The name in English, when known."}
              },
              "required": ["ru"],
              "additionalProperties": false
            }""";

    /** The names by the codes of their languages, as the forms send them. */
    public Map<String, String> byLanguage() {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("ru", ru);
        String english = Changes.text(en);
        if (english != null) {
            names.put("en", english);
        }
        return names;
    }
}
