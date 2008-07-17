/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2008
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

import java.lang.ClassNotFoundException;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.onto.Ontology;

/**
 * Represents an ontology alignment relating entities identified by their URIs
 *
 * @author Jérôme Euzenat
 * @version $Id$
 */

public class URIAlignment extends BasicAlignment {

    // JE: OMWG1, not sure it works...
    public void init(Object o1, Object o2) throws AlignmentException {
	if ( o1 instanceof Ontology && o2 instanceof Ontology ){
	    super.init( o1, o2 );
	} else if ( o1 instanceof URI && o2 instanceof URI ) {
	    super.init( o1, o2 );
	    this.onto1.setURI( (URI)o1 );
	    this.onto2.setURI( (URI)o2 );
	} else {
	    throw new AlignmentException("arguments must be URIs");
	};
    }

    public void setOntology1(Object ontology) throws AlignmentException {
	if ( ontology instanceof URI || ontology instanceof Ontology ){
	    super.setOntology1( ontology );
	} else {
	    throw new AlignmentException("arguments must be URIs");
	};
    };

    public void setOntology2(Object ontology) throws AlignmentException {
	if ( ontology instanceof URI || ontology instanceof Ontology ){
	    super.setOntology2( ontology );
	} else {
	    throw new AlignmentException("arguments must be URIs");
	};
    };

    /** Cell methods **/
    public Cell addAlignCell(String id, Object ob1, Object ob2, Relation relation, double measure) throws AlignmentException {
        if ( !( ob1 instanceof URI && ob2 instanceof URI ) )
	    throw new AlignmentException("arguments must be URIs");

	return super.addAlignCell( id, ob1, ob2, relation, measure);
    };
    public Cell addAlignCell(Object ob1, Object ob2, String relation, double measure) throws AlignmentException {
 
        if ( !( ob1 instanceof URI && ob2 instanceof URI ) )
	    throw new AlignmentException("arguments must be URIs");

	return super.addAlignCell( ob1, ob2, relation, measure);
    };
    public Cell addAlignCell(Object ob1, Object ob2) throws AlignmentException {
 
        if ( !( ob1 instanceof URI && ob2 instanceof URI ) )
	    throw new AlignmentException("arguments must be URIs");

	return super.addAlignCell( ob1, ob2 );
    };
    public Cell createCell(String id, Object ob1, Object ob2, Relation relation, double measure) throws AlignmentException {
	return (Cell)new URICell( id, (URI)ob1, (URI)ob2, relation, measure );
    }

    // Actually I should search them with equals() but this is what is supposed to be used
    public Set<Cell> getAlignCells1(Object ob) throws AlignmentException {
	if ( ob instanceof URI ){
	    return hash1.get( (URI)ob );
	//	    return super.getAlignCells1( ob );
	} else {
	    throw new AlignmentException("arguments must be URIs");
	}
    }
    public Set<Cell> getAlignCells2(Object ob) throws AlignmentException {
	if ( ob instanceof URI ){
	    return hash2.get( (URI)ob );
	//	    return super.getAlignCells2( ob );
	} else {
	    throw new AlignmentException("arguments must be URIs");
	}
    }

    // Deprecated: implement as the one retrieving the highest strength correspondence (
    public Cell getAlignCell1(Object ob) throws AlignmentException {
	if ( Annotations.STRICT_IMPLEMENTATION == true ){
	    throw new AlignmentException("getAlignCell1: deprecated (use getAlignCells1 instead)");
	} else {
	    if ( ob instanceof URI ){
		return super.getAlignCell1( ob );
	    } else {
		throw new AlignmentException("arguments must be URIs");
	    }
	}
    }

    public Cell getAlignCell2(Object ob) throws AlignmentException {
	if ( Annotations.STRICT_IMPLEMENTATION == true ){
	    throw new AlignmentException("getAlignCell2: deprecated (use getAlignCells2 instead)");
	} else {
	    if ( ob instanceof URI ){
		return super.getAlignCell2( ob );
	    } else {
		throw new AlignmentException("arguments must be URIs");
	    }
	}
    }

    /**
     * Returns default exception for conversion to URIAlignments
     *
     */
    public URIAlignment toURIAlignment() throws AlignmentException {
	return this;
    }

    /**
     * Generate a copy of this alignment object
     */
    // JE: this is a mere copy of the method in BasicAlignement
    public Object clone() {
	URIAlignment align = new URIAlignment();
	try { align.init( (URI)getOntology1(), (URI)getOntology2() ); }
	catch ( AlignmentException e ) {};
	align.setType( getType() );
	align.setLevel( getLevel() );
	align.setFile1( getFile1() );
	align.setFile2( getFile2() );
	for ( Object ext : ((BasicParameters)extensions).getValues() ){
	    align.setExtension( ((String[])ext)[0], ((String[])ext)[1], ((String[])ext)[2] );
	    }
	String oldid = align.getExtension( Annotations.ALIGNNS, "id" );
	if ( oldid != null && !oldid.equals("") ) {
	    align.setExtension( Annotations.ALIGNNS, "derivedFrom", oldid );
	    align.getExtensions().unsetParameter( Annotations.ALIGNNS+"id" );
	}
	align.setExtension( Annotations.ALIGNNS, "method", "http://exmo.inrialpes.fr/align/impl/URIAlignment#clone" );
	try {
	    align.ingest( this );
	} catch (AlignmentException ex) { ex.printStackTrace(); }
	return align;
    }

}

