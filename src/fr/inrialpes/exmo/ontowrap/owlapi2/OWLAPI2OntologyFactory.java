/*
 * $Id$
 *
 * Copyright (C) INRIA, 2008-2010
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

package fr.inrialpes.exmo.ontowrap.owlapi2;

import java.net.URI;
import java.net.URISyntaxException;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

import fr.inrialpes.exmo.ontowrap.OntologyCache;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;

public class OWLAPI2OntologyFactory extends OntologyFactory {

    private URI formalismUri = null;

    private String formalismId = "OWL1.1";

    private OWLOntologyManager manager;
    
    private static OntologyCache<OWLAPI2Ontology> cache = null;

    public OWLAPI2OntologyFactory() {
	cache = new OntologyCache<OWLAPI2Ontology>();
	try {
	    formalismUri = new URI("http://www.w3.org/2006/12/owl11-xml#");
	    manager = OWLManager.createOWLOntologyManager();
	} catch (URISyntaxException ex) { // should not happen
	    ex.printStackTrace();
	}
    }

    @Override
    public OWLAPI2Ontology newOntology( Object ontology ) throws AlignmentException {
	if ( ontology instanceof OWLOntology ) {
	    OWLAPI2Ontology onto = new OWLAPI2Ontology();
	    onto.setFormalism( formalismId );
	    onto.setFormURI( formalismUri );
	    onto.setOntology( (OWLOntology)ontology );
	    onto.setURI( ((OWLOntology)ontology).getURI() );
	    // JE: was commented but doubtful
	    cache.recordOntology( onto.getURI(), onto );
	    return onto;
	} else {
	    throw new AlignmentException( "Argument is not an OWLOntology: "+ontology );
	}
    }

    @Override
    public HeavyLoadedOntology loadOntology( URI uri ) throws AlignmentException {
	OWLAPI2Ontology onto = null;
	onto = cache.getOntologyFromURI( uri );
	if ( onto != null ) return onto;
	onto = cache.getOntology( uri );
	if ( onto != null ) return onto;
	OWLOntology ontology;
	try {
	    ontology = manager.loadOntologyFromPhysicalURI(uri);
	} catch (OWLOntologyCreationException e) {
	    e.printStackTrace();
	    throw new AlignmentException("Cannot load " + uri, e);
	}
	onto = new OWLAPI2Ontology();
	onto.setFormalism( formalismId );
	onto.setFormURI( formalismUri );
	onto.setOntology( ontology );
	onto.setFile( uri );
	onto.setURI( ontology.getURI() );
	cache.recordOntology( uri, onto );
	return onto;
    }
    
    public OWLOntologyManager getManager() {
	return manager;
    }

    @Override
    public void clearCache() {
	cache.clear();
    }

}
