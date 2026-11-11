package com.archive.backend.group.infra;

import com.archive.backend.group.read.GroupTypeReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcGroupTypeReadRepository implements GroupTypeReadRepository {

    private final JdbcClient jdbc;

    @Override
    public List<String> findAll() {
        return jdbc.sql("SELECT code FROM group_type ORDER BY code")
                .query(String.class)
                .list();
    }
}
