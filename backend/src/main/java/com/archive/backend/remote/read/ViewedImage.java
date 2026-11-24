package com.archive.backend.remote.read;

/** A picture to look at: its media type, read from its bytes, and the bytes in base64. */
public record ViewedImage(String mediaType, String data) {}
