/*
 * $Id$
 *
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

import java.lang.ClassNotFoundException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.io.PrintStream;
import java.io.IOException;
import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
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


public class DistanceAlignment extends BasicAlignment
{
    /** Creation **/
    public DistanceAlignment( OWLOntology onto1, OWLOntology onto2 ){
    	init( onto1, onto2 );
    };

    public void addAlignDistanceCell( Object ob1, Object ob2, String relation, double measure) throws AlignmentException {
	addAlignCell( ob1, ob2, relation, 1-measure );
    }
    public double getAlignedDistance1( Object ob ) throws AlignmentException {
	return (1 - getAlignedStrength1(ob));
    };
    public double getAlignedDistance2( Object ob ) throws AlignmentException{
	return (1 - getAlignedStrength1(ob));
    };

	// This mechanism should be parametric!
	// Select the best match
	// There can be many algorithm for these:
	// n:m: get all of those above a threshold
	// 1:1: get the best discard lines and columns and iterate
	// Here we basically implement ?:* because the algorithm
	// picks up the best matching object above threshold for i.

    protected void selectBestMatch( int nbobj1, Vector list1, int nbobj2, Vector list2, double[][] matrix, double threshold, Object way) throws AlignmentException {
	if (debug > 0) System.err.print("Storing class alignment\n");
	
	for ( int i=0; i<nbobj1; i++ ){
	    boolean found = false;
	    int best = 0;
	    double max = threshold;
	    for ( int j=0; j<nbobj2; j++ ){
		if ( matrix[i][j] < max) {
		    found = true;
		    best = j;
		    max = matrix[i][j];
		}
	    }
	    if ( found ) { addAlignDistanceCell( (OWLEntity)list1.get(i), (OWLEntity)list2.get(best), "=", max ); }
	}
    }

}
