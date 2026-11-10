package com.archive.backend.hardware.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.hardware.read.HardwareItem;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

/** Runs against the reference book of the migrations. */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcHardwareReadRepository.class})
class JdbcHardwareReadRepositoryIT {

    private static final List<String> GROUPS = List.of("computers", "storage", "dos", "sound", "controls", "expansion");

    @Autowired
    private JdbcHardwareReadRepository reads;

    @Test
    void readsTheHardwareOfThePlatformByGroupThenName() {
        List<HardwareItem> items = reads.findByPlatform("vector");

        assertThat(items).extracting(HardwareItem::code).contains("vector06c", "ay8910", "tape", "beeper");
        assertThat(items).extracting(HardwareItem::code).doesNotContain("bk0011m", "msxdos");
        assertThat(items).extracting(item -> GROUPS.indexOf(item.group())).isSorted();
        assertThat(items.stream().filter(item -> item.group().equals("sound")).map(HardwareItem::name))
                .isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER);
        assertThat(items).contains(new HardwareItem("ay8910", "AY-3-8910", "sound"));
    }

    @Test
    void readsNothingForAnUnknownPlatform() {
        assertThat(reads.findByPlatform("zx")).isEmpty();
    }
}
