package dk.medicinkortet.bindings;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class XmlDateTimeAdapter extends XmlAdapter<String, ZonedDateTime> {
    private static final DateTimeFormatter ISO_ZONED_DATE_TIME = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    @Override
    public ZonedDateTime unmarshal(String value) throws Exception {
        if (value == null || value.isEmpty()) {
            return null;
        }

        return ZonedDateTime.parse(value, ISO_ZONED_DATE_TIME);
    }

    @Override
    public String marshal(ZonedDateTime value) throws Exception {
        return value.toString();
    }

}