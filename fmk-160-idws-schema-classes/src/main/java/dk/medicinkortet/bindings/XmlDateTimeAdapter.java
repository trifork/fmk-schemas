package dk.medicinkortet.bindings;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class XmlDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
    private static final DateTimeFormatter ISO_LOCAL_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter ISO_OFFSET_DATE_TIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter ISO_LOCAL_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public LocalDateTime unmarshal(String value) throws Exception {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignore) {
            return OffsetDateTime.parse(value, ISO_OFFSET_DATE_TIME).toLocalDateTime();
        }
    }

    @Override
    public String marshal(LocalDateTime value) throws Exception {
        if (value == null) {
            return null;
        }
        return value.atOffset(ZoneOffset.UTC).format(ISO_OFFSET_DATE_TIME);
    }

}