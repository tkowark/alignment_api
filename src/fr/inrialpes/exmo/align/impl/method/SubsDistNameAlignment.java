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

/** This class has been built for ISWC experiments with bibliography.
 * It implements a non iterative (one step) OLA algorithms based on
 * the name of classes and properties. It could be made iterative by
 *  just adding range/domain on properties...
 *  The parameters are:
 *  - threshold: above what do we select for the alignment;
 *  - epsillon [ignored]: for convergence
 *  - pic1: weigth for class name
 *  - pic2: weight for class attributes
 *  - pia1 [ignored=1]: weigth for property name
 *  - pia3 [ignored=0]: weigth for property domain
 *  - pia4 [ignored=0]: weigth for property range
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */


public class SubsDistNameAlignment extends BasicAlignment implements AlignmentProcess
{
	
    /** Creation **/
    public SubsDistNameAlignment( OWLOntology onto1, OWLOntology onto2 ){
    	init( onto1, onto2 );
    };

    private double max( double i, double j) { if ( i>j ) return i; else return j; }

    /** Processing **/
    public void align( Alignment alignment ) throws AlignmentException, OWLException {
	//ignore alignment;
	double threshold = 0.6; // threshold above which distances are to high
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
	double pic1 = 0.5; // class weigth for name
	double pic2 = 0.5; // class weight for properties
	double pia1 = 0.5; // relation weight for name
	double pia2 = 0.25; // relation weight for domain
	double pia3 = 0.25; // relation weight for range
	double epsillon = 0.05; // stoping condition

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

	try {
	    System.err.println("Initializing property distances");
	    for ( i=0; i<nbprop1-1; i++ ){
		OWLProperty cl = (OWLProperty)proplist1.get(i);
		String s1 = cl.getURI().getFragment().toLowerCase();
		for ( j=0; j<nbprop2-1; j++ ){
		    cl = (OWLProperty)proplist2.get(j);
		    String s2 = cl.getURI().getFragment().toLowerCase();
		    propmatrix[i][j] = pia1 * SubStringDistance.getDistance( s1, s2 );
		}
	    }

	    System.err.println("Initializing class distances");
	    // Initialize class distances
	    for ( i=0; i<nbclass1-1; i++ ){
		OWLClass cl = (OWLClass)classlist1.get(i);
		for ( j=0; j<nbclass2-1; j++ ){
		    classmatrix[i][j] = pic1 * SubStringDistance.getDistance(
						    cl.getURI().getFragment().toLowerCase(),
						    ((OWLClass)classlist2.get(j)).getURI().getFragment().toLowerCase());
		}
	    }

	    // Iterate until completion
	    double factor = 1.0;
	    while ( factor > epsillon ){
		// Compute property distances
		// -- FirstExp: nothing to be done: one pass
		// Here create the best matches for property distance already
		// -- FirstExp: goes directly in the alignment structure
		//    since it will never be refined anymore...
		System.err.print("Storing property alignment\n");
		for ( i=0; i<nbprop1-1; i++ ){
		    boolean found = false;
		    int best = 0;
		    double max = threshold;
		    for ( j=0; j<nbprop2-1; j++ ){
			if ( propmatrix[i][j] < max) {
			    found = true;
			    best = j;
			    max = propmatrix[i][j];
			}
		    }
		    if ( found ) { addAlignCell( (OWLProperty)proplist1.get(i), (OWLProperty)proplist2.get(best), "=", max ); }
		}
		
		System.err.print("Computing class distances\n");
		// Compute classes distances
		// -- for all of its attribute, find the best match if possible... easy
		// -- simply replace in the matrix the value by the value plus the 
		// classmatrix[i][j] =
		// pic1 * classmatrix[i][j]
		// + pic2 * 2 *
		//  (sigma (att in c[i]) getAllignCell... )
		//  / nbatts of c[i] + nbatts of c[j]
		for ( i=0; i<nbclass1-1; i++ ){
		    Set properties1 = getProperties( (OWLClass)classlist1.get(i), onto1 );
		    int nba1 = properties1.size();
		    if ( nba1 > 0 ) { // if not, keep old values...
			Set correspondences = new HashSet();
			for ( j=0; j<nbclass2-1; j++ ){
			    Set properties2 = getProperties( (OWLClass)classlist2.get(j), onto2 );
			    int nba2 = properties1.size();
			    double attsum = 0.;
			    // check that there is a correspondance
			    // in list of class2 atts and add their weights
			    for ( Iterator prp = properties1.iterator(); prp.hasNext(); ){
				Cell cell = getAlignCell1( (OWLEntity)prp.next() );
				if ( cell != null ) {
				    if ( properties2.contains((Object)cell.getObject2() ) ) {
					attsum = attsum + cell.getMeasure();
				    }
				}
			    }
			    classmatrix[i][j] = pic1 * classmatrix[i][j]
				+ pic2 * (2 * attsum / (nba1 + nba2));
			}
		    }
		}
		// Assess factor
		// -- FirstExp: nothing to be done: one pass
		factor = 0.;
	    }

	    // This mechanism should be parametric!
	    // Select the best match
	    // There can be many algorithm for these:
	    // n:m: get all of those above a threshold
	    // 1:1: get the best discard lines and columns and iterate
	    // Here we basically implement ?:* because the algorithm
	    // picks up the best matching object above threshold for i.
	    System.err.print("Storing class alignment\n");

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
		if ( found ) { addAlignCell( (OWLClass)classlist1.get(i), (OWLClass)classlist2.get(best), "=", max ); }
	    }
	} catch (Exception e) { throw new AlignmentException ( "Problem getting URI"); }
    }

    private void getProperties( OWLDescription desc, OWLOntology o, Set list){
    }
    private void getProperties( OWLRestriction rest, OWLOntology o, Set list) throws OWLException {
	list.add( (Object)rest.getProperty() );
    }
    private void getProperties( OWLNaryBooleanDescription d, OWLOntology o, Set list) throws OWLException {
	for ( Iterator it = d.getOperands().iterator(); it.hasNext() ;){
	    getProperties( (OWLDescription)it.next(), o, list );
	}
    }
    private void getProperties( OWLClass cl, OWLOntology o, Set list) throws OWLException {
	for ( Iterator it = cl.getSuperClasses(o).iterator(); it.hasNext(); ){
	    getProperties( (OWLDescription)it.next(), o, list );
	}
	// JE: I suspect that this can be a cause for looping!!
	for ( Iterator it = cl.getEquivalentClasses(o).iterator(); it.hasNext(); ){
	    getProperties( (OWLDescription)it.next(), o, list );
	}
    }

    private Set getProperties( OWLClass cl, OWLOntology o ) throws OWLException {
	Set resultSet = new HashSet(); 
	getProperties( cl, o, resultSet );
	return resultSet;
    }
    private Set getPropertyCorrespondences( OWLClass cl, OWLOntology o ) throws OWLException {
	// Set(OWLFrame) getEquivalentClass|getSuperClasses( OWLOntology o)
	// Set(OWLRestriction) getRestrictions()
	// getProperty()
	Set resultSet = new HashSet(); 
	Set frSet = cl.getSuperClasses( o );
	OWLFrame fr;
	/// JE: certainly here I should implement inheritance...
	for ( Iterator frit = frSet.iterator(); frit.hasNext(); ){
	    fr = (OWLFrame)frit.next();
	    Set rstSet = fr.getRestrictions();
	    OWLRestriction rst;
	    for( Iterator rstit = rstSet.iterator(); rstit.hasNext(); ){
		rst = (OWLRestriction)rstit.next();
		//resultSet.add( (Object)rst.getProperty() );
		resultSet.add( getAlignCell1( (OWLEntity)rst.getProperty() ) );
	    }
	}
	return resultSet;
    }

    private double getCorrespondence( OWLProperty p, Set crsp ){
	// find the correspondence on onto2 in the alignment for the property
	// if it is in the set, then return its strength else return 0.
	return (double)0.;
    }

}

class SubStringDistance {

  //*****************************
  // Compute substring distance
  // = 1 - (2 | length of longest common substring | / |s1|+|s2|)
  //*****************************

    public static double getDistance (String s1, String s2) {
	if (s1 == null || s2 == null) {
	    throw new IllegalArgumentException("Strings must not be null");
	}
		
	int l1 = s1.length(); // length of s
	int l2 = s2.length(); // length of t
		
	if ((l1 == 0) && ( l2 == 0 )) return 0;
	if ((l1 == 0) || ( l2 == 0 )) return 1;

	int max = Math.min( l1, l2 ); // the maximal length of a subs
	int best = 0; // the best subs length so far
	    
	int i = 0; // iterates through s1
	int j = 0; // iterates through s2

	for( i=0; (i < l1) && (l1-i > best); i++ ){
	    j = 0;
	    while( l2-j > best ){
		int k = i;
		for( ; (j < l2 )
			 && (s1.charAt(k) != s2.charAt(j) ); j++) {};
		if ( j != l2 ) {// we have found a starting point
		    for( j++, k++; (j < l2) && (k < l1) && (s1.charAt(k) == s2.charAt(j)); j++, k++);
		    best = Math.max( best, k-i );
		}
	    }
	}
	//	    System.err.print(" = ");
	//  System.err.println((double)2*best / (l1+l2));
	    return (1.0 - ((double)2*best / (l1+l2)));
    }

}
