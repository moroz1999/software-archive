package com.archive.backend.assistant.read.model;

/** A call of a tool the model wrote: its id, the tool and the JSON arguments. */
public record ModelToolCall(String id, String name, String arguments) {}
