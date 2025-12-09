package dk.medicinkortet.bindings;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDate;

public class XmlDateAdapter  extends XmlAdapter<String, LocalDate> {

    @Override
    public LocalDate unmarshal(String v) throws Exception {
        if (v == null) {
            return null;
        }
        if (v.endsWith("Z")) {
            v = v.substring(0, v.length() - 1);
        }

        return LocalDate.parse(v);
    }

    @Override
    public String marshal(LocalDate v) throws Exception {
        if (v == null) {
            return null;
        }
        return v.toString();
    }
}