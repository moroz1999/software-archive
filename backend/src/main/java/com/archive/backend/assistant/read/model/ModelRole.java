package com.archive.backend.assistant.read.model;

public enum ModelRole {
    SYSTEM,
    USER,
    ASSISTANT,
    /** The result of a tool call. */
    TOOL
}
