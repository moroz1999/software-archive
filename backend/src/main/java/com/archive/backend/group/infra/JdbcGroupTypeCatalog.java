package com.archive.backend.group.infra;

import com.archive.backend.group.domain.GroupTypeCatalog;
import com.archive.backend.group.domain.GroupTypeCode;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcGroupTypeCatalog implements GroupTypeCatalog {

    private final JdbcClient jdbc;

    @Override
    public boolean allExist(Set<GroupTypeCode> codes) {
        Integer known = jdbc.sql("SELECT count(*) FROM group_type WHERE code IN (:codes)")
                .param("codes", codes.stream().map(GroupTypeCode::value).toList())
                .query(Integer.class)
                .single();
        return known == codes.size();
    }
}
