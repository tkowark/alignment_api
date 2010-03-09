/*
 * $Id$
 *
 * Sourceforge version 1.3 - 2008
 * Copyright (C) INRIA, 2007-2010
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

package fr.inrialpes.exmo.align.impl.edoal;

// JE2009: This is a total mess that must be rewritten wrt Direction

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Relation;

import java.lang.reflect.Constructor;
import java.io.PrintWriter;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.ContentHandler;

/**
 * Represents an ontology alignment relation.
 * In fact, for the EDOAL Mapping language, this encodes directions
 * but should be rewritten in order to achive a better implementation
 *
 * I feel this is not used anymore
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class EDOALRelation implements Relation {

    /**
     * <p>
     * Enumeration to distinuish the direction of the mapping.
     * </p>
     * <p>
     * $Id$
     * </p>
     * 
     * @author richi
     * @version $Revision: 1.10 $
     */
    // JE2009: THIS SHOULD BE REWRITTEN WITH SUBCLASSES AND NO DIRECTIONS...
    /* [JE:22/01/2008]
     * I make this compliant with 2.2.10 and replacing RDFRuleTYpe
     * The Trick for having a hash-table for recording the various relations makes the nameIndex non-static and the getRelation() unusable in static context...
     * Hence it has to be used as: Direction.GENERIC.getRelation();
     */
    public static enum Direction {
	GENERIC("Generic",(String)null),
	    EQUIVALENCE("Equivalence","="), 
	    DISJOINTFROM("DisjointFrom","><"), 
	    SUBSUMES("Subsumes",">"),
	    SUBSUMEDBY("SubsumedBy","<"),
	    INSTANCEOF("InstanceOf",(String)null),
	    HASINSTANCE("HasInstance",(String)null);
    
	private final String EDOALRepr;
	private final String abbrev;
	private final URI uri;
	
	private static Map<String, Direction> nameIndex = null;
	
	private Direction(final String repr, final String abb) {
	    EDOALRepr = repr;
	    abbrev = abb;
	    uri = (URI)null;
	}
	
	public String toString() {
	    return EDOALRepr;
	}

	public static Direction getRelation( final String name ){
	    if ( nameIndex == null ){
		nameIndex = new HashMap<String, Direction>();
		nameIndex.put( "Equivalence", EQUIVALENCE );
		nameIndex.put( "=", EQUIVALENCE );
		nameIndex.put( "equivalence", EQUIVALENCE );
		nameIndex.put( "ClassMapping", EQUIVALENCE );
		nameIndex.put( "Subsumes", SUBSUMES );
		nameIndex.put( ">", SUBSUMES );
		nameIndex.put( "SubsumedBy", SUBSUMEDBY );
		nameIndex.put( "<", SUBSUMEDBY );
		nameIndex.put("><",DISJOINTFROM);
		nameIndex.put("DisjointFrom",DISJOINTFROM);
		nameIndex.put("Disjoint",DISJOINTFROM);
		nameIndex.put("disjointFrom",DISJOINTFROM);
		nameIndex.put("disjoint",DISJOINTFROM);
		nameIndex.put( "InstanceOf", INSTANCEOF );
		nameIndex.put( "HasInstance", HASINSTANCE );
	    }
	    if (name == null) 
		throw new NullPointerException("The string to search must not be null");
	    return nameIndex.get(name);
	}
    }

    public void accept( AlignmentVisitor visitor) throws AlignmentException {
        visitor.visit( this );
    }
    /**
     * It is intended that the value of the relation is =, < or >.
     * But this can be any string in other applications.
     */
    protected String type = null;

    protected Direction direction = null;

    /** Creation **/
    public EDOALRelation( String t ) throws AlignmentException {
	direction = Direction.getRelation( t );
	if ( direction == null ) throw new AlignmentException( "Unknown EDOALRelation : "+t );
	type = t;
    }

    /** Creation **/
    public EDOALRelation( Direction d ) {
	type = d.toString();
	direction = d;
    }

    /** Creation: OLD Stuff should disappear **/
    public EDOALRelation( String t, Direction d ) {
	direction = d;
	type = t;
    }

    /** printable format **/
    public String getRelation(){
	return type;
    }

    /** printable format **/
    public Direction getDirection(){
	return direction;
    }

    public Relation compose(Relation r) {
	String newType = null;
	Direction newDirection = null;
	if ( ! (r instanceof EDOALRelation) ) return null;
	String rType = ((EDOALRelation)r).getRelation();
	// Compose types
	if ( type.startsWith("Class") ) { newType = "Class"; }
	else if ( type.startsWith("Relation") ) { newType = "Relation"; }
	else if ( type.startsWith("Attribute") ) { newType = "Attribute"; }
	else if ( type.startsWith("Instance") ) { newType = "Instance"; }
	if ( ( type.endsWith("Class") && !rType.startsWith("Class") ) ||
	     ( type.endsWith("Relation") && !rType.startsWith("Relation") ) ||
	     ( type.endsWith("Attribute") && !rType.startsWith("Attribute") ) ||
	     ( type.endsWith("Instance") && !rType.startsWith("Instance") ) ){
	    return null;
	}
	if ( rType.endsWith("Class") ) { newType += "Class"; }
	else if ( rType.endsWith("Relation") ) { newType += "Relation"; }
	else if ( rType.endsWith("Attribute") ) { newType += "Attribute"; }
	else if ( rType.endsWith("Instance") ) { newType += "Instance"; }
	// Compose directions
	Direction rDir = ((EDOALRelation)r).getDirection();
	if ( direction == Direction.GENERIC || rDir == Direction.GENERIC ||
	     direction == null || rDir == null ) return null;
	if ( direction == Direction.EQUIVALENCE ) { newDirection = rDir; }
	else if ( direction == Direction.SUBSUMES ) {
	    if ( rDir == Direction.SUBSUMES || rDir == Direction.EQUIVALENCE ) {
		newDirection = Direction.SUBSUMES;
	    } else if ( rDir == Direction.HASINSTANCE ) newDirection = Direction.HASINSTANCE;
	} else if ( direction == Direction.SUBSUMEDBY ) {
	    if ( rDir == Direction.SUBSUMEDBY || rDir == Direction.EQUIVALENCE )
		newDirection = Direction.SUBSUMEDBY;
	} else if ( direction == Direction.INSTANCEOF ) {
	    if ( rDir == Direction.SUBSUMEDBY || rDir == Direction.EQUIVALENCE )
		newDirection = Direction.INSTANCEOF;
	} else if ( direction == Direction.HASINSTANCE ) {
	    if ( rDir == Direction.EQUIVALENCE ) newDirection = Direction.HASINSTANCE;
	}
	if ( newType != null && newDirection != null ) {
	    return new EDOALRelation( newType, newDirection );
	} else return null;
    }

    /** By default the inverse is the relation itself **/
    public Relation inverse() {
	if ( type.equals("Class") ) return new EDOALRelation("Class", direction);
	else if (type.equals("ClassRelation") ) return new EDOALRelation("RelationClass", direction);
	else if (type.equals("ClassAttribute") ) return new EDOALRelation("AttributeClass", direction);
	else if (type.equals("ClassInstance") ) return new EDOALRelation("InstanceClass", direction);
	else if (type.equals("RelationClass") ) return new EDOALRelation("ClassRelation", direction);
	else if (type.equals("Relation") ) return new EDOALRelation("Relation", direction);
	else if (type.equals("RelationAttribute") ) return new EDOALRelation("AttributeRelation", direction);
	else if (type.equals("RelationInstance") ) return new EDOALRelation("InstanceRelation", direction);
	else if (type.equals("AttributeClass") ) return new EDOALRelation("ClassAttribute", direction);
	else if (type.equals("AttributeRelation") ) return new EDOALRelation("RelationAttribute", direction);
	else if (type.equals("Attribute") ) return new EDOALRelation("Attribute", direction);
	else if (type.equals("AttributeInstance") ) return new EDOALRelation("InstanceAttribute", direction);
	else if (type.equals("InstanceClass") ) return new EDOALRelation("ClassInstance", direction);
	else if (type.equals("InstanceRelation") ) return new EDOALRelation("RelationInstance", direction);
	else if (type.equals("InstanceAttribute") ) return new EDOALRelation("AttributeInstance", direction);
	else if (type.equals("Instance") ) return new EDOALRelation("Instance", direction);
	else return null;
    }

    /** Are the two relations equal **/
    public boolean equals( Relation r ) {
	if ( r instanceof EDOALRelation ){
	    return ( type.equals( ((EDOALRelation)r).getRelation() )
		     && direction.equals( ((EDOALRelation)r).getDirection() ) );
	} else {
	    return false;
	}
    }

    public int hashCode() {
	return 5 + 3*type.hashCode() + 7*direction.hashCode() ;
    }

    /** Housekeeping **/
    public void dump( ContentHandler h ){};

    /** This is kept for displayig more correctly the result **/
    public void write( PrintWriter writer ) {
	writer.print(direction.toString());
    }
}


