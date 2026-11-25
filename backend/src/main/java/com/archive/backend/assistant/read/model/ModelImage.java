package com.archive.backend.assistant.read.model;

/** A picture shown to the model: its media type and its bytes in Base64. */
public record ModelImage(String mediaType, String data) {}
