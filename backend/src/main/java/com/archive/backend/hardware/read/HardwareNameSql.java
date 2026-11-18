package com.archive.backend.hardware.read;

import com.archive.backend.shared.read.NameInLanguageSql;

/** SQL of the names of platforms and hardware in the language of the {@code :language} parameter, else in Russian. */
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
}
