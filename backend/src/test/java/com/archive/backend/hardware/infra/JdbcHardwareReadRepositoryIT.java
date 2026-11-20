package com.archive.backend.hardware.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.hardware.read.HardwareItem;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/** Runs against the reference book of the migrations. */
@IntegrationTest
@Transactional
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
                .containsExactlyInAnyOrder("Krista-2", "PK-6128C", "Vector Start-1200", "Vector-06C");
        assertThat(items).contains(new HardwareItem("ay8910", "AY-3-8910", "sound"));
    }

    @Test
    void readsTheHardwareOfThePlatformByGroupThenNameInRussian() {
        List<HardwareItem> items = reads.findByPlatform("vector", "ru");

        assertThat(items).extracting(item -> GROUPS.indexOf(item.group())).isSorted();
        assertThat(items.stream().filter(item -> item.group().equals("storage")).map(HardwareItem::name))
                .containsExactlyInAnyOrder("Дискета", "Магнитофон", "Жёсткий диск", "Квазидиск", "Квазидиск Баркаря");
        assertThat(items).contains(new HardwareItem("vector06c", "Вектор-06Ц", "computers"));
    }

    @Test
    void hasTheWholeVectorFamilyInEnglishAndRussian() {
        assertThat(reads.findByPlatform("vector", "en"))
                .contains(
                        new HardwareItem("pk6128c", "PK-6128C", "computers"),
                        new HardwareItem("start1200", "Vector Start-1200", "computers"),
                        new HardwareItem("hdd", "Hard disk", "storage"),
                        new HardwareItem("kvaz", "Quasi-disk", "storage"),
                        new HardwareItem("barkar", "Barkar's quasi-disk", "storage"),
                        new HardwareItem("beeper", "Beeper", "sound"),
                        new HardwareItem("vi53", "VI53 timer sound", "sound"),
                        new HardwareItem("joystick-p", "Joystick P", "controls"),
                        new HardwareItem("joystick-s", "Joystick S", "controls"),
                        new HardwareItem("z80", "Z80 processor", "expansion"));
        assertThat(reads.findByPlatform("vector", "ru"))
                .contains(
                        new HardwareItem("pk6128c", "ПК-6128Ц", "computers"),
                        new HardwareItem("start1200", "Вектор Старт-1200", "computers"),
                        new HardwareItem("hdd", "Жёсткий диск", "storage"),
                        new HardwareItem("kvaz", "Квазидиск", "storage"),
                        new HardwareItem("barkar", "Квазидиск Баркаря", "storage"),
                        new HardwareItem("beeper", "Бипер", "sound"),
                        new HardwareItem("vi53", "Звук таймера ВИ53", "sound"),
                        new HardwareItem("joystick-p", "Джойстик П", "controls"),
                        new HardwareItem("joystick-s", "Джойстик С", "controls"),
                        new HardwareItem("z80", "Процессор Z80", "expansion"));
    }

    @Test
    void readsNothingForAnUnknownPlatform() {
        assertThat(reads.findByPlatform("zx", "en")).isEmpty();
    }
}
