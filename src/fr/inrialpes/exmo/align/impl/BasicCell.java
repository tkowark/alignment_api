/*
 * $Id$
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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.model.OWLException;
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
    OWLEntity object1 = null;
    OWLEntity object2 = null;
    Relation relation = null;
    double measure = 0;

    /** Creation **/
    public BasicCell( OWLEntity ob1, OWLEntity ob2 ){
	new BasicCell( ob1, ob2, "=", 0 );
    };

    public BasicCell( OWLEntity ob1, OWLEntity ob2, String rel, double m ){
	object1 = ob1;
	object2 = ob2;
	relation = new BasicRelation( rel );
	// No exception, just keep 0?
	if ( m >= 0 && m <= 1 ) measure = m;
    };

    public OWLEntity getObject1(){ return object1; };
    public OWLEntity getObject2(){ return object2; };
    public void setObject1( OWLEntity ob ){ object1 = ob; };
    public void setObject2( OWLEntity ob ){ object2 = ob; };
    public Relation getRelation(){ return relation; };
    public void setRelation( Relation rel ){ relation = rel; };
    public double getMeasure(){ return measure; };
    public void setMeasure( double m ){ measure = m; };

    /** Housekeeping **/
    public void dump( ContentHandler h ){};

    public void write( PrintStream writer ) throws java.io.IOException, org.semanticweb.owl.model.OWLException {

	writer.print("    <Cell>\n      <entity1 rdf:resource='");
	writer.print( object1.getURI().toString() );
	writer.print("'/>\n      <entity2 rdf:resource='");
	writer.print( object2.getURI().toString() );
	writer.print("'/>\n      <measure rdf:datatype='http://www.w3.org/2001/XMLSchema#float'>");
	writer.print( measure );
	writer.print("</measure>\n      <relation>");
	relation.write( writer );
	writer.print("</relation>\n    </Cell>\n");
    };

}

