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


package fr.inrialpes.exmo.align.impl.method; 

import java.util.Iterator;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLFrame;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLNaryBooleanDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLEntity;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;

/** This class has been built for ISWC experiments with bibliography.
 * It implements a non iterative (one step) OLA algorithms based on
 * the name of classes and properties. It could be made iterative by
 *  just adding range/domain on properties...
 *  The parameters are:
 *  - threshold: above what do we select for the alignment;
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class SubsDistNameAlignment extends DistanceAlignment implements AlignmentProcess
{

    /** Creation **/
    public SubsDistNameAlignment( OWLOntology onto1, OWLOntology onto2 ){
    	super( onto1, onto2 );
	setType("**");
    };

    private double max( double i, double j) { if ( i>j ) return i; else return j; }

    /** Processing **/
    public void align( Alignment alignment, Parameters params ) throws AlignmentException, OWLException {
	// The first parameter to get should be debug
	//ignore alignment;
	double threshold = 1.; // threshold above which distances are to high
	int i, j = 0;     // index for onto1 and onto2 classes
	int l1, l2 = 0;   // length of strings (for normalizing)
	int nbclass1 = 0; // number of classes in onto1
	int nbclass2 = 0; // number of classes in onto2
	Vector classlist2 = new Vector(10); // onto2 classes
	Vector classlist1 = new Vector(10); // onto1 classes
	double classmatrix[][];   // class distance matrix
	int nbprop1 = 0; // number of properties in onto1
	int nbprop2 = 0; // number of properties in onto2
	Vector proplist2 = new Vector(10); // onto2 properties
	Vector proplist1 = new Vector(10); // onto1 properties
	double propmatrix[][];   // properties distance matrix

	// Create property lists and matrix
	for ( Iterator it = onto1.getObjectProperties().iterator(); it.hasNext(); nbprop1++ ){
	    proplist1.add( it.next() );
	}
	for ( Iterator it = onto1.getDataProperties().iterator(); it.hasNext(); nbprop1++ ){
	    proplist1.add( it.next() );
	}
	for ( Iterator it = onto2.getObjectProperties().iterator(); it.hasNext(); nbprop2++ ){
	    proplist2.add( it.next() );
	}
	for ( Iterator it = onto2.getDataProperties().iterator(); it.hasNext(); nbprop2++ ){
	    proplist2.add( it.next() );
	}
	propmatrix = new double[nbprop1+1][nbprop2+1];

	// Create class lists
	for ( Iterator it = onto2.getClasses().iterator(); it.hasNext(); nbclass2++ ){
	    classlist2.add( it.next() );
	}
	for ( Iterator it = onto1.getClasses().iterator(); it.hasNext(); nbclass1++ ){
	    classlist1.add( it.next() );
	}
	classmatrix = new double[nbclass1+1][nbclass2+1];
	
	if (debug > 0) System.err.println("Initializing property distances");
	for ( i=0; i<nbprop1; i++ ){
	    OWLProperty cl = (OWLProperty)proplist1.get(i);
	    String s1 = cl.getURI().getFragment();
	    if ( s1 != null ) s1 = s1.toLowerCase();
	    for ( j=0; j<nbprop2; j++ ){
		cl = (OWLProperty)proplist2.get(j);
		String s2 = cl.getURI().getFragment();
		if ( s2 != null ) s2 = s2.toLowerCase();
		if ( s1 == null || s2 == null ) { propmatrix[i][j] = 1.; }
		else { propmatrix[i][j] = StringDistances.subStringDistance( s1, s2 ); }
	    }
	}
	
	if (debug > 0) System.err.println("Initializing class distances");
	// Initialize class distances
	for ( i=0; i<nbclass1; i++ ){
	    OWLClass cl = (OWLClass)classlist1.get(i);
	    for ( j=0; j<nbclass2; j++ ){
		classmatrix[i][j] = StringDistances.subStringDistance(
						    cl.getURI().getFragment().toLowerCase(),
						    ((OWLClass)classlist2.get(j)).getURI().getFragment().toLowerCase());
	    }
	}
	
	// This mechanism should be parametric!
	// Select the best match
	// There can be many algorithm for these:
	// n:m: get all of those above a threshold
	// 1:1: get the best discard lines and columns and iterate
	// Here we basically implement ?:* because the algorithm
	// picks up the best matching object above threshold for i.
	if (debug > 0) System.err.print("Storing property alignment\n");
	for ( i=0; i<nbprop1; i++ ){
	    boolean found = false;
		int best = 0;
		double max = threshold;
		for ( j=0; j<nbprop2; j++ ){
		    if ( propmatrix[i][j] < max) {
			found = true;
			best = j;
			max = propmatrix[i][j];
		    }
		}
		if ( found ) { addAlignDistanceCell( (OWLProperty)proplist1.get(i), (OWLProperty)proplist2.get(best), "=", max ); }
	    }
		
	    if (debug > 0) System.err.print("Storing class alignment\n");

	    for ( i=0; i<nbclass1; i++ ){
		boolean found = false;
		int best = 0;
		double max = threshold;
		for ( j=0; j<nbclass2; j++ ){
		    if ( classmatrix[i][j] < max) {
			found = true;
			best = j;
			max = classmatrix[i][j];
		    }
		}
		if ( found ) { addAlignDistanceCell( (OWLClass)classlist1.get(i), (OWLClass)classlist2.get(best), "=", max ); }
	    }
    }

}
