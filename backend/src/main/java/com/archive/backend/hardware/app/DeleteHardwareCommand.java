package com.archive.backend.hardware.app;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.user.domain.Actor;

public record DeleteHardwareCommand(Actor actor, HardwareCode code) {}
