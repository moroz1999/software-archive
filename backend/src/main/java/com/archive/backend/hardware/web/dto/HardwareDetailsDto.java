package com.archive.backend.hardware.web.dto;

import java.util.List;
import java.util.Map;

public record HardwareDetailsDto(String code, Map<String, String> names, String group, List<String> platforms) {}
