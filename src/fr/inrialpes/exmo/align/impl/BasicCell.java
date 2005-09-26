/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2005
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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLEntity;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.rel.*;

/**
 * Represents an OWL ontology alignment. An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information
 * about those objects.
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
    OWLEntity object1 = null;
    OWLEntity object2 = null;
    Relation relation = null;
    double strength = 0;

    /** Creation **/
    public BasicCell( Object ob1, Object ob2 ) throws AlignmentException {
	new BasicCell( ob1, ob2, "=", 0 );
    };

    public BasicCell( String id, Object ob1, Object ob2, String rel, double m ) throws AlignmentException {
	new BasicCell( ob1, ob2, rel, m );
	setId( id );
    }

    public BasicCell( Object ob1, Object ob2, String rel, double m ) throws AlignmentException {
	throw new AlignmentException("BasicCell: must take two OWLEntity as argument");
    }
    public BasicCell( OWLEntity ob1, OWLEntity ob2, String rel, double m ) throws AlignmentException {
	object1 = ob1;
	object2 = ob2;
	if ( rel.equals("=") ) {
	    relation = new EquivRelation();
	} else if ( rel.equals("<") ) {
	    relation = new SubsumeRelation();
	} else if ( rel.equals("%") ) {
	    relation = new IncompatRelation();
	} else if ( rel.equals("~>") ) {
	    relation = new NonTransitiveImplicationRelation();
	} else {
	    // I could use the class name for relation, 
	    // this would be more extensible...
	    relation = new BasicRelation("=");
	};
	// No exception, just keep 0?
	if ( m >= 0 && m <= 1 ) strength = m;
    };


    // the strength must be compared with regard to abstract types
    public boolean equals( Cell c ) {
	if ( c instanceof BasicCell ){
	    return ( object1.equals(c.getObject1()) && object2.equals(c.getObject2()) && strength == c.getStrength() && (relation.equals( c.getRelation() )) );
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
    public void setObject1( Object ob ) throws AlignmentException {
	if ( ob instanceof OWLEntity ) {
	    object1 = (OWLEntity)ob;
	} else {
	    throw new AlignmentException("BasicCell.setObject1: must have an OWLEntity as argument");
	}
    }
    public void setObject2( Object ob ) throws AlignmentException {
	if ( ob instanceof OWLEntity ) {
	    object2 = (OWLEntity)ob;
	} else {
	    throw new AlignmentException("BasicCell.setObject2: must have an OWLEntity as argument");
	}
    }
    public Relation getRelation(){ return relation; };
    public void setRelation( Relation rel ){ relation = rel; };
    public double getStrength(){ return strength; };
    public void setStrength( double m ){ strength = m; };

    public void inverse() {
	OWLEntity ob = object1;
	object1 = object2;
	object2 = ob;
	relation = relation.inverse();
	// The sae should be done for the measure
    }

    /** Housekeeping **/
    public void dump( ContentHandler h ){};

}

