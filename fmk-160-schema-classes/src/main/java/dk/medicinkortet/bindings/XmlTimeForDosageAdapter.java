package dk.medicinkortet.bindings;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class XmlTimeForDosageAdapter extends XmlAdapter<String, LocalTime> {

    private static final DateTimeFormatter DOSAGE_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public LocalTime unmarshal(String v) {
        if (v == null) {
            return null;
        }

        try {
            return LocalTime.parse(v);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("ParseError: doseringsklokkeslæt skal angives i formatet HH:mm:ss");
        }
    }

    @Override
    public String marshal(LocalTime v) {
        return v == null ? null : v.format(DOSAGE_TIME_FORMAT);
    }
}
