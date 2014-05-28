package fr.inrialpes.exmo.align.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.OntologyNetwork;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.inrialpes.exmo.align.impl.BasicOntologyNetwork;

public class LOVAlignmentService extends AlignmentService{
	
	private AServProtocolManager manager;
	public LOVAlignmentService(AServProtocolManager manager) {
		this.manager = manager;
	}
	
	public static JsonObject getJsonObject( String uri ) throws IOException {
		
		URL url = new URL(uri);
		HttpURLConnection request = (HttpURLConnection) url.openConnection();
		request.connect();
		// Convert to a JSON object to print data
		JsonParser jp = new JsonParser(); 
		//convert the input stream to a json element
		JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); 
		JsonObject jsonobject = root.getAsJsonObject();
		return jsonobject;
		}


	public static BasicOntologyNetwork readLOV( String uri ) throws AlignmentException {
		
			JsonObject jsonobject;
			try {
				jsonobject = getJsonObject(uri);
			} catch (IOException e) {
				throw new AlignmentException( "Ontology Network error: unable to create Json Object from ("+uri+")" );
			}

		/*
		 * lov Structure at May 27th, 2014
		 * using api: http://lov.okfn.org/dataset/lov/api/v1/vocabs
		 * "vocabularies": [
		 *     {
		 *      "uri", "namespace", "prefix", 
		 *      "titles": [ {"value", "language", "dataType"} ],
		 *      "descriptions": [ {"value", "language", "dataType"} ],
		 *      	"lastVersionReviewed": {"date", "versionDecimal", "label", "link"}
		 *     }
		 * ]
		 * We are getting the ontologies' uri
		 */

		BasicOntologyNetwork on = null;
		//fetching the ontologies in the network
		JsonElement ontologynetwork = jsonobject.get("vocabularies");
		JsonArray array = ontologynetwork.getAsJsonArray();
		Iterator<JsonElement> itr = array.iterator();
		on = new BasicOntologyNetwork();
		while(itr.hasNext()) {
			JsonElement element = itr.next();
			JsonObject data = element.getAsJsonObject();
			
			// main data
	        String ontouri = data.get("uri").isJsonNull() ? null : data.get("uri").getAsString();
			try {
				on.addOntology(new URI(ontouri) );
			} catch (URISyntaxException e) {
				throw new AlignmentException( "Ontologies must be identified by URIs" );
				}
			/* if other information is needed:
			String namespace = data.get("namespace").isJsonNull() ? null : data.get("namespace").getAsString();
			String prefix = data.get("prefix").isJsonNull() ? null : data.get("prefix").getAsString();
	        
			// titles
	        JsonArray array_titles = data.get("titles").getAsJsonArray();
	        Iterator<JsonElement> itr_titles = array_titles.iterator();
	        while(itr_titles.hasNext()) {
	        	JsonElement titles = itr_titles.next();
	        	JsonObject data_titles = titles.getAsJsonObject();
	        	String title_value = data_titles.get("value").isJsonNull() ? null : data_titles.get("value").getAsString();
	        	String title_language = data_titles.get("language").isJsonNull() ? null : data_titles.get("language").getAsString();
	        	String title_dataType = data_titles.get("dataType").isJsonNull() ? null : data_titles.get("dataType").getAsString();
	        }

	        // descriptions
	        JsonArray array_descriptions = data.get("descriptions").getAsJsonArray();
	        Iterator<JsonElement> itr_descriptions = array_descriptions.iterator();
	        while(itr_descriptions.hasNext()) {
	        	JsonElement descriptions = itr_descriptions.next();
	        	JsonObject data_descriptions = descriptions.getAsJsonObject();
	        	String descriptions_value = data_descriptions.get("value").isJsonNull() ? null : data_descriptions.get("value").getAsString();
	        	String descriptions_language = data_descriptions.get("language").isJsonNull() ? null : data_descriptions.get("language").getAsString();
	        	String descriptions_dataType = data_descriptions.get("dataType").isJsonNull() ? null : data_descriptions.get("dataType").getAsString();
	        }
	 
	        // descriptions
	        if (!data.get("lastVersionReviewed").isJsonNull()) {
	            JsonObject lastVersionReviewed = data.get("lastVersionReviewed").getAsJsonObject();
	            String lastVersionReviewed_date = lastVersionReviewed.get("date").isJsonNull() ? null : lastVersionReviewed.get("date").getAsString();
	            String lastVersionReviewed_versionDecimal = lastVersionReviewed.get("versionDecimal").isJsonNull() ? null : lastVersionReviewed.get("versionDecimal").getAsString();
	            String lastVersionReviewed_label = lastVersionReviewed.get("label").isJsonNull() ? null : lastVersionReviewed.get("label").getAsString();
	            String lastVersionReviewed_link = lastVersionReviewed.get("link").isJsonNull() ? null : lastVersionReviewed.get("link").getAsString();
	        }
	        */
		}
		return on;
		}
		
	  public void lovServer( ) {
		  
		  logger.info("LOVAlignment server running");
		  BasicOntologyNetwork noo1 = new BasicOntologyNetwork();
		  BasicOntologyNetwork noo2 = new BasicOntologyNetwork();
		  String uri = "http://lov.okfn.org/dataset/lov/api/v1/vocabs";
		  Collection<OntologyNetwork> allOntologyNetworks = manager.ontologyNetworks();
		  for ( OntologyNetwork oNetwork : allOntologyNetworks ) {
		    	noo1 = (BasicOntologyNetwork) oNetwork;
		  }
		  
		  try {
			noo2 = readLOV (uri);
			String id = manager.alignmentCache.recordNewNetwork( noo2, true );
			logger.info("Loading network of ontology (lov) {}",uri);
			} catch (AlignmentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  Set<URI> networkOntology1 = noo1.getOntologies();
		  Set<URI> networkOntology2 = noo2.getOntologies();
		  if( !(networkOntology1.containsAll(networkOntology2) && networkOntology2.containsAll(networkOntology1))) {
			  //update differences
			  logger.info("lov Network has changed {}",uri);
		  }
		  
	  }
}
