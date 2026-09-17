package dk.medicinkortet.xmlschema;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Builds a header-flavour variant of a 2022_01_01 WSDL by pairing a flavour prologue with the
 * canonical WSDL's body.
 *
 * The two flavours of the contract differ only in their DTD prologue: the SOAP header lists, the
 * header schema includes and the header message declarations are all entities, so everything below
 * the DTD is identical. Generating the variant therefore means concatenating a prologue with the
 * canonical body, and the operations only ever have to be written once.
 *
 * The output is written next to the canonical WSDL because relative schemaLocation paths
 * ("../schemas/...") resolve against the WSDL's own directory. It is a build artifact and is
 * gitignored.
 *
 * Properties: -Dcanonical -Dprologue -Dout
 */
public final class WsdlFlavourGenerator {

    private static final String DTD_END = "]>";

    private WsdlFlavourGenerator() {
    }

    public static void main(String[] args) throws IOException {
        Path canonical = path("canonical");
        Path prologue = path("prologue");
        Path out = path("out");

        String body = bodyOf(read(canonical), canonical);
        String head = read(prologue);
        if (!head.stripTrailing().endsWith(DTD_END)) {
            throw new IllegalStateException("Prologue does not end with " + DTD_END + ": " + prologue);
        }

        String generated = head.stripTrailing() + body;
        // Skip the write when nothing changed, so downstream steps can rely on timestamps.
        if (Files.exists(out) && read(out).equals(generated)) {
            System.out.println("Flavour WSDL already up to date: " + out);
            return;
        }
        Files.write(out, generated.getBytes(StandardCharsets.UTF_8));
        System.out.println("Generated flavour WSDL: " + out + " (prologue " + prologue.getFileName() + ")");
    }

    /** Everything from the end of the DOCTYPE declaration onwards, i.e. the part both flavours share. */
    private static String bodyOf(String wsdl, Path source) {
        int end = wsdl.indexOf(DTD_END);
        if (end < 0) {
            throw new IllegalStateException("No DTD prologue found in " + source);
        }
        return wsdl.substring(end + DTD_END.length());
    }

    private static Path path(String property) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required property: -D" + property);
        }
        return Paths.get(value);
    }

    private static String read(Path p) throws IOException {
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }
}
