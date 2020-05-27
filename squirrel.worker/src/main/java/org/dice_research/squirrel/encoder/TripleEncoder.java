package org.dice_research.squirrel.encoder;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that can encode triples to UTF8
 * 
 * @author Geraldo de Souza Junior gsjunior@mail.uni-paderborn.de
 *
 */
public class TripleEncoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(TripleEncoder.class);
	private static TripleEncoder tripleEncoder;

	private TripleEncoder() {

	}

	public static final TripleEncoder getInstance() {
		if (tripleEncoder == null)
			tripleEncoder = new TripleEncoder();

		return tripleEncoder;
	}

	public Triple encodeTriple(Triple t) {
		Node s = t.getSubject();
		Node p = t.getPredicate();
		Node o = t.getObject();

		Triple nt = null;

		try {
			s = encodeUri(s);
			o = encodeUri(o);
			nt = new Triple(s, p, o);
		} catch (URISyntaxException | UnsupportedEncodingException e) {
			LOGGER.error("Could not encode triple {}", t, e);
			return t;
		}

		return nt;

	}

	protected Node encodeUri(Node n) throws  URISyntaxException, UnsupportedEncodingException {
		if(!n.isURI())
			return n;
		
		Map<String, String> parameters = getUriParameters(n.getURI());
		
		if(parameters.isEmpty())
			return n;
		
		String baseURI = n.toString().substring(0,n.toString().indexOf("?"));
		
		
		URIBuilder uriBuilder = new URIBuilder(baseURI);
		for(Entry<String, String> param: parameters.entrySet())
			uriBuilder.addParameter(param.getKey(), param.getValue());
		
		return NodeFactory.createURI(uriBuilder.toString());
	}


	private Map<String, String> getUriParameters(String uri) throws UnsupportedEncodingException {
		Map<String, String> mapParameters = new LinkedHashMap<String, String>();
		if(uri.indexOf("?") == -1)
			return mapParameters;
		try {
		String query = uri.substring(uri.indexOf("?") + 1);
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			mapParameters.put(pair.substring(0, idx),
					pair.substring(idx + 1));
		}
		}catch (IndexOutOfBoundsException e) {
			return new LinkedHashMap<String, String>();
		}

		return mapParameters;
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException, URISyntaxException {
		TripleEncoder te = new TripleEncoder();
		System.out.println(te.encodeUri(NodeFactory.createURI("https://ckan.govdata.de/")).toString());
	}

}
