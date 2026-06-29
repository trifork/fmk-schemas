package dk.medicinkortet.bindings;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class XmlDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
    private static final DateTimeFormatter ISO_OFFSET_DATE_TIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final ZoneId COPENHAGEN_ZONE_ID = ZoneId.of("Europe/Copenhagen");

    @Override
    public LocalDateTime unmarshal(String value) throws Exception {
        if (value == null || value.isEmpty()) {
            return null;
        }

        return LocalDateTime.parse(value, ISO_OFFSET_DATE_TIME);
    }

    @Override
    public String marshal(LocalDateTime value) throws Exception {
        if (value == null) {
            return null;
        }

        return value.atZone(COPENHAGEN_ZONE_ID)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toOffsetDateTime()
                .format(ISO_OFFSET_DATE_TIME);
    }

}