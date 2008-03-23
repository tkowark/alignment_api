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

import fr.inrialpes.exmo.align.onto.ConcreteOntology;
import fr.inrialpes.exmo.align.onto.LoadedOntology;
import fr.inrialpes.exmo.align.onto.Entity;

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


public class OWLAPIOntology extends ConcreteOntology<OWLOntology> implements LoadedOntology<OWLOntology> {
    public OWLAPIOntology() {
	setFormalism( "OWL1.0" );
	try {
	    setFormURI( new URI("http://www.w3.org/2002/07/owl#") );
	} catch (Exception e) {}; // does not happen
    };

    public OWLOntology getOntology() { return onto; }

    public void setOntology( OWLOntology o ) { this.onto = o; }

    // Here it shoud be better to report exception
    // JE: Onto this does not work at all, of course...
    public Set<Entity> getEntities() {
	try {
	    return (Set<Entity>)((OWLOntology)onto).getClasses(); // [W:unchecked]
	} catch (OWLException ex) {
	    return null;
	}
    }

    public Set<Entity> getClasses() {
	try {
	    return (Set<Entity>)((OWLOntology)onto).getClasses(); // [W:unchecked]
	} catch (OWLException ex) {
	    return null;
	}
    }

    public Set<Entity> getProperties() {
	try {
	    return (Set<Entity>)((OWLOntology)onto).getClasses(); // [W:unchecked]
	} catch (OWLException ex) {
	    return null;
	}
    }

    public Set<Entity> getIndividuals() {
	try {
	    return (Set<Entity>)((OWLOntology)onto).getIndividuals(); // [W:unchecked]
	} catch (OWLException ex) {
	    return null;
	}
    }

    // The only point is if I should return OWLProperties or names...
    // I guess that I will return names (ns+name) because otherwise I will need a full
    // Abstract ontology interface and this is not the rule...
    public Iterator<Object> getObjectProperties() throws AlignmentException {
	try {
	    // [Warning:unchecked] due to OWL API not serving generic types
	    return ((OWLOntology)onto).getObjectProperties().iterator(); // [W:unchecked]
	} catch (OWLException ex) {
	    throw new AlignmentException( "Cannot get object properties", ex );
	}
    }

    // JE: these are really specific methods I guess
    public Iterator<URI> getObjectPropertyNames() throws AlignmentException {
	try {
	    return new URIIterator(((OWLOntology)onto).getObjectProperties().iterator());
	} catch (OWLException ex) {
	    throw new AlignmentException( "Cannot get object properties", ex );
	}
    }

    public Iterator<URI> getDataPropertyNames() throws AlignmentException {
	try {
	    return new URIIterator(((OWLOntology)onto).getDataProperties().iterator());
	} catch (OWLException ex) {
	    throw new AlignmentException( "Cannot get object properties", ex );
	}
    }

    public Iterator<URI> getClassNames() throws AlignmentException {
	try {
	    return new URIIterator(((OWLOntology)onto).getClasses().iterator());
	} catch (OWLException ex) {
	    throw new AlignmentException( "Cannot get object properties", ex );
	}
    }

    public Iterator<URI> getInstanceNames() throws AlignmentException {
	try {
	    return new URIIterator(((OWLOntology)onto).getIndividuals().iterator());
	} catch (OWLException ex) {
	    throw new AlignmentException( "Cannot get object properties", ex );
	}
    }

    public int nbClasses() throws AlignmentException {
	try {
	    return ((OWLOntology)onto).getClasses().size();
	} catch (OWLException ex) {
	    throw new AlignmentException( "Cannot get class number" );
	}
    }

    public int nbObjectProperties() throws AlignmentException {
	try {
	    return ((OWLOntology)onto).getObjectProperties().size();
	} catch (OWLException ex) {
	    throw new AlignmentException( "Cannot get object property number" );
	}
    }

    public int nbDataProperties() throws AlignmentException {
	try {
	    return ((OWLOntology)onto).getDataProperties().size();
	} catch (OWLException ex) {
	    throw new AlignmentException( "Cannot get data property number" );
	}
    }

    public int nbInstances() throws AlignmentException {
	try {
	    return ((OWLOntology)onto).getIndividuals().size();
	} catch (OWLException ex) {
	    throw new AlignmentException( "Cannot get individual number" );
	}
    }

    public boolean contains(Entity e) {
	if ( e instanceof OWLEntity ){
	    if ( e instanceof OWLClass ) {
		for ( Object o : getClasses() ){
		    if ( o == e ) return true;
		}
	    } else if ( e instanceof OWLDataProperty ) {
		for ( Object o : getProperties() ){
		    if ( o == e ) return true;
		}
	    } else if ( e instanceof OWLIndividual ) {
		for ( Object o : getIndividuals() ){
		    if ( o == e ) return true;
		}
	    }
	};
	return false;
    }

    public void unload() {
	try {
	    ((OWLOntology)onto).getOWLConnection().notifyOntologyDeleted( ((OWLOntology)onto) );
	} catch (OWLException ex) { System.err.println(ex); };
    }

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

}

final class URIIterator implements Iterator<URI> {
    private Iterator it = null;

    public URIIterator ( Iterator i ){
	it = i;
    }
    public boolean hasNext() { return it.hasNext(); }
    public URI next() throws NoSuchElementException {
	try{ 
	    return ((OWLEntity)it.next()).getURI();
	} catch (OWLException ex) {
	    throw new NoSuchElementException(ex.toString()); // cannot encapsulate!
	}
    }
    public void remove() throws UnsupportedOperationException {
	throw new UnsupportedOperationException();
    }
}
