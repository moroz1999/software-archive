package com.archive.backend.assistant.read.model;

/** A tool offered to the model: its name, what it does and the JSON Schema of its arguments. */
public record ModelTool(String name, String description, String schema) {}
