/*
 * $Id$
 * Copyright (C) INRIA Rhône-Alpes, 2003-2004
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

import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.Cell;

import org.semanticweb.owl.model.OWLException;

import java.io.PrintStream;
import java.io.IOException;

import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Represents an OWL ontology alignment. An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information
 * about those objects.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class BasicRelation implements Relation
{
    /**
     * It is intended that the value of the relation is =, < or >.
     * But this can be any string in other applications.
     */
    private String relation = null;

    /** Creation **/
    public BasicRelation( String rel ){
	relation = rel;
    }

    /** Housekeeping **/
    public void dump( ContentHandler h ){};

    public void write( PrintStream writer ) throws java.io.IOException {
	writer.print(relation);
    }

    public void printAsAxiom( Cell c ) throws OWLException {
	if ( relation.equals("=") ){
	    // Test if the object is a class
	    System.out.println("  <owl:Class rdf:about=\""+c.getObject1().getURI().toString()+"\">");
	    System.out.println("    <owl:sameClassAs rdf:resource=\""+c.getObject2().getURI().toString()+"\"/>");
	    System.out.println("  </owl:Class>");
	} else {
	}

    }

}


