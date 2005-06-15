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

package fr.inrialpes.exmo.align.impl; 

import java.util.Iterator;
import java.util.HashMap;
import java.net.URI;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.align.impl.Similarity;

/**
 * Implements the structure needed for recording class similarity
 * or dissimilarity within a matrix structure.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */


public abstract class MatrixMeasure implements Similarity {

    //Momentaneously public
    public OWLOntology onto1 = null;
    public OWLOntology onto2 = null;
    public int nbclass1 = 0; // number of classes in onto1
    public int nbclass2 = 0; // number of classes in onto2
    public int nbprop1 = 0; // number of classes in onto1
    public int nbprop2 = 0; // number of classes in onto2
    public int i, j = 0;     // index for onto1 and onto2 classes
    public int l1, l2 = 0;   // length of strings (for normalizing)
    public HashMap classlist2 = null; // onto2 classes
    public HashMap classlist1 = null; // onto1 classes
    public HashMap proplist2 = null; // onto2 properties
    public HashMap proplist1 = null; // onto1 properties

    public double clmatrix[][];   // distance matrix
    public double prmatrix[][];   // distance matrix
	
    public void initialize( OWLOntology onto1, OWLOntology onto2, Alignment align ){
	initialize( onto1, onto2 );
	// Set the values of the initial alignment in the cells
    }

    public void initialize( OWLOntology o1, OWLOntology o2 ){
	onto1 = o1;
	onto2 = o2;
	classlist2 = new HashMap(); // onto2 classes
	classlist1 = new HashMap(); // onto1 classes
	proplist2 = new HashMap(); // onto2 properties
	proplist1 = new HashMap(); // onto1 properties

	try {
	    // Create class lists
	    for ( Iterator it = onto2.getClasses().iterator(); it.hasNext(); nbclass2++ ){
		classlist2.put( it.next(), new Integer(nbclass2) );
	    }
	    for ( Iterator it = onto1.getClasses().iterator(); it.hasNext(); nbclass1++ ){
		classlist1.put( it.next(), new Integer(nbclass1)  );
	    }
	    clmatrix = new double[nbclass1+1][nbclass2+1];

	    // Create property lists
	    for ( Iterator it = onto2.getObjectProperties().iterator(); it.hasNext(); nbprop2++ ){
		proplist2.put( it.next(), new Integer(nbprop2) );
	    }
	    for ( Iterator it = onto2.getDataProperties().iterator(); it.hasNext(); nbprop2++ ){
		proplist2.put( it.next(), new Integer(nbprop2)  );
	    }
	    for ( Iterator it = onto1.getObjectProperties().iterator(); it.hasNext(); nbprop1++ ){
		proplist1.put( it.next(), new Integer(nbprop1) );
	    }
	    for ( Iterator it = onto1.getDataProperties().iterator(); it.hasNext(); nbprop1++ ){
		proplist1.put( it.next(), new Integer(nbprop1) );
	    }
	    prmatrix = new double[nbprop1+1][nbprop2+1];
	} catch (OWLException e) { e.printStackTrace(); };
    }

    public double getIndividualSimilarity( OWLIndividual i1, OWLIndividual i2 ){
	// JE: non finished...
        int i,j = 0;
	return 0.;
    }
    public double getClassSimilarity( OWLClass c1, OWLClass c2 ){
	return clmatrix[((Integer)classlist1.get(c1)).intValue()][((Integer)classlist2.get(c2)).intValue()];
    }
    public double getPropertySimilarity( OWLProperty p1, OWLProperty p2 ){
	return prmatrix[((Integer)proplist1.get(p1)).intValue()][((Integer)proplist2.get(p2)).intValue()];
    }
}
