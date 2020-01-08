package org.dice_research.squirrel.data.uri.norm;

import org.apache.http.client.utils.URIBuilder;
import org.dice_research.squirrel.data.uri.CrawleableUri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


public class DomainBasedUriGenerator implements UriGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(NormalizerImpl.class);

    @Override
    public CrawleableUri getUriVariant(CrawleableUri uri) {
        URI newUri = null;
        URI uriObject = uri.getUri();

        // converting the uri to it's domain
        String domain = uriObject.getHost();
        CrawleableUri uriVariant = null;
        try {
            newUri = new URI(domain);
            uriVariant = new CrawleableUri(newUri);
        } catch (URISyntaxException e) {
            LOGGER.error("Exception while generating variant URIs", e);
        }
        return uriVariant;
    }
}
