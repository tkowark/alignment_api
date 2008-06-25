/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2007-2008
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package fr.inrialpes.exmo.align.impl; 

import java.io.PrintStream;
import java.io.IOException;
import java.util.Comparator;
import java.lang.ClassNotFoundException;
import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.rel.*;

/**
 * Represents an ontology alignment correspondence between two URIs
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 * @deprecated OWLAPICell as been deprecated to the profit of ObjectCell
 * It remains here for compatibility purposes and is reimplemented in terms
 * of ObjectCell.
 */

@Deprecated
public class OWLAPICell extends ObjectCell {
    public void accept( AlignmentVisitor visitor) throws AlignmentException {
        visitor.visit( this );
    }

    public OWLAPICell( String id, OWLEntity ob1, OWLEntity ob2, Relation rel, double m ) throws AlignmentException {
	super( id, ob1, ob2, rel, m );
    };

    // the strength must be compared with regard to abstract types
    // NOOWL
    public boolean equals( Cell c ) {
	if ( c instanceof OWLAPICell ){
	    return ( object1.equals(c.getObject1()) && object2.equals(c.getObject2()) && strength == c.getStrength() && (relation.equals( c.getRelation() )) );
	} else {
	    return false;
	}
    }

    // Only OWL
    public URI getObject1AsURI( Alignment al ) throws AlignmentException {
	try {
	    return ((OWLEntity)object1).getURI();
	} catch (OWLException e) {
	    throw new AlignmentException( "Cannot convert to URI "+object1, e );
	}
    }

    // Only OWL
    public URI getObject2AsURI( Alignment al ) throws AlignmentException {
	try {
	    return ((OWLEntity)object2).getURI();
	} catch (OWLException e) {
	    throw new AlignmentException( "Cannot convert to URI "+object2, e );
	}
    }

    // Only OWL
    public Cell inverse() throws AlignmentException {
	return (Cell)new OWLAPICell( (String)null, (OWLEntity)object2, (OWLEntity)object1, relation.inverse(), strength );
	// The same should be done for the measure
    }

}

