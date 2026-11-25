package com.archive.backend.assistant.read.chat;

/**
 * An entry of the archive a conversation mentions, as a link: its type ({@code software}, {@code release},
 * {@code author}, {@code group}, {@code label}, {@code country}, {@code city}, {@code hardware}), id, title and the
 * path of its page in the archive.
 */
public record EntityRef(String type, String id, String title, String path) {}
