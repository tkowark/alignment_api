/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2008
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
import java.text.NumberFormat;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.onto.LoadedOntology;

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
    public LoadedOntology onto1 = null;
    public LoadedOntology onto2 = null;
    public int nbclass1 = 0; // number of classes in onto1
    public int nbclass2 = 0; // number of classes in onto2
    public int nbprop1 = 0; // number of classes in onto1
    public int nbprop2 = 0; // number of classes in onto2
    public int nbind1 = 0; // number of individuals in onto1
    public int nbind2 = 0; // number of individuals in onto2
    public int i, j = 0;     // index for onto1 and onto2 classes
    public int l1, l2 = 0;   // length of strings (for normalizing)
    public HashMap<Object,Integer> classlist2 = null; // onto2 classes
    public HashMap<Object,Integer> classlist1 = null; // onto1 classes
    public HashMap<Object,Integer> proplist2 = null; // onto2 properties
    public HashMap<Object,Integer> proplist1 = null; // onto1 properties
    public HashMap<Object,Integer> indlist2 = null; // onto2 individuals
    public HashMap<Object,Integer> indlist1 = null; // onto1 individuals

    private NumberFormat numFormat = null; // printing

    public double clmatrix[][];   // distance matrix
    public double prmatrix[][];   // distance matrix
    public double indmatrix[][];   // distance matrix
	
    public void initialize( LoadedOntology onto1, LoadedOntology onto2, Alignment align ){
	initialize( onto1, onto2 );
	// Set the values of the initial alignment in the cells
    }

    public void initialize( LoadedOntology o1, LoadedOntology o2 ){
	onto1 = o1;
	onto2 = o2;
	classlist2 = new HashMap<Object,Integer>(); // onto2 classes
	classlist1 = new HashMap<Object,Integer>(); // onto1 classes
	proplist2 = new HashMap<Object,Integer>(); // onto2 properties
	proplist1 = new HashMap<Object,Integer>(); // onto1 properties
	indlist2 = new HashMap<Object,Integer>(); // onto2 instances
	indlist1 = new HashMap<Object,Integer>(); // onto1 instances

	try {
	    // Create class lists
	    for ( Iterator it = onto2.getClasses().iterator(); it.hasNext(); nbclass2++ ){
		classlist2.put( (Object)it.next(), new Integer(nbclass2) );
	    }
	    for ( Iterator it = onto1.getClasses().iterator(); it.hasNext(); nbclass1++ ){
		classlist1.put( (Object)it.next(), new Integer(nbclass1)  );
	    }
	    clmatrix = new double[nbclass1+1][nbclass2+1];

	    // Create property lists
	    for ( Iterator it = onto2.getObjectProperties().iterator(); it.hasNext(); nbprop2++ ){
		proplist2.put( (Object)it.next(), new Integer(nbprop2) );
	    }
	    for ( Iterator it = onto2.getDataProperties().iterator(); it.hasNext(); nbprop2++ ){
		proplist2.put( (Object)it.next(), new Integer(nbprop2)  );
	    }
	    for ( Iterator it = onto1.getObjectProperties().iterator(); it.hasNext(); nbprop1++ ){
		proplist1.put( (Object)it.next(), new Integer(nbprop1) );
	    }
	    for ( Iterator it = onto1.getDataProperties().iterator(); it.hasNext(); nbprop1++ ){
		proplist1.put( (Object)it.next(), new Integer(nbprop1) );
	    }
	    prmatrix = new double[nbprop1+1][nbprop2+1];
	    // Create individual lists
	    for ( Iterator it = onto2.getIndividuals().iterator(); it.hasNext();  ){
		Object o = (Object)it.next();
		//System.err.println( o );
		// We suppress anonymous individuals... this is not legitimate
		if ( onto2.getEntityURI(o) != null ) {
		    indlist2.put( o, new Integer(nbind2) );
		    nbind2++;
		}
	    }
	    for ( Iterator it = onto1.getIndividuals().iterator(); it.hasNext(); ){
		Object o = (Object)it.next();
		//System.err.println( o );
		// We suppress anonymous individuals... this is not legitimate
		if ( onto2.getEntityURI(o) != null ) {
		    indlist1.put( o, new Integer(nbind1) );
		    nbind1++;
		}
	    }
	    indmatrix = new double[nbind1+1][nbind2+1];

	} catch (AlignmentException e) { e.printStackTrace(); };
    }

    public void compute( Parameters params ){
	try {
	    // Compute distances on classes
	    for ( Iterator it2 = onto2.getClasses().iterator(); it2.hasNext(); ){
		Object cl2 = (Object)it2.next();
		for ( Iterator it1 = onto1.getClasses().iterator(); it1.hasNext(); ){
		    Object cl1 = (Object)it1.next();
		    clmatrix[((Integer)classlist1.get(cl1)).intValue()][((Integer)classlist2.get(cl2)).intValue()] = classMeasure( cl1, cl2 );
		}
	    }
	    // Compute distances on individuals
	    // (this comes first because otherwise, it2 is defined)
	    for ( Iterator it2 = onto2.getIndividuals().iterator(); it2.hasNext(); ){
		Object ind2 = (Object)it2.next();
		if ( indlist2.get(ind2) != null ) {
		    for ( Iterator it1 = onto1.getIndividuals().iterator(); it1.hasNext(); ){
			Object ind1 = (Object)it1.next();
			if ( indlist1.get(ind1) != null ) {
			    indmatrix[((Integer)indlist1.get(ind1)).intValue()][((Integer)indlist2.get(ind2)).intValue()] = individualMeasure( ind1, ind2 );
			}
		    }
		}
	    }
	    // Compute distances on properties
	    ConcatenatedIterator it2 = new
		ConcatenatedIterator(onto2.getObjectProperties().iterator(),
				     onto2.getDataProperties().iterator());
	    for ( ; it2.hasNext(); ){
		Object pr2 = (Object)it2.next();
		ConcatenatedIterator it1 = new
		    ConcatenatedIterator(onto1.getObjectProperties().iterator(),
					 onto1.getDataProperties().iterator());
		for ( ; it1.hasNext(); ){
		    Object pr1 = (Object)it1.next();
		    prmatrix[((Integer)proplist1.get(pr1)).intValue()][((Integer)proplist2.get(pr2)).intValue()] = propertyMeasure( pr1, pr2 );
		}
	    }
	    // What is caught is really Exceptions
	} catch (Exception e) { e.printStackTrace(); }
    }

    public double getIndividualSimilarity( Object i1, Object i2 ){
	return indmatrix[((Integer)indlist1.get(i1)).intValue()][((Integer)indlist2.get(i2)).intValue()];
    }
    public double getClassSimilarity( Object c1, Object c2 ){
	return clmatrix[((Integer)classlist1.get(c1)).intValue()][((Integer)classlist2.get(c2)).intValue()];
    }
    public double getPropertySimilarity( Object p1, Object p2 ){
	return prmatrix[((Integer)proplist1.get(p1)).intValue()][((Integer)proplist2.get(p2)).intValue()];
    }

    // Not an efficient access...
    public void printClassSimilarityMatrix( String type ){
	// Number format class to format the values
	numFormat = NumberFormat.getInstance();
	numFormat.setMinimumFractionDigits( 2 );
	numFormat.setMaximumFractionDigits( 2 );
	System.out.print("\\begin{tabular}{r|");
	for ( int i = 0; i < nbclass1 ; i++ ) System.out.print("c");
	System.out.println("}");
	try {
	    for ( Iterator it1 = onto1.getClasses().iterator(); it1.hasNext(); ){
		Object cl1 = (Object)it1.next();
		System.out.print(" & \\rotatebox{90}{"+onto1.getEntityName( cl1 )+"}");
	    }
	    System.out.println(" \\\\ \\hline");
	    for ( Iterator it2 = onto2.getClasses().iterator(); it2.hasNext(); ){
		Object cl2 = (Object)it2.next();
		System.out.print( onto2.getEntityName( cl2 ) );
		for ( Iterator it1 = onto1.getClasses().iterator(); it1.hasNext(); ){
		    Object cl1 = (Object)it1.next();
		    System.out.print(" & "+numFormat.format(clmatrix[((Integer)classlist1.get(cl1)).intValue()][((Integer)classlist2.get(cl2)).intValue()]));
		}
		System.out.println("\\\\");
	    }
	} catch (AlignmentException e) { e.printStackTrace(); };
	System.out.println("\n\\end{tabular}");
    }

    // Considered not useful so far
    public void printPropertySimilarityMatrix( String type ){};
    public void printIndividualSimilarityMatrix( String type ){};

}
