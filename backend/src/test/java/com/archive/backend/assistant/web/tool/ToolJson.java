package com.archive.backend.assistant.web.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.List;

/** A tool's result as the model reads it: its JSON. */
public final class ToolJson {

    private static final JsonMapper JSON =
            JsonMapper.builder().findAndAddModules().build();

    private ToolJson() {}

    /** The values a path finds, as a list. */
    public static List<Object> list(DocumentContext json, String path) {
        List<Object> values = json.read(path);
        return values;
    }

    public static DocumentContext of(Object result) {
        try {
            return JsonPath.parse(JSON.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
