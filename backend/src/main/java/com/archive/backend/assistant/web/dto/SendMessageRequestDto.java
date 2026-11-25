package com.archive.backend.assistant.web.dto;

import org.jspecify.annotations.Nullable;

public record SendMessageRequestDto(String text, @Nullable PageContextRequestDto page) {}
