/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2004
 * Except for the Levenshtein class whose copyright is not claimed to
 * our knowledge.
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

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;

/**
 * This class aligns ontology with regard to the editing distance between 
 * class names.
 * TODO: It does not normalize the results...
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */


public class EditDistNameAlignment extends DistanceAlignment implements AlignmentProcess
{
	
    /** Creation **/
    public EditDistNameAlignment( OWLOntology onto1, OWLOntology onto2 ){
	super( onto1, onto2 );
	setType("**");
    };

    private double max( double i, double j) { if ( i>j ) return i; else return j; }
    /** Processing **/
    /** This is not exactly equal, this uses toLowerCase() */
    public void align( Alignment alignment, Parameters params ) throws AlignmentException, OWLException {
	//ignore alignment;
	double threshold = 1.; // threshold above which distances are to high
	int nbclass1 = 0; // number of classes in onto1
	int nbclass2 = 0; // number of classes in onto2
	int nbprop1 = 0; // number of classes in onto1
	int nbprop2 = 0; // number of classes in onto2
	int i, j = 0;     // index for onto1 and onto2 classes
	int l1, l2 = 0;   // length of strings (for normalizing)
	Vector classlist2 = new Vector(10); // onto2 classes
	Vector classlist1 = new Vector(10); // onto1 classes
	Vector proplist2 = new Vector(10); // onto2 classes
	Vector proplist1 = new Vector(10); // onto1 classes
	double clmatrix[][];   // distance matrix
	double prmatrix[][];   // distance matrix

	// Create class lists
	for ( Iterator it = onto2.getClasses().iterator(); it.hasNext(); nbclass2++ ){
	    classlist2.add( it.next() );
	}
	for ( Iterator it = onto1.getClasses().iterator(); it.hasNext(); nbclass1++ ){
	    classlist1.add( it.next() );
	}
	clmatrix = new double[nbclass1+1][nbclass2+1];
	// Create property lists
	for ( Iterator it = onto2.getObjectProperties().iterator(); it.hasNext(); nbprop2++ ){
	    proplist2.add( it.next() );
	}
	for ( Iterator it = onto2.getDataProperties().iterator(); it.hasNext(); nbprop2++ ){
	    proplist2.add( it.next() );
	}
	for ( Iterator it = onto1.getObjectProperties().iterator(); it.hasNext(); nbprop1++ ){
	    proplist1.add( it.next() );
	}
	for ( Iterator it = onto1.getDataProperties().iterator(); it.hasNext(); nbprop1++ ){
	    proplist1.add( it.next() );
	}
	prmatrix = new double[nbprop1+1][nbprop2+1];

	// Create class lists
	for ( Iterator it = onto2.getClasses().iterator(); it.hasNext(); nbclass2++ ){
	    classlist2.add( it.next() );
	}
	for ( Iterator it = onto1.getClasses().iterator(); it.hasNext(); nbclass1++ ){
	    classlist1.add( it.next() );
	}
	clmatrix = new double[nbclass1+1][nbclass2+1];
	    
	// Compute distances on classes
	for ( i=0; i<nbclass1; i++ ){
	    OWLClass cl = (OWLClass)classlist1.get(i);
	    l1 = cl.getURI().getFragment().length();
	    for ( j=0; j<nbclass2; j++ ){
		l2 = ((OWLClass)classlist2.get(j)).getURI().getFragment().length();
		clmatrix[i][j] = StringDistances.levenshteinDistance(
						    cl.getURI().getFragment().toLowerCase(),
						    ((OWLClass)classlist2.get(j)).getURI().getFragment().toLowerCase()) / max(l1,l2);
	    }
	}
	// Compute distances on properties
	for ( i=0; i<nbprop1; i++ ){
	    OWLProperty pr = (OWLProperty)proplist1.get(i);
	    l1 = pr.getURI().getFragment().length();
	    for ( j=0; j<nbprop2; j++ ){
		l2 = ((OWLProperty)proplist2.get(j)).getURI().getFragment().length();
		prmatrix[i][j] = StringDistances.levenshteinDistance(
						    pr.getURI().getFragment().toLowerCase(),
						    ((OWLProperty)proplist2.get(j)).getURI().getFragment().toLowerCase()) / max(l1,l2);
	    }
	}

	// This mechanism should be parametric!
	// Select the best match
	// There can be many algorithm for these:
	// n:m: get all of those above a threshold
	// 1:1: get the best discard lines and columns and iterate
	// Here we basically implement ?:* because the algorithm
	// picks up the best matching object above threshold for i.
	for ( i=0; i<nbclass1; i++ ){
	    boolean found = false;
	    int best = 0;
	    double max = threshold;
	    for ( j=0; j<nbclass2; j++ ){
		if ( clmatrix[i][j] < max) {
		    found = true;
		    best = j;
		    max = clmatrix[i][j];
		}
	    }
	    if ( found ) { addAlignDistanceCell( (OWLClass)classlist1.get(i), (OWLClass)classlist2.get(best), "=", max ); }
	}
	for ( i=0; i<nbprop1; i++ ){
	    boolean found = false;
	    int best = 0;
	    double max = threshold;
	    for ( j=0; j<nbprop2; j++ ){
		if ( prmatrix[i][j] < max) {
		    found = true;
		    best = j;
		    max = prmatrix[i][j];
		}
	    }
	    if ( found ) { addAlignDistanceCell( (OWLProperty)proplist1.get(i), (OWLProperty)proplist2.get(best), "=", max ); }
	}
    }

}
