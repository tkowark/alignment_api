/*
 * $Id$
 *
 * Copyright (C) INRIA, 2009
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

package fr.inrialpes.exmo.align.util;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.OntologyNetwork;

import fr.inrialpes.exmo.align.impl.BasicOntologyNetwork;

import java.net.URI;
import java.util.Set;
import java.util.Collections;
import java.util.ArrayList;

/**
 * OntologyNetworkWeakener
 *
 * A utility class that transform an ontology network in one with less alignments/
 */
public class OntologyNetworkWeakener {

    /**
     * suppress alignments in the ontology network so that it retain n-connectivity,
     * i.e., any pairs of ontologies connected by less than n alignments
     * are still connected through at most n alignments. 
     * JE: this is an interesting graph theoretic problem and I do not know where
     * to find it.
     */
    public static OntologyNetwork unconnect( OntologyNetwork on, int n ){
	return on;
    }

    /**
     * suppress n% of the correspondences at random in all alignments
     * n is a number between 0. and 1.
     * Returns a brand new BasicOntologyNetwork (with new alignments and cells)
     * the @threshold parameter tells if the corrrespondences are suppressed at random (false) of by suppressing the n% of lower confidence (true)
     */
    public static OntologyNetwork weakenAlignments( OntologyNetwork on, double n, boolean threshold ) throws AlignmentException {
	if ( n < 0. || n > 1. )
	    throw new AlignmentException( "Argument must be between 0 and 1.: "+n );
	OntologyNetwork newon = new BasicOntologyNetwork();
	for ( URI ontouri : on.getOntologies() ){
	    newon.addOntology( ontouri );
	}
	for ( Alignment al : on.getAlignments() ){
	    Alignment newal = (Alignment)al.clone();
	    if ( threshold ) {
		newal.cut( "perc", (100.-(double)n)/100. );
	    } else {
		int size = newal.nbCells();
		// --------------------------------------------------------------------
		// JE: Here is a tricky one.
		// Using collection schuffle randomly reorganises a list
		// Then choosing the fist n% and destroying them in the Set is performed
		// The complexity is O(copy=N)+O(shuffle=N)+n%*O(delete=N)
		// That's not bad... (and also avoid checking if the same nb is drawn)
		// But in practice other solutions may be better, like:
		// Generating randomly n%*N numbers between 0 and N (util.Random)
		// Ordering them 
		// Traversing the initial structure and deleting the choosen ones...
		// This one (deleting when traversing) is tricky in Java.
		// --------------------------------------------------------------------
		ArrayList<Cell> array = new ArrayList<Cell>( size );
		for ( Cell c : newal ) {
		    array.add( c );
		}
		Collections.shuffle( array );
		for ( int i = (int)(n*size); i > 0; i-- ){
		    newal.remCell( array.get( i ) );
		}
	    }
	    newon.addAlignment( newal );
	}
	return newon;
    }

    /**
     * randomly drops n% of all alignments
     * n is a number between 0. and 1.
     * Returns a brand new BasicOntologyNetwork (with the initial alignments)
     */
    public static OntologyNetwork dropAlignments( OntologyNetwork on, double n ) throws AlignmentException {
	System.err.println( " >>>> "+n );
	if ( n < 0. || n > 1. )
	    throw new AlignmentException( "Argument must be between 0 and 1.: "+n );
	OntologyNetwork newon = new BasicOntologyNetwork();
	for ( URI ontouri : on.getOntologies() ){
	    newon.addOntology( ontouri );
	}
	Set<Alignment> alignments = on.getAlignments();
	int size = alignments.size();
	ArrayList<Alignment> array = new ArrayList<Alignment>( size );
	for ( Alignment al : alignments ){
	    array.add( al );
	}
	Collections.shuffle( array );
	for ( int i = size - (int)(n*size); i > 0; i-- ) {
	    newon.addAlignment( array.get( i ) );
	}
	return newon;
    }
}
