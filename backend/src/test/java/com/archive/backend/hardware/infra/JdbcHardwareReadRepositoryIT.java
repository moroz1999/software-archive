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
        assertThat(items).contains(new HardwareItem("ay8910", "AY-3-8910", "sound", "Sound"));
    }

    @Test
    void readsTheHardwareOfThePlatformByGroupThenNameInRussian() {
        List<HardwareItem> items = reads.findByPlatform("vector", "ru");

        assertThat(items).extracting(item -> GROUPS.indexOf(item.group())).isSorted();
        assertThat(items.stream().filter(item -> item.group().equals("storage")).map(HardwareItem::name))
                .containsExactlyInAnyOrder("Дискета", "Магнитофон", "Жёсткий диск", "Квазидиск", "Квазидиск Баркаря");
        assertThat(items).contains(new HardwareItem("vector06c", "Вектор-06Ц", "computers", "Компьютеры"));
    }

    @Test
    void hasTheWholeVectorFamilyInEnglishAndRussian() {
        assertThat(reads.findByPlatform("vector", "en"))
                .contains(
                        new HardwareItem("pk6128c", "PK-6128C", "computers", "Computers"),
                        new HardwareItem("start1200", "Vector Start-1200", "computers", "Computers"),
                        new HardwareItem("hdd", "Hard disk", "storage", "Storage"),
                        new HardwareItem("kvaz", "Quasi-disk", "storage", "Storage"),
                        new HardwareItem("barkar", "Barkar's quasi-disk", "storage", "Storage"),
                        new HardwareItem("beeper", "Beeper", "sound", "Sound"),
                        new HardwareItem("vi53", "VI53 timer sound", "sound", "Sound"),
                        new HardwareItem("joystick-p", "Joystick P", "controls", "Controls"),
                        new HardwareItem("joystick-s", "Joystick S", "controls", "Controls"),
                        new HardwareItem("z80", "Z80 processor", "expansion", "Expansion"));
        assertThat(reads.findByPlatform("vector", "ru"))
                .contains(
                        new HardwareItem("pk6128c", "ПК-6128Ц", "computers", "Компьютеры"),
                        new HardwareItem("start1200", "Вектор Старт-1200", "computers", "Компьютеры"),
                        new HardwareItem("hdd", "Жёсткий диск", "storage", "Носители"),
                        new HardwareItem("kvaz", "Квазидиск", "storage", "Носители"),
                        new HardwareItem("barkar", "Квазидиск Баркаря", "storage", "Носители"),
                        new HardwareItem("beeper", "Бипер", "sound", "Звук"),
                        new HardwareItem("vi53", "Звук таймера ВИ53", "sound", "Звук"),
                        new HardwareItem("joystick-p", "Джойстик П", "controls", "Управление"),
                        new HardwareItem("joystick-s", "Джойстик С", "controls", "Управление"),
                        new HardwareItem("z80", "Процессор Z80", "expansion", "Расширения"));
    }

    @Test
    void readsTheHardwareOfTheCodesByGroupLeavingOutCodesTheReferenceBookDoesNotHave() {
        assertThat(reads.findByCodes(List.of("ay8910", "zx-interface", "vector06c"), "en"))
                .containsExactly(
                        new HardwareItem("vector06c", "Vector-06C", "computers", "Computers"),
                        new HardwareItem("ay8910", "AY-3-8910", "sound", "Sound"));
        assertThat(reads.findByCodes(List.of("vector06c"), "ru"))
                .containsExactly(new HardwareItem("vector06c", "Вектор-06Ц", "computers", "Компьютеры"));
        assertThat(reads.findByCodes(List.of(), "en")).isEmpty();
    }

    @Test
    void readsNothingForAnUnknownPlatform() {
        assertThat(reads.findByPlatform("zx", "en")).isEmpty();
    }
}
