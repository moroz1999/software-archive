package com.archive.backend.hardware.web.dto;

import java.util.Map;

public record HardwareGroupDetailsDto(String code, Map<String, String> names) {}
