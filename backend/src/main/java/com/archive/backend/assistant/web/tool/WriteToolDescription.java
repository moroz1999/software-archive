package com.archive.backend.assistant.web.tool;

import com.fasterxml.jackson.databind.JsonNode;

/** A write tool as the model reads it before it puts it into a plan. */
public record WriteToolDescription(String name, String description, String kind, String entityType, JsonNode schema) {}
