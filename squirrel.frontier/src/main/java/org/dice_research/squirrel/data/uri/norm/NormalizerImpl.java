package org.dice_research.squirrel.data.uri.norm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;
import org.dice_research.squirrel.data.uri.CrawleableUri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Parts of the code borrowed from <a href=
 * "https://github.com/crawler-commons/crawler-commons/blob/master/src/main/java/crawlercommons/filters/basic/BasicURLNormalizer.java">Crawler
 * Commons</a>. Converts URLs to a normal form:
 * <ul>
 * <li>remove dot segments in path: <code>/./</code> or <code>/../</code></li>
 * <li>remove default ports, e.g. 80 for protocol <code>http://</code></li>
 * <li>normalize <a href=
 * "https://en.wikipedia.org/wiki/Percent-encoding#Percent-encoding_in_a_URI">
 * percent-encoding</a> in URL paths</li>
 * <li>sort query parameters</li>
 * <li>add "/" for empty paths</li>
 * <li>filter parts of the URI</li>
 * </ul>
 */
@Component
@Qualifier("normalizerImpl")
public class NormalizerImpl implements UriNormalizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NormalizerImpl.class);

    /**
     * Nutch 1098 - finds URL encoded parts of the URL
     */
    private final static Pattern UNESCAPE_RULE_PATTERN = Pattern.compile("%([0-9A-Fa-f]{2})");
    /**
     * look-up table for characters which should not be escaped in URL paths
     */
    private final static BitSet UNESCAPED_CHARS = new BitSet(0x7F);

    static {
        /*
         * https://tools.ietf.org/html/rfc3986#section-2.2 For consistency,
         * percent-encoded octets in the ranges of ALPHA (%41-%5A and %61-%7A), DIGIT
         * (%30-%39), hyphen (%2D), period (%2E), underscore (%5F), or tilde (%7E)
         * should not be created by URI producers and, when found in a URI, should be
         * decoded to their corresponding unreserved characters by URI normalizers.
         */
        UNESCAPED_CHARS.set(0x2D, 0x2E);
        UNESCAPED_CHARS.set(0x30, 0x39);
        UNESCAPED_CHARS.set(0x41, 0x5A);
        UNESCAPED_CHARS.set(0x61, 0x7A);
        UNESCAPED_CHARS.set(0x5F);
        UNESCAPED_CHARS.set(0x7E);
    }

    @Override
    public CrawleableUri normalize(CrawleableUri uri) {
        URI uriObject = uri.getUri();
        boolean changed = false;
        // normalize path
//        String path = uriObject.getPath();
//        if (path != null) {
//            String temp = normalizePath(path);
//            if (temp != path1) {
//                path = temp;
//                changed = true;
//            }
//        }

        // Copy Normalization from
        // https://github.com/crawler-commons/crawler-commons/blob/master/src/main/java/crawlercommons/filters/basic/BasicURLNormalizer.java
        // OR use URI.normalize()

        // Check whether the query part of a URI has to be sorted
        String query = uriObject.getQuery();
        if((query != null)&& query.length() > 0) {
            String[] queryList = query.split("&");
            Arrays.sort(queryList);
            query = String.join("&", queryList);
            changed = true;
        }

        //Remove default ports
        int port = uriObject.getPort();
        if(port == 80 || port == 443){
            port = -1;
            changed = true;
        }

        //Add '/' for empty paths
        String path = uriObject.getPath();
        if(path.equals("")){
            URIBuilder builder3 = new URIBuilder(uriObject);
            builder3.setPath("/");
            try {
                uri = new CrawleableUri(builder3.build());
            } catch (URISyntaxException e) {
                LOGGER.error("Exception while normalizing URI. Returning original URI.", e);
            }

        }

        // Filter fragments (i.e., delete them)
        String fragment = uriObject.getFragment();
        if ((fragment != null) && (fragment.length() > 0)) {
            changed = true;
        }

        // Filter attributes of the URI
        // uriObject.getQuery();

        if (changed) {
            // create new URI object;
            URIBuilder builder = new URIBuilder(uriObject);
            builder.setFragment(null);
   //        builder.setPath(path);
            builder.setCustomQuery(query);
            builder.setPort(port);
            try {
                uri = new CrawleableUri(builder.build());
            } catch (URISyntaxException e) {
                LOGGER.error("Exception while normalizing URI. Returning original URI.", e);
            }
        }
        return uri;
    }

    /**
     * Path normalization adapted from the {@link URI} class (which is based upon
     * src/solaris/native/java/io/canonicalize_md.c) and the <a href=
     * "https://github.com/crawler-commons/crawler-commons/blob/master/src/main/java/crawlercommons/filters/basic/BasicURLNormalizer.java">Crawler
     * Commons</a> project.
     * 
     * @param path
     * @return the normalized path or the given path object if no changes have been
     *         made.
     */
    public String normalizePath(String path) {
        // Check for encoded parts
        Matcher matcher = UNESCAPE_RULE_PATTERN.matcher(path);
        StringBuffer changedPath = null;
        if (matcher.find()) {
            changedPath = new StringBuffer(path);
            int hex, pos = 0;
            do {
                changedPath.append(path.substring(pos, matcher.start()));
                pos = matcher.start();
                hex = getHexValue(path.charAt(pos + 1), path.charAt(pos + 2));
                // If this character shouldn't be escaped
                if (UNESCAPED_CHARS.get(hex)) {
                    changedPath.append((char) hex);
                } else {
                    changedPath.append(path.substring(pos, pos + 3));
                }
                pos += 3;
            } while (matcher.find());
            if (pos < path.length()) {
                changedPath.append(path.substring(pos));
            }
        }
        if (changedPath == null) {
            return PathNormalization.normalize(path);
        } else {
            String newPath = changedPath.toString();
            return PathNormalization.normalize(newPath.equals(path) ? path : newPath);
        }
    }

    protected static int getHexValue(char c1, char c2) {
        int hex;
        if (c1 <= 0x39) {
            hex = c1 - 0x30;
        } else {
            // Check whether it is A-F or a-f
            hex = (c1 <= 0x46) ? (c1 - 0x37) : (c1 - 0x57);
        }
        hex <<= 4;
        if (c2 <= 0x39) {
            hex |= c2 - 0x30;
        } else {
            // Check whether it is A-F or a-f
            hex |= (c2 <= 0x46) ? (c2 - 0x37) : (c2 - 0x57);
        }
        return hex;
    }

}
