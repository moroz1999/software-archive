package com.archive.backend.hardware.read;

import com.archive.backend.shared.read.NameInLanguageSql;

/** SQL of the names of platforms, hardware and its groups in the language of {@code :language}, else in Russian. */
public final class HardwareNameSql {

    private HardwareNameSql() {}

    /** The name of the platform with the code {@code code}, an SQL expression such as {@code p.code}. */
    public static String platform(String code) {
        return NameInLanguageSql.of("platform_name", "platform_code", "name", code);
    }

    /** The name of the hardware with the code {@code code}, an SQL expression such as {@code h.code}. */
    public static String hardware(String code) {
        return NameInLanguageSql.of("hardware_name", "hardware_code", "name", code);
    }

    /** The name of the group of hardware with the code {@code code}, an SQL expression such as {@code g.code}. */
    public static String group(String code) {
        return NameInLanguageSql.of("hardware_group_name", "group_code", "name", code);
    }
}
