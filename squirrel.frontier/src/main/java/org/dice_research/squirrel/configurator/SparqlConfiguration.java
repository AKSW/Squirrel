package org.dice_research.squirrel.configurator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryHttp;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.DatasetDescription;
import org.dice_research.squirrel.Constants;
import org.dice_research.squirrel.data.uri.CrawleableUri;
import org.dice_research.squirrel.frontier.impl.FrontierQueryGenerator;
import org.dice_research.squirrel.sink.tripleBased.AdvancedTripleBasedSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public  class SparqlConfiguration implements AdvancedTripleBasedSink{

	private static final Logger LOGGER = LoggerFactory.getLogger(SparqlConfiguration.class);
	/**
	 * The Query factory used to query the SPARQL endpoint.
	 */
	protected static QueryExecutionFactory queryExecFactory = null;

	protected UpdateExecutionFactory updateExecFactory = null;
	protected static CrawleableUri metadataGraphUri = null;


	public SparqlConfiguration(QueryExecutionFactory queryExecFactory, UpdateExecutionFactory updateExecFactory) {
		this.queryExecFactory = queryExecFactory;
		this.updateExecFactory = updateExecFactory;
	}

	public static SparqlConfiguration create(String sparqlEndpointUrl) {

		return create(sparqlEndpointUrl, null, null);
	}

	public static SparqlConfiguration create(String sparqlEndpointUrl, String username, String password) {
		QueryExecutionFactory queryExecFactory = null;
		UpdateExecutionFactory updateExecFactory = null;
		if (username != null && password != null) {
			// Create the factory with the credentials
			final Credentials credentials = new UsernamePasswordCredentials(username, password);
			HttpAuthenticator authenticator = new HttpAuthenticator() {
				@Override
				public void invalidate() {
				}

				@Override
				public void apply(AbstractHttpClient client, HttpContext httpContext, URI target) {
					client.setCredentialsProvider(new CredentialsProvider() {
						@Override
						public void clear() {
						}

						@Override
						public Credentials getCredentials(AuthScope scope) {
							return credentials;
						}

						@Override
						public void setCredentials(AuthScope arg0, Credentials arg1) {
							LOGGER.error("I am a read-only credential provider but got a call to set credentials.");
						}
					});
				}
			};
			queryExecFactory = new QueryExecutionFactoryHttp(sparqlEndpointUrl, new DatasetDescription(),
					authenticator);
			updateExecFactory = new UpdateExecutionFactoryHttp(sparqlEndpointUrl, authenticator);
		} else {
			queryExecFactory = new QueryExecutionFactoryHttp(sparqlEndpointUrl);
			updateExecFactory = new UpdateExecutionFactoryHttp(sparqlEndpointUrl);
		}
		return new SparqlConfiguration(queryExecFactory, updateExecFactory);
	}

	public static void main(String args[]) {

		String sparqlEndpointUrl = "http://localhost:8890/sparql";
		SparqlConfiguration.create(sparqlEndpointUrl);
		Query selectQuery = FrontierQueryGenerator.getInstance().getTimeStampQuery();
		System.out.println(selectQuery);

		QueryExecution qe = queryExecFactory.createQueryExecution(selectQuery);
		ResultSet rs = qe.execSelect();
		List<Triple> triplesFound = new ArrayList<>();
		while (rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			RDFNode subject = sol.get("subject");
			RDFNode predicate = sol.get("predicate");
			RDFNode object = sol.get("object");
			triplesFound.add(Triple.create(subject.asNode(), predicate.asNode(), object.asNode()));
			System.out.println(subject+" "+ predicate+" "+object);
		}
		qe.close();
	}



	@Override
	public List<Triple> getTriplesForGraph(CrawleableUri uri) {
		Query selectQuery = null;
		// if (uri.equals(metaDataGraphUri)) {
		selectQuery = FrontierQueryGenerator.getInstance().getSelectQuery();
		// } else {
		// selectQuery = QueryGenerator.getInstance().getSelectQuery(getGraphId(uri));
		// }

		QueryExecution qe = queryExecFactory.createQueryExecution(selectQuery);
		ResultSet rs = qe.execSelect();
		List<Triple> triplesFound = new ArrayList<>();
		while (rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			RDFNode subject = sol.get("subject");
			RDFNode predicate = sol.get("predicate");
			RDFNode object = sol.get("object");
			triplesFound.add(Triple.create(subject.asNode(), predicate.asNode(), object.asNode()));
		}
		qe.close();
		return triplesFound;
	}
	public static String getGraphId(CrawleableUri uri) {
		return Constants.DEFAULT_RESULT_GRAPH_URI_PREFIX + uri.getData(Constants.UUID_KEY).toString();
	}


	@Override
	public void addTriple(CrawleableUri uri, Triple triple) {
		// TODO Auto-generated method stub

	}

	@Override
	public void openSinkForUri(CrawleableUri uri) {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeSinkForUri(CrawleableUri uri) {
		// TODO Auto-generated method stub

	}
}

