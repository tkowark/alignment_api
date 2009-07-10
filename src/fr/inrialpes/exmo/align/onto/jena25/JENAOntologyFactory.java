/*
 * $Id$
 *
 * Copyright (C) INRIA, 2003-2008
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.onto.jena25;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

import org.semanticweb.owl.align.AlignmentException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import fr.inrialpes.exmo.align.onto.LoadedOntology;
import fr.inrialpes.exmo.align.onto.OntologyFactory;
import fr.inrialpes.exmo.align.onto.OntologyCache;

public class JENAOntologyFactory extends OntologyFactory {

    private static URI formalismUri = null;
    private static String formalismId = "OWL1.0";

    private static OntologyCache<JENAOntology> cache = null;

    public JENAOntologyFactory() {
	cache = new OntologyCache<JENAOntology>();
	try { 
	    formalismUri = new URI("http://www.w3.org/2002/07/owl#");
	} catch (URISyntaxException ex) { ex.printStackTrace(); } // should not happen
    }

    public JENAOntology newOntology( Object ontology ) throws AlignmentException {
	if ( ontology instanceof OntModel ) {
	    JENAOntology onto = new JENAOntology();
	    onto.setFormalism( formalismId );
	    onto.setFormURI( formalismUri );
	    onto.setOntology( (OntModel)ontology );
	    //onto.setFile( uri );// unknown
	    // to be checked : why several ontologies in a model ???
	    // If no URI can be extracted from ontology, then we use the physical URI
	    try {
		try {
		    onto.setURI(new URI(((Ontology)((OntModel)ontology).listOntologies().next()).getURI()));
		} catch (NoSuchElementException nse) {
		    // JE: not verysafe
		    onto.setURI(new URI(((OntModel)ontology).getNsPrefixURI("")));
		}
	    } catch ( URISyntaxException usex ){
		// Better put in the AlignmentException of loaded
		throw new AlignmentException( "URI Error ", usex );
	    }
	    cache.recordOntology( onto.getURI(), onto );
	    return onto;
	} else {
	    throw new AlignmentException( "Argument is not an OntModel: "+ontology );
	}
    }

    public JENAOntology loadOntology( URI uri ) throws AlignmentException {
	JENAOntology onto = null;
	onto = cache.getOntologyFromURI( uri );
	if ( onto != null ) return onto;
	onto = cache.getOntology( uri );
	if ( onto != null ) return onto;
	try {
	    OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
	    m.read(uri.toString());
	    onto = new JENAOntology();
	    onto.setFile(uri);
	    // to be checked : why several ontologies in a model ???
	    // If no URI can be extracted from ontology, then we use the physical URI
	    try {
		onto.setURI(new URI(((Ontology)m.listOntologies().next()).getURI()));
	    } catch (NoSuchElementException nse) {
		onto.setURI(new URI(m.getNsPrefixURI("")));
		//onto.setFile(uri);
	    }
	    //onto.setURI(new URI(m.listOntologies()getOntology(null).getURI()));
	    onto.setOntology(m);
	    cache.recordOntology( uri, onto );
	    return onto;
        } catch (Exception e) {
	    throw new AlignmentException("Cannot load "+uri, e );
	}
    }

    @Override
    public void clearCache() {
	cache.clear();
    };

}
