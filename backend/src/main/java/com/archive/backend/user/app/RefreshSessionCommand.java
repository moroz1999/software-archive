package com.archive.backend.user.app;

import com.archive.backend.user.domain.session.RefreshSecret;

public record RefreshSessionCommand(RefreshSecret secret) {}
