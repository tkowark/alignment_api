/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2005
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

package fr.inrialpes.exmo.align.ling; 

import java.util.Iterator;
import java.util.Vector;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;

/**
 * This Class uses JWNLDistances to align two ontologies.
 * @author  Jerome Pierson
 * @version $Id: JWNLAlignment.java,v 1.0 2004/08/04 
 */


public class JWNLAlignmentTest extends DistanceAlignment implements AlignmentProcess
{
    /** Creation **/
    public JWNLAlignmentTest( OWLOntology onto1, OWLOntology onto2 ){
	super( onto1, onto2 );
	setType("**");
    };

    private double max( double i, double j) { if ( i>j ) return i; else return j; }

    /** Processing **/
    public void align( Alignment alignment, Parameters params ) throws AlignmentException, OWLException {
	//ignore alignment;
	double threshold = 1.; // threshold above which distances are too high
	int i, j, k = 0;     // index for onto1 and onto2 classes
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
	double pia1 = 1.; // relation weight for name
	double pia2 = 0.; // relation weight for domain
	double pia3 = 0.; // relation weight for range
	double epsillon = 0.05; // stoping condition
	// JE: changed within the tutorial Old to not-ald
	JWNLDistances Dist = new JWNLDistances();
	//OldJWNLDistances Dist = new OldJWNLDistances();
     Dist.Initialize();
				if ( params.getParameter("debug") != null )
			     debug = ((Integer)params.getParameter("debug")).intValue();
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
	    String st1=new String();
	    String st2=new String();
	    if (cl.getURI().getFragment()!=null){ st1 = cl.getURI().getFragment().toLowerCase();}
	    for ( j=0; j<nbprop2; j++ ){
		cl = (OWLProperty)proplist2.get(j);
		if(cl.getURI().getFragment()!=null){st2 = cl.getURI().getFragment().toLowerCase() ;}
			propmatrix[i][j] = Dist.BasicSynonymDistance(st1,st2);
	    }
	}
		if (debug > 0) System.err.println("Initializing class distances");
	for ( i=0; i<nbclass1; i++ ){
	    OWLClass cl = (OWLClass)classlist1.get(i);
	    for ( j=0; j<nbclass2; j++ ){
		classmatrix[i][j] = Dist.BasicSynonymDistance(
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
		if ( found ) {addAlignDistanceCell( (OWLProperty)proplist1.get(i), (OWLProperty)proplist2.get(best), "=", max ); }
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
	
	
	
	
	
	
			
