package dk.medicinkortet.bindings;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class XmlDateTimeAdapter extends XmlAdapter<String, OffsetDateTime> {
    private static final DateTimeFormatter ISO_OFFSET_DATE_TIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public OffsetDateTime unmarshal(String value) throws Exception {
        if (value == null || value.isEmpty()) {
            return null;
        }

        return OffsetDateTime.parse(value, ISO_OFFSET_DATE_TIME);
    }

    @Override
    public String marshal(OffsetDateTime value) throws Exception {
        return value.toString();
    }

}