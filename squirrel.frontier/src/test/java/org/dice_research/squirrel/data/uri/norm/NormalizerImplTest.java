package org.dice_research.squirrel.data.uri.norm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dice_research.squirrel.Constants;
import org.dice_research.squirrel.data.uri.CrawleableUri;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NormalizerImplTest {

    private CrawleableUri originalUri;
    private CrawleableUri expectedUri;

    public NormalizerImplTest(CrawleableUri originalUri, CrawleableUri expectedUri) {
        this.originalUri = originalUri;
        this.expectedUri = expectedUri;
    }

    @Test
    public void test() {
        NormalizerImpl normalizer = new NormalizerImpl();
        CrawleableUri normUri = normalizer.normalize(originalUri);
        Assert.assertEquals(expectedUri, normUri);

        Map<String, Object> expectedData = expectedUri.getData();
        Map<String, Object> normData = normUri.getData();
        for (Entry<String, Object> expected : expectedData.entrySet()) {
            Assert.assertTrue(
                    "The normalized URI does not contain data with the expected key \"" + expected.getKey() + "\".",
                    normData.containsKey(expected.getKey()));
            Assert.assertEquals(expected.getValue(), normData.get(expected.getKey()));
        }
        Assert.assertEquals("The normalized URI seems to have a different amount of appended data elemenets.",
                expectedData.size(), normData.size());


    }


    @Parameters
    public static Collection<Object[]> data() throws URISyntaxException {
        List<Object[]> testConfigs = new ArrayList<Object[]>();
        CrawleableUri originalUri;
        CrawleableUri expectedUri;
        
        // Very simple first test case
        originalUri = new CrawleableUri(new URI("http://example.org/test1"));
        expectedUri = new CrawleableUri(new URI("http://example.org/test1"));
        testConfigs.add(new Object[] { originalUri, expectedUri });

        // Make sure attached data is not lost
        originalUri = new CrawleableUri(new URI("http://example.org/test1"));
        originalUri.addData(Constants.URI_TYPE_KEY, "some type");
        originalUri.addData("My own key", "My own value");
        expectedUri = new CrawleableUri(new URI("http://example.org/test1"));
        expectedUri.addData(Constants.URI_TYPE_KEY, "some type");
        expectedUri.addData("My own key", "My own value");
        testConfigs.add(new Object[] { originalUri, expectedUri });

        // Add a fragment and make sure that it is deleted
        originalUri = new CrawleableUri(new URI("http://example.org/dump.gz#Resource1"));
        expectedUri = new CrawleableUri(new URI("http://example.org/dump.gz"));
        testConfigs.add(new Object[] { originalUri, expectedUri });

        // Add query and make sure the parameters are sorted
        originalUri = new CrawleableUri(new URI("http://www.example.com/?b=1&a=2&a=1"));
        expectedUri = new CrawleableUri(new URI("http://www.example.com/?a=1&a=2&b=1"));
        testConfigs.add(new Object[] { originalUri, expectedUri});

        //Add port and make sure the default ports are removed
        originalUri = new CrawleableUri(new URI("http://www.example.com:80/"));
        expectedUri = new CrawleableUri(new URI("http://www.example.com/"));
        testConfigs.add(new Object[] { originalUri, expectedUri});

        //Check for empty paths and add a '/' for path in such cases
        originalUri = new CrawleableUri(new URI("http://www.example.com"));
        expectedUri = new CrawleableUri(new URI("http://www.example.com/"));
        testConfigs.add(new Object[] { originalUri, expectedUri});

        //Check for path resolving
        originalUri = new CrawleableUri(new URI("http://www.example.com/a/./b/../c"));
        expectedUri = new CrawleableUri(new URI("http://www.example.com/a/c"));
        testConfigs.add(new Object[] { originalUri, expectedUri});

        originalUri = new CrawleableUri(new URI("http://www.example.com/some//path"));
        expectedUri = new CrawleableUri(new URI("http://www.example.com/some/path"));
        testConfigs.add(new Object[] { originalUri, expectedUri});

        //Check for percent encoding
        originalUri = new CrawleableUri(new URI("http://www.example.com/%7euser"));
        expectedUri = new CrawleableUri(new URI("http://www.example.com/~user"));
        testConfigs.add(new Object[] { originalUri, expectedUri});

        // Check for empty query
        originalUri = new CrawleableUri(new URI("http://www.example.com/?"));
        expectedUri = new CrawleableUri(new URI("http://www.example.com/"));
        testConfigs.add(new Object[] {originalUri, expectedUri});

        return testConfigs;


    }
}
