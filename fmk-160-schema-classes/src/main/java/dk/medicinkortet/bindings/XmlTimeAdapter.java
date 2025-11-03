package dk.medicinkortet.bindings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Helper used by JAXB bindings to convert between XML Schema date/time
 * representations and java.time classes.
 */
public final class XmlTimeAdapter {

    private static final DateTimeFormatter ISO_LOCAL_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter ISO_OFFSET_DATE_TIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter ISO_LOCAL_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private XmlTimeAdapter() {
        // Utility class, not meant for instantiation
    }

    public static LocalDateTime parseDateTime(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignore) {
            return OffsetDateTime.parse(value, ISO_OFFSET_DATE_TIME).toLocalDateTime();
        }
    }

    public static String printDateTime(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atOffset(ZoneOffset.UTC).format(ISO_OFFSET_DATE_TIME);
    }

    public static LocalDate parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return LocalDate.parse(value, ISO_LOCAL_DATE);
    }

    public static String printDate(LocalDate value) {
        if (value == null) {
            return null;
        }
        return value.format(ISO_LOCAL_DATE);
    }
}
