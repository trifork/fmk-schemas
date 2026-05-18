package dk.medicinkortet.bindings;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.format.DateTimeParseException;

public class XmlTimeAdapter extends XmlAdapter<String, LocalTime> {

    @Override
    public LocalTime unmarshal(String v) {
        if (v == null) return null;

        try {
            return LocalTime.parse(v);
        } catch (DateTimeParseException e) {
            return OffsetTime.parse(v).toLocalTime();
        }
    }

    @Override
    public String marshal(LocalTime v) {
        if (v == null) return null;

        return v.toString();
    }
}
