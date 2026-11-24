package com.archive.backend.assistant.web.tool;

/** The pieces of the schemas the write tools share. */
public final class WriteTools {

    /** The locations of a person, a group or a label: given, they replace them. */
    public static final String LOCATIONS = """
            {"type": ["array", "null"], "items": %s, "description": "Every place; given, they replace them."}""".formatted(LocationArguments.SCHEMA);

    private WriteTools() {}
}
