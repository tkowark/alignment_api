/*
 * $Id$
 *
 * Copyright (C) INRIA, 2009
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

package fr.inrialpes.exmo.align.impl; 

import java.lang.Cloneable;
import java.lang.Iterable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Hashtable;
import java.net.URI;

import fr.inrialpes.exmo.align.onto.Ontology;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.OntologyNetwork;

/**
 * Represents a distributed system of aligned ontologies or network of ontologies.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class BasicOntologyNetwork implements OntologyNetwork {

    private Hashtable<URI,OntologyTriple> ontologies;
    private HashSet<Alignment> alignments;

    public BasicOntologyNetwork(){
	ontologies = new Hashtable<URI,OntologyTriple>();
	alignments = new HashSet<Alignment>();
    }

    public void addOntology( URI onto ){
	if ( ontologies.get( onto ) == null )
	    ontologies.put( onto, new OntologyTriple( onto ) );
    };
    public void remOntology( URI onto ) throws AlignmentException {
	OntologyTriple ot = ontologies.get( onto );
	if ( ot != null ) {
	    for( Alignment al : ot.sourceAlignments ){
		remAlignment( al );
	    }
	    for( Alignment al : ot.targettingAlignments ){
		remAlignment( al );
	    }
	    ontologies.remove( onto ); // Or set to null
	}
    };
    public void addAlignment( Alignment al ) throws AlignmentException {
	URI o1 = al.getOntology1URI();
	addOntology( o1 );
	ontologies.get( o1 ).sourceAlignments.add( al );
	URI o2 = al.getOntology2URI();
	addOntology( o2 );
	ontologies.get( o2 ).targettingAlignments.add( al );
	alignments.add( al );
    }; 
    public void remAlignment( Alignment al ) throws AlignmentException {
	ontologies.get( al.getOntology1URI() ).sourceAlignments.remove( al );
	ontologies.get( al.getOntology2URI() ).targettingAlignments.remove( al );
	alignments.remove( al );
    };
    public Set<Alignment> getAlignments(){
	return alignments;
    };
    public Set<URI> getOntologies(){
	return ontologies.keySet(); // ??
    };
    public Set<Alignment> getTargetingAlignments( URI onto ){
	return ontologies.get( onto ).targettingAlignments;
    };
    public Set<Alignment> getSourceAlignments( URI onto ){
	return ontologies.get( onto ).sourceAlignments;
    };

}

class OntologyTriple {

    public URI onto;
    public HashSet<Alignment> targettingAlignments;
    public HashSet<Alignment> sourceAlignments;

    OntologyTriple( URI o ){
	onto = o;
	targettingAlignments = new HashSet<Alignment>();
	sourceAlignments = new HashSet<Alignment>();
    }
}



