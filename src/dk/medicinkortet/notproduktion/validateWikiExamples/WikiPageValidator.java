package dk.medicinkortet.notproduktion.validateWikiExamples;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validate XML examples from DokuWiki. This class loads the given wiki page, finds all XML examples on the page,
 * downloads each XML,  and invokes
 * XML validation for each .
 *
 * Call for with one or more DokuWiki Sitemap pages as argument, for example:
 *
 * java ValidatePagesFromSitemap http://wiki.fmk.netic.dk/doku.php?id=fmk:1.4.6:fmk_1.4.6_snitflade&do=index
 */
public class WikiPageValidator {

    private static final Pattern linkPattern = Pattern.compile("<a href=\"([^\"]+)\" title=\"Download Snippet\" class=\"mediafile mf_xml\">([^<]+)</a>");
    private String assumedNamespace;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java ValidatePage <pageUrl> [<namespace>]");
            System.exit(1);
        }

        String url = args[0];
        String assumedNamespace = null;
        if (args.length > 1) {
            assumedNamespace = args[1];
        }
        boolean result;

        try {
            result = new WikiPageValidator(assumedNamespace).validatePage(url);
        } catch (Exception e) {
            System.err.println("Exception while processing page url: " + url);
            e.printStackTrace();
            result = false;
        }

        if (!result) {
            System.exit(1);
        }
    }

    public WikiPageValidator(String assumedNamespace) {
        this.assumedNamespace = assumedNamespace;
    }

    /** @return true for success, false if there were errors. */
    public boolean validatePage(String url) throws IOException {
        return validatePage(new URL(url));
    }

    /** @return true for success, false if there were errors. */
    public boolean validatePage(URL url) throws IOException {
        boolean result = true;

        // Read html file
        String sitemapHtml;
        try (InputStream in = url.openStream()) {
            sitemapHtml = IOUtils.toString(in);
        }

        XMLValidator xmlValidator = new XMLValidator(assumedNamespace);

        // Find page links, and validate each page
        Matcher m = linkPattern.matcher(sitemapHtml);
        while (m.find()) {
            //http://wiki.fmk.netic.dk/doku.php?do=export_code&id=fmk:1.4.6:bestilling&codeblock=0
            //http://wiki.fmk.netic.dk/doku.php?do=export_code&amp;id=fmk:1.4.6:bestilling&amp;codeblock=0

            String xmlRelativeUrl = m.group(1).replace("&amp;", "&");
            String filename = m.group(2);

            URL xmlURL = new URL(url, xmlRelativeUrl);
            System.out.println("  --- XML snippet '" + filename + "'"); //+ "' at url='" + xmlURL + "' relativeURL=" + xmlRelativeUrl);
            try {
                result = xmlValidator.validateXml(xmlURL) || result; // Rækkefølgen er ikke ligegyldig - hvis 1. arg er true så evalueres 2. arg ikke!
            } catch (Exception e) {
                System.out.println("Exception while processing XML snippet '" + filename + "' at url='" + xmlURL + "' relativeURL=" + xmlRelativeUrl);
                e.printStackTrace(System.out);
                result = false;
            }
        }

        return result;
    }
}
