/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2005, 2007
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

import java.net.URI;

import org.xml.sax.ContentHandler;

//*/3.0
//import org.semanticweb.owl.model.OWLEntity;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

/**
 * Represents an ontology alignment correspondence. 
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class BasicCell implements Cell, Comparable {
    public void accept( AlignmentVisitor visitor) throws AlignmentException {
        visitor.visit( this );
    }

    String id = null;
    String semantics = null;
    //*/3.0
    //OWLEntity object1 = null;
    //OWLEntity object2 = null;
    Object object1 = null;
    Object object2 = null;
    Relation relation = null;
    double strength = 0;

    /** Creation **/
    //    public BasicCell( Object ob1, Object ob2 ) throws AlignmentException {
    //	new BasicCell( (String)null, ob1, ob2, "=", 0 );
    //    };

//*/3.0
//    public BasicCell( Object ob1, Object ob2, String rel, double m ) throws AlignmentException {
//	throw new AlignmentException("BasicCell: must take two OWLEntity as argument");
//    }
//    public BasicCell( OWLEntity ob1, OWLEntity ob2, String rel, double m ) throws AlignmentException {
//    public BasicCell( String id, Object ob1, Object ob2, String rel, double m ) throws AlignmentException {
//	new BasicCell( id, ob1, ob2, BasicRelation.createRelation(rel), m );
//    };

//*/3.0
//    public BasicCell( OWLEntity ob1, OWLEntity ob2, Relation rel, double m ) throws AlignmentException {
    public BasicCell( String id, Object ob1, Object ob2, Relation rel, double m ) throws AlignmentException {
	setId( id ); 
	object1 = ob1;
	object2 = ob2;
	relation = rel;
	// No exception, just keep 0?
	if ( m >= 0 && m <= 1 ) strength = m;
    };

    // the strength must be compared with regard to abstract types
    public boolean equals( Cell c ) {
	if ( c instanceof BasicCell ){
	    return ( object1 == c.getObject1() && object2 == c.getObject2() && strength == c.getStrength() && (relation.equals( c.getRelation() )) );
	} else {
	    return false;
	}
    }

    /**
     * Used to order the cells in an alignment:
     * -- this > c iff this.getStrength() < c.getStrength() --
     */
    public int compareTo( Object c ){
	//if ( ! (c instanceof Cell) ) return 1;
	if ( ((Cell)c).getStrength() > getStrength() ) return 1;
	if ( getStrength() > ((Cell)c).getStrength() ) return -1;
	return 0;
    }

    public String getId(){ return id; };
    public void setId( String id ){ this.id = id; };
    public String getSemantics(){ 
	if ( semantics != null ) { return semantics; }
	else { return "first-order"; }
    };
    public void setSemantics( String sem ){ semantics = sem; };
    public Object getObject1(){ return object1; };
    public Object getObject2(){ return object2; };
    public URI getObject1AsURI() throws AlignmentException { 
	if ( object1 instanceof URI ) {
	    return (URI)object1; 
	} else {
	    throw new AlignmentException( "Cannot find URI for "+object1 );
	}
    }
    public URI getObject2AsURI() throws AlignmentException { 
	if ( object2 instanceof URI ) {
	    return (URI)object2; 
	} else {
	    throw new AlignmentException( "Cannot find URI for "+object2 );
	}
    }
    public void setObject1( Object ob ) throws AlignmentException {
//*/3.0
//	if ( ob instanceof OWLEntity ) {
//	    object1 = (OWLEntity)ob;
//	} else {
//	    throw new AlignmentException("BasicCell.setObject1: must have an OWLEntity as argument");
//	}
	object1 = ob;
    }
    public void setObject2( Object ob ) throws AlignmentException {
//*/3.0
//	if ( ob instanceof OWLEntity ) {
//	    object2 = (OWLEntity)ob;
//	} else {
//	    throw new AlignmentException("BasicCell.setObject2: must have an OWLEntity as argument");
//	}
	object2 = ob;
    }
    public Relation getRelation(){ return relation; };
    public void setRelation( Relation rel ){ relation = rel; };
    public double getStrength(){ return strength; };
    public void setStrength( double m ){ strength = m; };

    public Cell inverse() throws AlignmentException {
	return (Cell)new BasicCell( (String)null, object2, object1, relation.inverse(), strength );
	// The sae should be done for the measure
    }

    /** Housekeeping **/
    public void dump( ContentHandler h ){};

}

