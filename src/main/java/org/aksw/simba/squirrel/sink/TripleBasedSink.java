package org.aksw.simba.squirrel.sink;

import org.aksw.simba.squirrel.data.uri.CrawleableUri;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Component;

/**
 * A sink that can handle triples and models (JENA).
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
@Component
public interface TripleBasedSink extends SinkBase {

    public void addTriple(CrawleableUri uri, Triple triple);
    
    public void addModel(CrawleableUri uri, Model model);

}
