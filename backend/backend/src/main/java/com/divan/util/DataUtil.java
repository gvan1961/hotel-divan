package com.divan.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DataUtil {

    public static final ZoneId ZONA = ZoneId.of("America/Maceio");

    public static LocalDate hoje() {
        return LocalDate.now(ZONA);
    }

    public static LocalDateTime agora() {
        return LocalDateTime.now(ZONA);
    }
}
