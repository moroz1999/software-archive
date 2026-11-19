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
        List<HardwareItem> items = reads.findByPlatform("vector", "en");

        assertThat(items).extracting(HardwareItem::code).contains("vector06c", "krista2", "ay8910", "tape");
        assertThat(items).extracting(item -> GROUPS.indexOf(item.group())).isSorted();
        assertThat(items.stream()
                        .filter(item -> item.group().equals("computers"))
                        .map(HardwareItem::name))
                .containsExactly("Krista-2", "Vector-06C");
        assertThat(items).contains(new HardwareItem("ay8910", "AY-3-8910", "sound"));
    }

    @Test
    void readsTheHardwareOfThePlatformByGroupThenNameInRussian() {
        List<HardwareItem> items = reads.findByPlatform("vector", "ru");

        assertThat(items).extracting(item -> GROUPS.indexOf(item.group())).isSorted();
        assertThat(items.stream().filter(item -> item.group().equals("storage")).map(HardwareItem::name))
                .containsExactly("Дискета", "Магнитофон");
        assertThat(items).contains(new HardwareItem("vector06c", "Вектор-06Ц", "computers"));
    }

    @Test
    void readsNothingForAnUnknownPlatform() {
        assertThat(reads.findByPlatform("zx", "en")).isEmpty();
    }
}
