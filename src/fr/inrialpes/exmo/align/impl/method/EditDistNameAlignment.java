/*
 * $Id$
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

package fr.inrialpes.exmo.align.impl; 

import java.util.Iterator;
import java.util.Vector;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.AlignmentException;

/**
 * This class aligns ontology with regard to the editing distance between 
 * class names.
 * TODO: It does not normalize the results...
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */


public class EditDistNameAlignment extends BasicAlignment implements AlignmentProcess
{
	
    /** Creation **/
    public EditDistNameAlignment( OWLOntology onto1, OWLOntology onto2 ){
    	init( onto1, onto2 );
    };

    private double max( double i, double j) { if ( i>j ) return i; else return j; }
    /** Processing **/
    /** This is not exactly equal, this uses toLowerCase() */
    public void align( Alignment alignment ) throws AlignmentException, OWLException {
	//ignore alignment;
	double threshold = 0.6; // threshold above which distances are to high
	int nbclass1 = 0; // number of classes in onto1
	int nbclass2 = 0; // number of classes in onto2
	int i, j = 0;     // index for onto1 and onto2 classes
	int l1, l2 = 0;   // length of strings (for normalizing)
	Vector classlist2 = new Vector(10); // onto2 classes
	Vector classlist1 = new Vector(10); // onto1 classes
	double matrix[][];   // distance matrix

	try {
	    // Create class lists
	    for ( Iterator it = onto2.getClasses().iterator(); it.hasNext(); nbclass2++ ){
		classlist2.add( it.next() );
	    }
	    for ( Iterator it = onto1.getClasses().iterator(); it.hasNext(); nbclass1++ ){
		classlist1.add( it.next() );
	    }
	    matrix = new double[nbclass1+1][nbclass2+1];
	    
	    // Compute distances
	    for ( i=0; i<nbclass1; i++ ){
		OWLClass cl = (OWLClass)classlist1.get(i);
		l1 = cl.getURI().getFragment().length();
		for ( j=0; j<nbclass2; j++ ){
		    //System.err.print(cl.getURI().getFragment().toLowerCase());
		    //System.err.print(" - ");
		    //System.err.print(((OWLClass)classlist2.get(j)).getURI().getFragment().toLowerCase());
		    l2 = ((OWLClass)classlist2.get(j)).getURI().getFragment().length();
		    matrix[i][j] = Levenshtein.getDistance(
//StringEditDistance.editDistance(
						    cl.getURI().getFragment().toLowerCase(),
						    ((OWLClass)classlist2.get(j)).getURI().getFragment().toLowerCase()) / max(l1,l2);
		    //System.err.print(" = ");
		    //System.err.println(matrix[i][j]);
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
		    if ( matrix[i][j] < max) {
			found = true;
			best = j;
			max = matrix[i][j];
		    }
		}
		if ( found ) { addAlignCell( (OWLClass)classlist1.get(i), (OWLClass)classlist2.get(best), "=", max ); }
	    }
	} catch (Exception e) { throw new AlignmentException ( "Problem getting URI"); }

    }

}

/* Pointer was provided in Todd Hugues (Lockheed)
   Taken from http://www.merriampark.com/ldjava.htm
   Initial algorithm by Michael Gilleland
   Integrated in Apache Jakarta Commons
   Improved by Chas Emerick
   This algorithm should be taken appart of this file and reset in the
   context of a proper package name with an acceptable license terms.
   Hopefully, Jakarta Commons will provide this.
 */

class Levenshtein {

  //*****************************
  // Compute Levenshtein distance
  //*****************************

    public static int getDistance (String s, String t) {
	if (s == null || t == null) {
	    throw new IllegalArgumentException("Strings must not be null");
	}
		
	/*
	  The difference between this impl. and the previous is that, rather 
	  than creating and retaining a matrix of size s.length()+1 by 
	  t.length()+1,
	  we maintain two single-dimensional arrays of length s.length()+1.
	  The first, d, is the 'current working' distance array that maintains
	  the newest distance cost counts as we iterate through the characters
	  of String s.  Each time we increment the index of String t we are 
	  comparing, d is copied to p, the second int[]. Doing so allows us
	  to retain the previous cost counts as required by the algorithm
	  (taking the minimum of the cost count to the left, up one, and
	  diagonally up and to the left of the current cost count being
	  calculated).
	  (Note that the arrays aren't really copied anymore, just switched...
	  this is clearly much better than cloning an array or doing a
	  System.arraycopy() each time  through the outer loop.)
	  
	  Effectively, the difference between the two implementations is this
	  one does not cause an out of memory condition when calculating the LD
	  over two very large strings.  		
	*/		
		
	int n = s.length(); // length of s
	int m = t.length(); // length of t
		
	if (n == 0) return m;
	else if (m == 0) return n;

	int p[] = new int[n+1]; //'previous' cost array, horizontally
	int d[] = new int[n+1]; // cost array, horizontally
	int _d[]; //placeholder to assist in swapping p and d

	// indexes into strings s and t
	int i; // iterates through s
	int j; // iterates through t

	char t_j; // jth character of t

	int cost; // cost

	for (i = 0; i<=n; i++) p[i] = i;
	
	for (j = 1; j<=m; j++) {
	    t_j = t.charAt(j-1);
	    d[0] = j;
	    
	    for (i=1; i<=n; i++) {
		cost = s.charAt(i-1)==t_j ? 0 : 1;
		// minimum of cell to the left+1, to the top+1,
		// diagonally left and up +cost				
		d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);  
	    }
	    
	    // copy current distance counts to 'previous row' distance counts
	    _d = p;
	    p = d;
	    d = _d;
	} 

	// our last action in the above loop was to switch d and p, so p now 
	// actually has the most recent cost counts
	return p[n];
    }
}
