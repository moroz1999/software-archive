package com.archive.backend.hardware.infra;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.hardware.domain.PlatformRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcPlatformRepository implements PlatformRepository {

    private final JdbcClient jdbc;

    @Override
    public boolean exists(PlatformCode code) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM platform WHERE code = :code)")
                .param("code", code.value())
                .query(Boolean.class)
                .single();
    }
}
