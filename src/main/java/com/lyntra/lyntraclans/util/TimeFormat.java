package com.lyntra.lyntraclans.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public final class TimeFormat {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")
            .withZone(ZoneId.systemDefault());

    private TimeFormat() {
    }

    public static String format(long epochMillis) {
        return FORMATTER.format(Instant.ofEpochMilli(epochMillis));
    }

    public static long daysSince(long epochMillis) {
        long diff = System.currentTimeMillis() - epochMillis;
        return TimeUnit.MILLISECONDS.toDays(Math.max(diff, 0));
    }
}
