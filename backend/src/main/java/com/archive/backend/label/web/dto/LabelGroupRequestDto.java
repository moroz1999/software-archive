package com.archive.backend.label.web.dto;

import java.util.List;
import org.jspecify.annotations.Nullable;

/** The types of the group made of a label; missing means none. */
public record LabelGroupRequestDto(@Nullable List<String> types) {}
