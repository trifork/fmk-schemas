package dk.medicinkortet.notproduktion.validateWikiExamples;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validate XML examples from DokuWiki. This class starts from a Sitemap page, finds all referenced pages and invokes
 * ValidatePage for each page.
 *
 * Call for with one or more DokuWiki Sitemap pages as argument:
 *   java ValidatePagesFromSitemap <sitemapURL> [<namespace>]
 *
 * Example:
 *   java ValidatePagesFromSitemap http://wiki.fmk.netic.dk/doku.php?id=fmk:1.4.6:fmk_1.4.6_snitflade&do=index http://www.dkma.dk/medicinecard/xml.schema/2015/06/01
 */
public class WikiSitemapValidator {

    // Match line like this:
    // <li class="level3"><div class="li"><a href="/doku.php?id=fmk:1.4.6:aendringer_foretaget_samtidigt_med_en_udlevering" class="wikilink1" title="fmk:1.4.6:aendringer_foretaget_samtidigt_med_en_udlevering">aendringer_foretaget_samtidigt_med_en_udlevering</a></div></li>
    private static final Pattern linkPattern = Pattern.compile("<a href=\"([^\"]+)\" class=\"wikilink1\" title=\"([^\"]+)\">([^<]+)</a>");

    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println("Usage: java ValidatePagesFromSitemap <sitemapURL> [<namespace>]");
            System.exit(1);
        }

        WikiSitemapValidator instance = new WikiSitemapValidator();

        String url = args[0];
        String assumedNamespace = (args.length > 1) ? args[1] : null;

        boolean result;
        try {
            System.out.println("--------------------------------------\nProcessing Sitemap: " + url);
            result = instance.validatePagesFromSitemap(url, assumedNamespace);
        } catch (Exception e) {
            System.out.println("Exception while processing Sitemap url: " + url);
            e.printStackTrace(System.out);
            result = false;
        }

        if (!result) {
            System.exit(1);
        }
    }

    /** @return true for success, false if there were errors. */
    public boolean validatePagesFromSitemap(String sitemapURLStr, String assumedNamespace) throws IOException {
        WikiPageValidator pageValidator = new WikiPageValidator(assumedNamespace);
        boolean result = true;

        // Read html file
        String sitemapHtml;
        URL sitemapURL = new URL(sitemapURLStr);
        try (InputStream in = sitemapURL.openStream()) {
            sitemapHtml = IOUtils.toString(in);
        }

        // Find page links, and validate each page
        Matcher m = linkPattern.matcher(sitemapHtml);
        while (m.find()) {
            String pageUrlStr = m.group(1);
            String id = m.group(2);
            String text = m.group(3);

            if ("start".equals(id) || "wiki".equals(id)) {
                continue;
            }

            URL pageURL = new URL(sitemapURL, pageUrlStr);
            System.out.println("*** Process page '" + text + "' at url='" + pageURL + "'");
            try {
                result = pageValidator.validatePage(pageURL) || result; // Rækkefølgen er ikke ligegyldig - hvis 1. arg er true så evalueres 2. arg ikke!
            } catch (Exception e) {
                System.out.println("Exception while processing page '" + text + "' at url='" + pageURL + "'");
                e.printStackTrace(System.out);
                result = false;
            }
        }

        return result;
    }
}
