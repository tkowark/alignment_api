package fr.inrialpes.exmo.ontowrap.skoslite;

import java.net.URI;
import java.net.URISyntaxException;

import com.hp.hpl.jena.rdf.model.Model;

import fr.inrialpes.exmo.ontowrap.OntologyCache;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class SKOSLiteOntologyFactory extends OntologyFactory {

    private URI formalismUri;
    private final static String formalismId = "SKOS1.0";
    private final static OntologyCache<SKOSLiteThesaurus> cache = new OntologyCache<SKOSLiteThesaurus>();
    
    public SKOSLiteOntologyFactory() {
	try {
	    formalismUri = new URI("http://www.w3.org/2004/02/skos/core#");
	} catch (URISyntaxException e) {
	    
	    e.printStackTrace();
	}
    }
   
    @Override
    public void clearCache() throws OntowrapException {
	cache.clear();
    }

    @Override
    public SKOSLiteThesaurus loadOntology(URI uri) throws OntowrapException {
	SKOSLiteThesaurus onto = cache.getOntologyFromURI( uri );
	if ( onto != null ) return onto;
	onto = cache.getOntology( uri );
	if ( onto != null ) return onto;
	onto = new SKOSLiteThesaurus(uri);
	onto.setFormalism( formalismId );
	onto.setFormURI( formalismUri );
	
	// TODO find the URI of a skos thesaurus
	//onto.setURI( dataset.getURI() );
	//cache.recordOntology( uri, onto );
	
	return onto;
    }

    @Override
    public SKOSLiteThesaurus newOntology(Object m) throws OntowrapException {
	if ( m instanceof Model ) {
	    SKOSLiteThesaurus onto = new SKOSLiteThesaurus((Model) m);
	    onto.setFormalism( formalismId );
	    onto.setFormURI( formalismUri );
	    //TODO Find the URI of a skos thesaurus ?
	    // This is the URI of the corresponding OWL API Ontology
	    //URI uri = ((SKOSDataset)ontology).getURI();
	    //onto.setURI( uri );
	    //cache.recordOntology( uri, onto );
	    return onto;
	} else {
	    throw new OntowrapException( "Argument is not an Jena Model: "+m );
	}
    }

}
