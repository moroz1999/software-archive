package com.archive.backend.group.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
class JdbcGroupTypeReadRepositoryIT {

    @Autowired
    private JdbcGroupTypeReadRepository repository;

    @Test
    void listsTheReferenceBookTheSchemaBringsByCode() {
        assertThat(repository.findAll()).containsExactly("crack group", "demogroup", "game studio", "label");
    }
}
