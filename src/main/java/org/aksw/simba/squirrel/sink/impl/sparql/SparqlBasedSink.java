package org.aksw.simba.squirrel.sink.impl.sparql;

import org.aksw.simba.squirrel.data.uri.CrawleableUri;
import org.aksw.simba.squirrel.metadata.CrawlingActivity;
import org.aksw.simba.squirrel.sink.Sink;
import org.apache.jena.graph.Triple;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;

public class SparqlBasedSink implements Sink {

    public static final String JENA_PORTS_KEY = "JENA_PORT";
    public static final String JENA_CONTAINER_NAME_KEY = "JENA_HOST_NAME";
    private static String strContentDatasetUriUpdate;
    private static String strContentDatasetUriQuery;
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(SparqlBasedSink.class);


    public SparqlBasedSink() throws Exception {
        Map<String, String> env = System.getenv();
        String containerName = null;
        String port = null;
        if (env.containsKey(JENA_CONTAINER_NAME_KEY) || env.containsKey(JENA_PORTS_KEY)) {
            containerName = env.get(JENA_CONTAINER_NAME_KEY);
            port = env.get(JENA_PORTS_KEY);
        } else {
            String msg = "Couldn't get " + JENA_CONTAINER_NAME_KEY + " or " + JENA_PORTS_KEY + " from the environment.";
            throw new Exception(msg);
        }

        String datasetPrefix = "http://" + containerName + ":" + port + "/ContentSet/";
        strContentDatasetUriUpdate = datasetPrefix + "update";
        strContentDatasetUriQuery = datasetPrefix + "query";

    }

    public void addMetadata(final CrawlingActivity crawlingActivity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addTriple(CrawleableUri uri, Triple triple) {
        UpdateRequest request = UpdateFactory.create(QueryGenerator.getInstance().getAddQuery(uri, triple, false));
        UpdateProcessor proc = UpdateExecutionFactory.createRemote(request, strContentDatasetUriUpdate);
        proc.execute();
    }

    @Override
    public void openSinkForUri(CrawleableUri uri) {
    }

    @Override
    public void closeSinkForUri(CrawleableUri uri) {
    }

    @Override
    public void addData(CrawleableUri uri, InputStream stream) {
    }
}
