package com.archive.backend.software.read;

import java.util.List;
import java.util.UUID;

public record SoftwareCredit(UUID id, String type, String title, List<String> roles) {}
