package com.archive.backend.assistant.web.label;

import com.archive.backend.assistant.web.tool.LocationArguments;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** The arguments of create_label. */
public record CreateLabelArguments(String name, @Nullable List<LocationArguments> locations) {}
