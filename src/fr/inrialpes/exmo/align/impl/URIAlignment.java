/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2007
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

/**
 * Represents an ontology alignment relating entities identified by their URIs
 *
 * @author Jérôme Euzenat
 * @version $Id$
 */

public class URIAlignment extends BasicAlignment {

    //protected URI onto1 = null;

    //protected URI onto2 = null;

    public URIAlignment() {
    }

    public void init(URI onto1, URI onto2) throws AlignmentException {
	if ( onto1 instanceof URI && onto2 instanceof URI ){
	    super.init( onto1, onto2 );
	} else {
	    throw new AlignmentException("arguments must be URIs");
	};
    }

    public void setOntology1(Object ontology) throws AlignmentException {
	if ( ontology instanceof URI ){
	    super.setOntology1( ontology );
	} else {
	    throw new AlignmentException("arguments must be URIs");
	};
    };

    public void setOntology2(Object ontology) throws AlignmentException {
	if ( ontology instanceof URI ){
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
    public Set getAlignCells1(Object ob) throws AlignmentException {
	if ( ob instanceof URI ){
	    return (HashSet)hash1.get( (URI)ob );
	//	    return super.getAlignCells1( ob );
	} else {
	    throw new AlignmentException("arguments must be URIs");
	}
    }
    public Set getAlignCells2(Object ob) throws AlignmentException {
	if ( ob instanceof URI ){
	    return (HashSet)hash2.get( (URI)ob );
	//	    return super.getAlignCells2( ob );
	} else {
	    throw new AlignmentException("arguments must be URIs");
	}
    }

    // Deprecated: implement as the one retrieving the highest strength correspondence (
    public Cell getAlignCell1(Object ob) throws AlignmentException {
	if ( BasicAlignment.STRICT_IMPLEMENTATION == true ){
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
	if ( BasicAlignment.STRICT_IMPLEMENTATION == true ){
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
     * Generate a copy of this alignment object
     */
    // JE: this is a mere copy of the method in BasicAlignement
    // It has two difficulties
    // - it should call the current init() and not that of BasicAlignement
    // - it should catch the AlignmentException that it is supposed to raise
    public Object clone() {
	URIAlignment align = new URIAlignment();
	try {
	    align.init( (URI)getOntology1(), (URI)getOntology2() );
	} catch ( AlignmentException e ) {};
	align.setType( getType() );
	align.setLevel( getLevel() );
	align.setFile1( getFile1() );
	align.setFile2( getFile2() );
	for ( Enumeration e = extensions.getNames() ; e.hasMoreElements(); ){
	    String label = (String)e.nextElement();
	    align.setExtension( label, getExtension( label ) );
	}
	try {
	    align.ingest( this );
	} catch (AlignmentException ex) { ex.printStackTrace(); }
	return align;
    }

}

