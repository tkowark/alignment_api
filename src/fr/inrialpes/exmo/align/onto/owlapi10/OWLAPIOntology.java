/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2007-2008
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

package fr.inrialpes.exmo.align.onto.owlapi10;

import java.net.URI;
import java.util.Set;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.lang.UnsupportedOperationException;

import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.onto.LoadedOntology;
import fr.inrialpes.exmo.align.onto.BasicOntology;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;

/**
 * Store the information regarding ontologies in a specific structure
 * Acts as an interface with regard to an ontology APY
 */

//private class OWLAPIOntology extends OntologyAdapter<OWLOntology> {
//    private OWLAPIOntology() {super();}
//}


public class OWLAPIOntology extends BasicOntology<OWLOntology> implements LoadedOntology<OWLOntology> {

    public OWLAPIOntology() {
	setFormalism( "OWL1.0" );
	try {
	    setFormURI( new URI("http://www.w3.org/2002/07/owl#") );
	} catch (Exception e) {}; // does not happen
    };

    public OWLOntology getOntology() { return onto; }

    public void setOntology( OWLOntology o ) { this.onto = o; }

    public Object getEntity( URI uri ) throws AlignmentException {
	try {
	    OWLEntity result = ((OWLOntology)onto).getClass( uri );
	    if ( result == null ) result = ((OWLOntology)onto).getDataProperty( uri );
	    if ( result == null ) result = ((OWLOntology)onto).getObjectProperty( uri );
	    if ( result == null ) result = ((OWLOntology)onto).getIndividual( uri );
	    return result;
	} catch (OWLException ex) {
	    throw new AlignmentException( "Cannot dereference URI : "+uri );
	}
    }

    public URI getEntityURI( Object o ) throws AlignmentException {
	try {
	    return ((OWLEntity)o).getURI();
	} catch (OWLException oex) {
	    throw new AlignmentException( "Cannot get URI ", oex );
	}
    }
    public String getEntityName( Object o ) throws AlignmentException {
	return "Dummt";
    };

    public boolean isEntity( Object o ){
	if ( o instanceof OWLEntity ) return true;
	else return false;
    };
    public boolean isClass( Object o ){
	if ( o instanceof OWLClass ) return true;
	else return false;
    };
    public boolean isProperty( Object o ){
	if ( o instanceof OWLProperty ) return true;
	else return false;
    };
    public boolean isDatatypeProperty( Object o ){
	if ( o instanceof OWLDataProperty ) return true;
	else return false;
    };
    public boolean isObjectProperty( Object o ){
	if ( o instanceof OWLObjectProperty ) return true;
	else return false;
    };
    public boolean isIndividual( Object o ){
	if ( o instanceof OWLIndividual ) return true;
	else return false;
    };

    // Here it shoud be better to report exception
    // JE: Onto this does not work at all, of course...!!!!
    public Set<Object> getEntities() {
	try {
	    return ((OWLOntology)onto).getClasses(); // [W:unchecked]
	} catch (OWLException ex) {
	    return null;
	}
    }

    public Set<Object> getClasses() {
	try {
	    return ((OWLOntology)onto).getClasses(); // [W:unchecked]
	} catch (OWLException ex) {
	    return null;
	}
    }

    public Set<Object> getProperties() {
	try {
	    return ((OWLOntology)onto).getClasses(); // [W:unchecked]
	} catch (OWLException ex) {
	    return null;
	}
    }

    // The only point is if I should return OWLProperties or names...
    // I guess that I will return names (ns+name) because otherwise I will need a full
    // Abstract ontology interface and this is not the rule...
    public Set<Object> getObjectProperties() {
	try {
	    // [Warning:unchecked] due to OWL API not serving generic types
	    return ((OWLOntology)onto).getObjectProperties(); // [W:unchecked]
	} catch (OWLException ex) {
	    //throw new AlignmentException( "Cannot get object properties", ex );
	    return null;
	}
    }

    public Set<Object> getDatatypeProperties() {
	try {
	    // [Warning:unchecked] due to OWL API not serving generic types
	    return ((OWLOntology)onto).getDataProperties(); // [W:unchecked]
	} catch (OWLException ex) {
	    //throw new AlignmentException( "Cannot get object properties", ex );
	    return null;
	}
    }

    public Set<Object> getIndividuals() {
	try {
	    return ((OWLOntology)onto).getIndividuals(); // [W:unchecked]
	} catch (OWLException ex) {
	    return null;
	}
    }

    // JE: particularly inefficient
    public int nbClasses() {
	try {
	    return ((OWLOntology)onto).getClasses().size();
	} catch (OWLException oex) {
	    return 0;
	}
    }

    public int nbObjectProperties() {
	try {
	    return ((OWLOntology)onto).getObjectProperties().size();
	} catch (OWLException oex) {
	    return 0;
	}
    }

    public int nbDataProperties() {
	try {
	    return ((OWLOntology)onto).getDataProperties().size();
	} catch (OWLException oex) {
	    return 0;
	}
    }

    public int nbInstances() {
	try {
	    return ((OWLOntology)onto).getIndividuals().size();
	} catch (OWLException oex) {
	    return 0;
	}
    }

    public void unload() {
	try {
	    ((OWLOntology)onto).getOWLConnection().notifyOntologyDeleted( ((OWLOntology)onto) );
	} catch (OWLException ex) { System.err.println(ex); };
    }

}
