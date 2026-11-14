package com.archive.backend.software.read;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record SoftwareCredit(UUID id, @Nullable UUID ownerId, String type, String title, List<String> roles) {}
