/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2005, 2007-2008
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
import java.util.Enumeration;

import org.xml.sax.ContentHandler;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.Parameters;

/**
 * Represents an ontology alignment correspondence.
 *
 * @author Jérôme Euzenat
 * @version $Id$
 */

public class BasicCell implements Cell, Comparable<Cell> {
    public void accept( AlignmentVisitor visitor) throws AlignmentException {
        visitor.visit( this );
    }

    protected String id = null;
    protected String semantics = null;
    protected Object object1 = null;
    protected Object object2 = null;
    protected Relation relation = null;
    protected double strength = 0;
    protected Parameters extensions = null;

    /** Creation **/
    public BasicCell( String id, Object ob1, Object ob2, Relation rel, double m ) throws AlignmentException {
	setId( id );
	object1 = ob1;
	object2 = ob2;
	relation = rel;
	// No exception, just keep 0?
	if ( m >= 0 && m <= 1 ) strength = m;
	// extensions is only created on demand, otherwise, it is too expensive
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
    public int compareTo( Cell c ){
	//if ( ! (c instanceof Cell) ) return 1;
	if ( c.getStrength() > getStrength() ) return 1;
	if ( getStrength() > c.getStrength() ) return -1;
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
    /**
     * Since version 3.3, the interpretation of objects (and thus finding their
     * URI) depends on the Ontology API which is used. This information is not
     * stored in the Cells (this would cost two pointers per cell) and thus,
     * most of the time, this will raise an exception.
     * Use <tt>Ontology.getEntityURI( this )</tt> instead.
     */
    public URI getObject1AsURI() throws AlignmentException {
	if ( object1 instanceof URI ) {
	    return (URI)object1;
	} else {
	    throw new AlignmentException( "Cannot find URI for "+object1 );
	}
    }
    /**
     * Since version 3.3, the interpretation of objects (and thus finding their
     * URI) depends on the Ontology API which is used. This information is not
     * stored in the Cells (this would cost two pointers per cell) and thus,
     * most of the time, this will raise an exception.
     * Use <tt>Ontology.getEntityURI( this )</tt> instead.
     */
    public URI getObject2AsURI() throws AlignmentException {
	if ( object2 instanceof URI ) {
	    return (URI)object2;
	} else {
	    throw new AlignmentException( "Cannot find URI for "+object2 );
	}
    }
    public void setObject1( Object ob ) throws AlignmentException {
	object1 = ob;
    }
    public void setObject2( Object ob ) throws AlignmentException {
	object2 = ob;
    }
    public Relation getRelation(){ return relation; };
    public void setRelation( Relation rel ){ relation = rel; };
    public double getStrength(){ return strength; };
    public void setStrength( double m ){ strength = m; };

    public Parameters getExtensions(){ return extensions; }
    public void setExtensions( Parameters p ){
	extensions = p;
    }

    public void setExtension( String uri, String label, String value ) {
	if ( extensions == null )
	    extensions = new BasicParameters();
	String [] ext = { uri, label, value };
	extensions.setParameter( uri+label, ext );
    };

    public String getExtension( String uri, String label ) {
	if ( extensions != null ) {
	    return ((String [])extensions.getParameter( uri+label ))[2];
	} else {
	    return (String)null;
	}
    };

    public Cell inverse() throws AlignmentException {
	Cell result = (Cell)new BasicCell( (String)null, object2, object1, relation.inverse(), strength );
	if ( extensions != null ) {
	    for ( Object ext : ((BasicParameters)extensions).getValues() ){
		result.setExtension( ((String[])ext)[0], ((String[])ext)[1], ((String[])ext)[2] );
	    }
	}
	result.getExtensions().unsetParameter( Annotations.ALIGNNS+Annotations.ID );
	// The sae should be done for the measure
	return result;
    }

    public Cell compose(Cell c) throws AlignmentException {
    	if (!object2.equals(c.getObject1()) && relation.compose(c.getRelation())==null )
    		return null;
    	Cell result = (Cell)new BasicCell( (String)null, object1, c.getObject2(), relation.compose(c.getRelation()), strength*c.getStrength() );
    	// TODO : extension...
    	return result;
    }

    /** Housekeeping **/
    public void dump( ContentHandler h ){};

}

