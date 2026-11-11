package com.archive.backend.group.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcGroupTypeReadRepository.class})
class JdbcGroupTypeReadRepositoryIT {

    @Autowired
    private JdbcGroupTypeReadRepository repository;

    @Test
    void listsTheReferenceBookTheSchemaBringsByCode() {
        assertThat(repository.findAll()).containsExactly("crack group", "demogroup", "game studio", "label");
    }
}
