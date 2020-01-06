package org.dice_research.squirrel.frontier.impl;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDataset;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.dice_research.squirrel.data.uri.CrawleableUri;
import org.dice_research.squirrel.frontier.recrawling.FrontierQueryGenerator;
import org.dice_research.squirrel.vocab.PROV_O;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RecrawlingTest {
    @Test
    public void RecrawlingTest() throws Exception {
        Dataset dataset = DatasetFactory.create();
        dataset.setDefaultModel(ModelFactory.createDefaultModel());
        QueryExecutionFactory queryExecFactory = new QueryExecutionFactoryDataset(dataset);
        CrawleableUri uri = new CrawleableUri(new URI("http://example.org/dataset"));
        uri.addProperty((PROV_O.endedAtTime), "2020-01-05T21:40:11.173Z");

        CrawleableUri uri1 = new CrawleableUri(new URI("http://example.org/resource"));
        uri1.addProperty((PROV_O.endedAtTime), "2019-12-25T21:40:11.173Z");

        Query query = QueryFactory.create(FrontierQueryGenerator.getOutdatedUrisQuery());
        try {
            QueryExecution execution = queryExecFactory.createQueryExecution(query);
            ResultSet rs = execution.execSelect();
            assertEquals(true, uri1);
        } catch (Exception e) {
        }
    }
}
