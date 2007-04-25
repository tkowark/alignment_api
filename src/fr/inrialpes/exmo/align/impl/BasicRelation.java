/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2005, 2007
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

import fr.inrialpes.exmo.align.impl.rel.EquivRelation;
import fr.inrialpes.exmo.align.impl.rel.SubsumeRelation;
import fr.inrialpes.exmo.align.impl.rel.SubsumedRelation;
import fr.inrialpes.exmo.align.impl.rel.IncompatRelation;
import fr.inrialpes.exmo.align.impl.rel.NonTransitiveImplicationRelation;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Relation;

import java.lang.reflect.Constructor;
import java.io.PrintWriter;

import org.xml.sax.ContentHandler;

/**
 * Represents an ontology alignment relation.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class BasicRelation implements Relation
{
    public void accept( AlignmentVisitor visitor) throws AlignmentException {
        visitor.visit( this );
    }
    /**
     * It is intended that the value of the relation is =, < or >.
     * But this can be any string in other applications.
     */
    protected String relation = null;

    /** Creation **/
    public BasicRelation( String rel ){
	relation = rel;
    }

    /** printable format **/
    public String getRelation(){
	return relation;
    }

    public static Relation createRelation( String rel ) {
	Relation relation = null;
	if ( rel.equals("=") ) {
	    relation = new EquivRelation();
	} else if ( rel.equals("<") || rel.equals("&lt;") ) {
	    relation = new SubsumeRelation();
	} else if ( rel.equals(">") || rel.equals("&gt;") ) {
	    relation = new SubsumedRelation();
	} else if ( rel.equals("%") ) {
	    relation = new IncompatRelation();
	} else if ( rel.equals("~>") || rel.equals("~&gt;") ) {
	    relation = new NonTransitiveImplicationRelation();
	} else {
	    try {
		// Create a relation from classname
		Class relationClass = Class.forName(rel);
		Constructor relationConstructor = relationClass.getConstructor((Class[])null);
		relation = (Relation)relationConstructor.newInstance((Object[])null);
	    } catch ( Exception ex ) {
		//ex.printStackTrace();
		//Otherwise, just create a Basic relation
		relation = (Relation)new BasicRelation( rel );
	    }
	};
	return relation;
    }

    /** By default the inverse is the relation itself **/
    public Relation inverse() {
	return this;
    }

    /** Are the two relations equal **/
    public boolean equals( Relation r ) {
	if ( r instanceof BasicRelation ){
	    return ( relation.equals( ((BasicRelation)r).getRelation() ) );
	} else {
	    return false;
	}
    }

    /** Housekeeping **/
    public void dump( ContentHandler h ){};

    /** This is kept for displayig more correctly the result **/
    //public void write( PrintStream writer ) {
    public void write( PrintWriter writer ) {
	writer.print(relation);
    }
}


