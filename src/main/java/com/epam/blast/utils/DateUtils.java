package com.epam.blast.utils;

import java.time.Clock;
import java.time.LocalDateTime;

public final class DateUtils {

    private DateUtils() {
        //no op
    }

    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(Clock.systemUTC());
    }
}
