/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2004
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
import java.lang.ClassNotFoundException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLEntity;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

/**
 * Represents an OWL ontology alignment. An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information
 * about those objects.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class BasicCell implements Cell
{
    public void accept( AlignmentVisitor visitor) throws AlignmentException {
        visitor.visit( this );
    }

    OWLEntity object1 = null;
    OWLEntity object2 = null;
    Relation relation = null;
    double strength = 0;

    /** Creation **/
    public BasicCell( Object ob1, Object ob2 ) throws AlignmentException {
	new BasicCell( ob1, ob2, "=", 0 );
    };

    public BasicCell( Object ob1, Object ob2, String rel, double m ) throws AlignmentException {
	try {
	    if ( !Class.forName("org.semanticweb.owl.model.OWLEntity").isInstance(ob1) ||
		 !Class.forName("org.semanticweb.owl.model.OWLEntity").isInstance(ob2) )
		throw new AlignmentException("BasicCell: must take two OWLEntity as argument");
	} catch (ClassNotFoundException e) { e.printStackTrace(); }
	object1 = (OWLEntity)ob1;
	object2 = (OWLEntity)ob2;
	if ( rel.equals("=") ) {
	    relation = new EquivRelation();
	} else if ( rel.equals("<") ) {
	    relation = new SubsumeRelation();
	} else if ( rel.equals("%") ) {
	    relation = new IncompatRelation();
	} else {
	    // I could use the class name for relation, 
	    // this would be more extensible...
	    relation = new BasicRelation("=");
	};
	// No exception, just keep 0?
	if ( m >= 0 && m <= 1 ) strength = m;
    };

    public Object getObject1(){ return object1; };
    public Object getObject2(){ return object2; };
    public void setObject1( Object ob ) throws AlignmentException {
	try { 
	    if ( !Class.forName("org.semanticweb.owl.model.OWLEntity").isInstance(ob) )
		throw new AlignmentException("BasicCell.setObject1: must have an OWLEntity as argument");
	} catch (ClassNotFoundException e) { e.printStackTrace(); }
	object1 = (OWLEntity)ob;
    }
    public void setObject2( Object ob ) throws AlignmentException {
	try { 
	    if ( !Class.forName("org.semanticweb.owl.model.OWLEntity").isInstance(ob) )
		throw new AlignmentException("BasicCell.setObject2: must have an OWLEntity as argument");
	} catch (ClassNotFoundException e) { e.printStackTrace(); }
	object2 = (OWLEntity)ob;
    };
    public Relation getRelation(){ return relation; };
    public void setRelation( Relation rel ){ relation = rel; };
    public double getStrength(){ return strength; };
    public void setStrength( double m ){ strength = m; };

    /** Housekeeping **/
    public void dump( ContentHandler h ){};

}

