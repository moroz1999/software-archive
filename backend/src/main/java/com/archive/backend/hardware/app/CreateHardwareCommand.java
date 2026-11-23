package com.archive.backend.hardware.app;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.HardwareGroupCode;
import com.archive.backend.hardware.domain.HardwareNames;
import com.archive.backend.hardware.domain.HardwarePlatforms;
import com.archive.backend.user.domain.Actor;

public record CreateHardwareCommand(
        Actor actor, HardwareCode code, HardwareNames names, HardwareGroupCode group, HardwarePlatforms platforms) {}
