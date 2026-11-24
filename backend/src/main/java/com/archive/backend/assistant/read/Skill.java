package com.archive.backend.assistant.read;

/** A skill of the assistant: its name, when to load it, and its instructions. */
public record Skill(String name, String description, String instructions) {}
