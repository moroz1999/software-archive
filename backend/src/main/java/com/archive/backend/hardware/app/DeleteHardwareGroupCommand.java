package com.archive.backend.hardware.app;

import com.archive.backend.hardware.domain.HardwareGroupCode;
import com.archive.backend.user.domain.Actor;

public record DeleteHardwareGroupCommand(Actor actor, HardwareGroupCode code) {}
