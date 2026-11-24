package com.archive.backend.assistant.web.tool;

/** What kind of change a write tool makes, as a plan shows it. */
public enum ChangeKind {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LINK("link"),
    UNLINK("unlink"),
    ATTACH("attach"),
    DETACH("detach"),
    MERGE("merge"),
    CONVERT("convert");

    private final String code;

    ChangeKind(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
