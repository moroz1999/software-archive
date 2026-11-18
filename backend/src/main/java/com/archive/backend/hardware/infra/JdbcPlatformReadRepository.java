package com.archive.backend.hardware.infra;

import com.archive.backend.hardware.read.HardwareNameSql;
import com.archive.backend.hardware.read.PlatformItem;
import com.archive.backend.hardware.read.PlatformReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcPlatformReadRepository implements PlatformReadRepository {

    private final JdbcClient jdbc;

    @Override
    public List<PlatformItem> findAll(String language) {
        return jdbc.sql("SELECT code, name FROM (SELECT p.code, " + HardwareNameSql.platform("p.code")
                        + " AS name FROM platform p) named ORDER BY lower(name), code")
                .param("language", language)
                .query((rs, row) -> new PlatformItem(rs.getString("code"), rs.getString("name")))
                .list();
    }
}
