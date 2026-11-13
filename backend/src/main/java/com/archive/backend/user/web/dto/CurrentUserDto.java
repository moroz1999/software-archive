package com.archive.backend.user.web.dto;

import java.util.List;
import java.util.UUID;

public record CurrentUserDto(UUID id, String name, String email, List<String> groups, List<String> abilities) {}
