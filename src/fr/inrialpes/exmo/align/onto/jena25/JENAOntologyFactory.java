package fr.inrialpes.exmo.align.onto.jena25;

import java.net.URI;
import java.util.Iterator;

import org.semanticweb.owl.align.AlignmentException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import fr.inrialpes.exmo.align.onto.LoadedOntology;
import fr.inrialpes.exmo.align.onto.OntologyFactory;

public class JENAOntologyFactory extends OntologyFactory{

    @Override
    public LoadedOntology loadOntology(URI uri) throws AlignmentException {
	try {
        	OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
        	m.read(uri.toString());
        	JENAOntology onto = new JENAOntology();
        	onto.setFile(uri);
        	// to be checked : why several ontologies in a model ???
        	onto.setURI(new URI(((Ontology)m.listOntologies().next()).getURI()));
        	//onto.setURI(new URI(m.listOntologies()getOntology(null).getURI()));
        	onto.setOntology(m);
        	return onto;
        }
	catch (Exception e) {
	    throw new AlignmentException("Cannot load "+uri, e );
	}
    }

}
