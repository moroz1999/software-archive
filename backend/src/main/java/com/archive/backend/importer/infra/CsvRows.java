package com.archive.backend.importer.infra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The rows of a CSV text by the names its first row gives the columns: fields apart by commas, a field in double
 * quotes holding commas, line breaks and doubled quotes as they are. A row shorter than the first leaves its last
 * columns empty.
 */
final class CsvRows {

    private static final char BOM = '﻿';

    private CsvRows() {}

    static List<Map<String, String>> parse(String text) {
        List<List<String>> records = records(!text.isEmpty() && text.charAt(0) == BOM ? text.substring(1) : text);
        if (records.isEmpty()) {
            return List.of();
        }
        List<String> header = records.getFirst();
        List<Map<String, String>> rows = new ArrayList<>();
        for (List<String> record : records.subList(1, records.size())) {
            Map<String, String> row = new HashMap<>();
            for (int i = 0; i < header.size(); i++) {
                row.put(header.get(i), i < record.size() ? record.get(i) : "");
            }
            rows.add(row);
        }
        return rows;
    }

    /** Every record of the text, the header first; a blank line is no record. */
    private static List<List<String>> records(String text) {
        List<List<String>> records = new ArrayList<>();
        List<String> record = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean any = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < text.length() && text.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    field.append(c);
                }
                continue;
            }
            switch (c) {
                case '"' -> {
                    quoted = true;
                    any = true;
                }
                case ',' -> {
                    record.add(field.toString());
                    field.setLength(0);
                    any = true;
                }
                case '\r' -> {}
                case '\n' -> {
                    if (any || field.length() > 0) {
                        record.add(field.toString());
                        records.add(record);
                    }
                    record = new ArrayList<>();
                    field.setLength(0);
                    any = false;
                }
                default -> {
                    field.append(c);
                    any = true;
                }
            }
        }
        if (any || field.length() > 0) {
            record.add(field.toString());
            records.add(record);
        }
        return records;
    }
}
