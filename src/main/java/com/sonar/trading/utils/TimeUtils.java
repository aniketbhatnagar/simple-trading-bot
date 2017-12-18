package com.sonar.trading.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    public static long toTimestamp(ZonedDateTime dateTime) {
        return dateTime.toInstant().toEpochMilli();
    }

    public static String formatTimestamp(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return DateTimeFormatter.ISO_DATE_TIME.format(localDateTime);
    }
}
