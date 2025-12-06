package com.example.chat.Utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeFormatter {

    public static String format(Instant time) {
        if (time == null) return null;

        LocalDateTime dateTime = LocalDateTime.ofInstant(time, ZoneId.systemDefault());
        LocalDate today = LocalDate.now();
        LocalDate date = dateTime.toLocalDate();

        if (date.equals(today)) {
            return dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
        } else if (date.equals(today.minusDays(1))) {
            return "Yesterday";
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }
}
