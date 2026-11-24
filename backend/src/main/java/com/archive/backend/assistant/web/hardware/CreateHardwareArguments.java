package com.archive.backend.assistant.web.hardware;

import com.archive.backend.assistant.web.tool.NamesArguments;
import java.util.List;

/** The arguments of create_hardware. */
public record CreateHardwareArguments(String code, NamesArguments names, String group, List<String> platforms) {}
