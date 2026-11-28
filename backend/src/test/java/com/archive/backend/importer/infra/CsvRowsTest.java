package com.archive.backend.importer.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The lists of bk-catalog are CSV texts with a first row naming the columns. */
class CsvRowsTest {

    @Test
    void readsEveryRowByTheNamesOfTheColumns() {
        assertThat(CsvRows.parse("ID,Название,Год выпуска\n555901,Artillery 2026,2026\n48370,Adventure,\n"))
                .containsExactly(
                        Map.of("ID", "555901", "Название", "Artillery 2026", "Год выпуска", "2026"),
                        Map.of("ID", "48370", "Название", "Adventure", "Год выпуска", ""));
    }

    @Test
    void keepsCommasLineBreaksAndQuotesOfAFieldInQuotes() {
        List<Map<String, String>> rows =
                CsvRows.parse("ID,Описание\n1,\"Игра на Бейсике \"\"Вильнюс\"\",\nноябрь 2026\"\n");

        assertThat(rows).containsExactly(Map.of("ID", "1", "Описание", "Игра на Бейсике \"Вильнюс\",\nноябрь 2026"));
    }

    @Test
    void readsRowsEndedByCarriageReturnsAndALastRowWithoutAnEnd() {
        assertThat(CsvRows.parse("ID,Название\r\n1,Digger\r\n2,Tetris"))
                .containsExactly(Map.of("ID", "1", "Название", "Digger"), Map.of("ID", "2", "Название", "Tetris"));
    }

    @Test
    void skipsTheMarkOfTheEncodingBeforeTheFirstColumn() {
        assertThat(CsvRows.parse("﻿ID,Название\n1,Digger\n")).containsExactly(Map.of("ID", "1", "Название", "Digger"));
    }

    @Test
    void givesAShortRowItsMissingColumnsEmpty() {
        assertThat(CsvRows.parse("ID,Название,Жанр\n1,Digger\n"))
                .containsExactly(Map.of("ID", "1", "Название", "Digger", "Жанр", ""));
    }
}
