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
import java.lang.reflect.Method;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLFrame;
import org.semanticweb.owl.impl.model.OWLDataCardinalityRestrictionImpl;
import org.semanticweb.owl.impl.model.OWLObjectCardinalityRestrictionImpl;
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


public class ClassStructAlignment extends DistanceAlignment implements AlignmentProcess
{
    /** Creation **/
    public ClassStructAlignment( OWLOntology onto1, OWLOntology onto2 ){
    	super( onto1, onto2 );
    };

    private double max( double i, double j) { if ( i>j ) return i; else return j; }

    /** Processing **/
    public void align( Alignment alignment, Parameters params ) throws AlignmentException, OWLException {
	//ignore alignment;
	double threshold = 0.6; // threshold above which distances are to high
	int i, j = 0;     // index for onto1 and onto2 classes
	int l1, l2 = 0;   // length of strings (for normalizing)
	int nbclass1 = 0; // number of classes in onto1
	int nbclass2 = 0; // number of classes in onto2
	Vector classlist2 = new Vector(10); // onto2 classes
	Vector classlist1 = new Vector(10); // onto1 classes
	double classmatrix[][];   // class distance matrix
	double pic1 = 0.5; // class weigth for name
	double pic2 = 0.5; // class weight for properties

	ingest( alignment );
	// Create class lists
	for ( Iterator it = onto2.getClasses().iterator(); it.hasNext(); nbclass2++ ){
	    classlist2.add( it.next() );
	}
	for ( Iterator it = onto1.getClasses().iterator(); it.hasNext(); nbclass1++ ){
	    classlist1.add( it.next() );
	}
	classmatrix = new double[nbclass1+1][nbclass2+1];
	
	if (debug > 0) System.err.println("Initializing class distances");
	// Initialize class distances
	for ( i=0; i<nbclass1; i++ ){
	    OWLClass cl = (OWLClass)classlist1.get(i);
	    for ( j=0; j<nbclass2; j++ ){
		classmatrix[i][j] = pic1 * StringDistances.subStringDistance(
									     cl.getURI().getFragment().toLowerCase(),
									     ((OWLClass)classlist2.get(j)).getURI().getFragment().toLowerCase());
	    }
	}

	if (debug > 0) System.err.print("Computing class distances\n");
	// Compute classes distances
	// -- for all of its attribute, find the best match if possible... easy
	// -- simply replace in the matrix the value by the value plus the 
	// classmatrix[i][j] =
	// pic1 * classmatrix[i][j]
	// + pic2 * 2 *
	//  (sigma (att in c[i]) getAllignCell... )
	//  / nbatts of c[i] + nbatts of c[j]
	for ( i=0; i<nbclass1; i++ ){
	    Set properties1 = getProperties( (OWLClass)classlist1.get(i), onto1 );
	    int nba1 = properties1.size();
	    if ( nba1 > 0 ) { // if not, keep old values...
		Set correspondences = new HashSet();
		for ( j=0; j<nbclass2; j++ ){
		    Set properties2 = getProperties( (OWLClass)classlist2.get(j), onto2 );
		    int nba2 = properties1.size();
		    double attsum = 0.;
		    // check that there is a correspondance
		    // in list of class2 atts and add their weights
		    for ( Iterator prp = properties1.iterator(); prp.hasNext(); ){
			Cell cell = getAlignCell1( (OWLEntity)prp.next() );
			if ( cell != null ) {
			    if ( properties2.contains((Object)cell.getObject2() ) ) {
				attsum = attsum + 1 - cell.getStrength();
			    }
			}
		    }
		    classmatrix[i][j] = classmatrix[i][j]
			+ pic2 * (2 * attsum / (nba1 + nba2));
		}
	    }
	    // Assess factor
	    // -- FirstExp: nothing to be done: one pass
	}
	selectBestMatch( nbclass1, classlist1, nbclass2, classlist2, classmatrix, threshold, null);

    }

    public void getProperties( OWLDescription desc, OWLOntology o, Set list){
	// I am Jerome Euzenat and I am sure that there is some problem here...
	// DISPATCHING MANUALLY !
	try {
	    Method mm = null;
	if ( Class.forName("org.semanticweb.owl.model.OWLRestriction").isInstance(desc) ){
	    mm = this.getClass().getMethod("getProperties",
					   new Class [] {Class.forName("org.semanticweb.owl.model.OWLRestriction"),Class.forName("org.semanticweb.owl.model.OWLOntology"),Class.forName("java.util.Set")});
	} else if (Class.forName("org.semanticweb.owl.model.OWLClass").isInstance(desc) ) {
	    mm = this.getClass().getMethod("getProperties",
					   new Class [] {Class.forName("org.semanticweb.owl.model.OWLClass"),Class.forName("org.semanticweb.owl.model.OWLOntology"),Class.forName("java.util.Set")});
	} else if (Class.forName("org.semanticweb.owl.model.OWLNaryBooleanDescription").isInstance(desc) ) {
	    mm = this.getClass().getMethod("getProperties",
					   new Class [] {Class.forName("org.semanticweb.owl.model.OWLNaryBooleanDescription"),Class.forName("org.semanticweb.owl.model.OWLOntology"),Class.forName("java.util.Set")});
	}
	if ( mm != null ) mm.invoke(this,new Object[] {desc,o,list});
	    //Method mmm[] = this.getClass().getMethods();
	    //for ( int i = 0; i < mmm.length ; i++ ){
	    //	if ( mmm[i].getName().equals("getProperties") ){
	    //	    mmm[i].invoke(this,new Object[] {desc,o,list});
	    //	    i = mmm.length;
	    //	}
	    // }
	} catch (Exception e) { e.printStackTrace(); };
    }
    public void getProperties( OWLRestriction rest, OWLOntology o, Set list) throws OWLException {
	list.add( (Object)rest.getProperty() );
    }
    public void getProperties( OWLNaryBooleanDescription d, OWLOntology o, Set list) throws OWLException {
	for ( Iterator it = d.getOperands().iterator(); it.hasNext() ;){
	    getProperties( (OWLDescription)it.next(), o, list );
	}
    }
    public void getProperties( OWLClass cl, OWLOntology o, Set list) throws OWLException {
	for ( Iterator it = cl.getSuperClasses(o).iterator(); it.hasNext(); ){
	    OWLDescription dsc = (OWLDescription)it.next();
	    getProperties( dsc, o, list );
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

}
