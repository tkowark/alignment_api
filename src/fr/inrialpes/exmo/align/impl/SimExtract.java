/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2004
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

import java.net.URI;
import java.util.Iterator;
import java.util.Vector;
import java.lang.ClassNotFoundException;
import java.lang.InstantiationException;
import java.lang.IllegalAccessException;
import java.lang.reflect.InvocationTargetException;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLIndividual;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.Similarity;

/**
 * Extract a alignment from a distance of similarity Measure
 * //JE: I am pretty sure that most of this class is useless.
 * As a matter of fact, it copies the similarity into a matrix...
 * instead of asking it (and iterating on the objects).
 *
 * @author jpierson
 * 
 */
public class SimExtract extends alignmentExtractor implements PreAlignment {
    private OWLOntology onto1; 
    private OWLOntology onto2;

    private Similarity sim; 

    /**	 properties similarity matrix
     * 
     */
    private double propmatrix[][]; 

    /**	 class similarity matrix
     * 
     */
    private double classmatrix[][]; 

    /** Individual similarity matrix
     * 
     */

    private double individualmatrix[][];
    private int nbclass1 = 0; // number of classes in onto1
    private int nbclass2 = 0; // number of classes in onto2
    private int nbprop1 = 0; // number of properties in onto1
    private int nbprop2 = 0; // number of properties in onto2
    private int nbindividu1 = 0; // number of individual in onto1
    private int nbindividu2 = 0; // number of individual in onto2
    private Vector classlist2 = new Vector(10); // onto2 classes
    private Vector classlist1 = new Vector(10); // onto1 classes
    private Vector proplist2 = new Vector(10); // onto2 properties
    private Vector proplist1 = new Vector(10); // onto1 properties
    private Vector individulist2 = new Vector(10); // onto2 Individuals
    private Vector individulist1 = new Vector(10); // onto1 Individuals
    private double threshold;

    public SimExtract( String simClassName ) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
	//sim = new SimilarityMeasure();
	// Create similarity object
	Object[] mparams = {};
	Class similarityMeasureClass = Class.forName(simClassName);
	java.lang.reflect.Constructor[] simConstructors = similarityMeasureClass.getConstructors();
	sim = (Similarity) simConstructors[0].newInstance(mparams);
    }

    /**
     * Intialize the Alignment extraction process.
     * @param param
     */
    public void align( Parameters param) {
	sim.initialize(param);
	onto1 = (OWLOntology) param.getParameter("ontology1");
	onto2 = (OWLOntology) param.getParameter("ontology2");
	threshold = ((Double) param.getParameter("Threshold")).doubleValue();
	createDistanceMatrix();
    }

    /**
     * Create a Similarity Matrix between the terms of the two ontologies
     */
    public void createDistanceMatrix() {
	// Create similarity matrix
	
	int i, j = 0; // index for onto1 and onto2 classes
	
	try {
	    // Create property lists and matrix
	    for (Iterator it = onto1.getObjectProperties().iterator(); it.hasNext(); nbprop1++) {
		proplist1.add(it.next());}
	    for (Iterator it = onto1.getDataProperties().iterator(); it.hasNext(); nbprop1++) {
		proplist1.add(it.next());}
	    for (Iterator it = onto2.getObjectProperties().iterator(); it.hasNext(); nbprop2++) {
		proplist2.add(it.next());}
	    for (Iterator it = onto2.getDataProperties().iterator(); it.hasNext(); nbprop2++) {
		proplist2.add(it.next());}
	} catch (OWLException e) {	e.printStackTrace();}
		
	propmatrix = new double[nbprop1 + 1][nbprop2 + 1];
	try {
	    // Create class lists
	    for (Iterator it = onto2.getClasses().iterator(); it.hasNext(); nbclass2++) {
		classlist2.add(it.next());}
	    for (Iterator it = onto1.getClasses().iterator(); it.hasNext(); nbclass1++) {
		classlist1.add(it.next());}
	} catch (OWLException e1) {e1.printStackTrace();}
	
	classmatrix = new double[nbclass1 + 1][nbclass2 + 1];
		
	try {
	    // Create individual lists
	    for (Iterator it = onto2.getIndividuals().iterator(); it.hasNext(); nbindividu2++) {
		individulist2.add(it.next());}
	    for (Iterator it = onto1.getIndividuals().iterator(); it.hasNext(); nbindividu1++) {
		individulist1.add(it.next());}
	} catch (OWLException e1) {e1.printStackTrace();}

	individualmatrix = new double[nbindividu1 + 1][nbindividu2 + 1];

	// Computing the similarity measure between the onlogies entities
	try {
	    for (i = 0; i < nbprop1; i++) {
		OWLProperty cl = (OWLProperty) proplist1.get(i);
		URI U1 = cl.getURI();
		for (j = 0; j < nbprop2; j++) {
		    cl = (OWLProperty) proplist2.get(j);
		    URI U2;
		    U2 = cl.getURI();
		    propmatrix[i][j] = sim.getSimilarity(U1, U2);
		}
	    }
	} catch (Exception e2) {e2.printStackTrace();}
	
	try {
	    for (i = 0; i < nbclass1; i++) {
		OWLClass cl = (OWLClass) classlist1.get(i);
		for (j = 0; j < nbclass2; j++) {
		    classmatrix[i][j] = sim.getSimilarity(cl.getURI(),((OWLClass) classlist2.get(j)).getURI());
		}
	    }
	} catch (Exception e2) {e2.printStackTrace();}
		
	try {
	    for (i = 0; i < nbindividu1; i++) {
		OWLIndividual In = (OWLIndividual) individulist1.get(i);
		for (j = 0; j < nbindividu2; j++) {
		    individualmatrix[i][j] = sim.getSimilarity(In.getURI(),((OWLIndividual) individulist2.get(j)).getURI());
		}
	    }
	} catch (Exception e2) {e2.printStackTrace();}
    }

    /**
     * Extract an alignment form the Similarity Matrix.
     * It only keeps the best alignment for each entity of ontology1
     * under the condition that it is not under threshold.
     *
     * The algorithm does not even test that the corresponding object in 
     * ontology2 is not already in an anlignment...
     * 
     * A greedy method for that would be:
     * - test it (easy)
     * - if it is the case, then choose the best of the two and reiterate the computation on the other.
     * - beware of two things: (a) this will require to map the objects to their rank, (b) do change something ONLY if it is stricly better [less optimal but terminates]
     * 
     * @return A basicAligment object
     */
    public Alignment extractAlignment(String type) {
	BasicAlignment Al = new BasicAlignment();
	int i = 0, j = 0;

	Al.init(onto1, onto2);
	Al.setType(type);
		
	for (i = 0; i < nbprop1; i++) {
	    boolean found = false;
	    int best = 0;
	    double max = threshold;
	    for (j = 0; j < nbprop2; j++) {
		if (propmatrix[i][j] > max) {
		    found = true;
		    best = j;
		    max = propmatrix[i][j];
		}
	    }
	    try {
		if (found && max > threshold) {
		    Al.addAlignCell((OWLProperty) proplist1.get(i),
				    (OWLProperty) proplist2.get(best), "=", max);
		    //System.out.println( ((OWLProperty) proplist1.get(i)).getURI() + " = " +   ((OWLProperty) proplist2.get(best)).getURI() );
		}
	    } catch (Exception e2) {e2.printStackTrace();}
	}

	for (i = 0; i < nbclass1; i++) {
	    boolean found = false;
	    int best = 0;
	    double max = threshold;
	    for (j = 0; j < nbclass2; j++) {
		if (classmatrix[i][j] > max) {
		    found = true;
		    best = j;
		    max = classmatrix[i][j];
		}
	    }
	    try {
		if (found && max > threshold) {
		    Al.addAlignCell((OWLClass) classlist1.get(i),
				    (OWLClass) classlist2.get(best), "=", max);
		    //	System.out.println( classlist1.get(i).toString()  + " = " +   classlist2.get(best).toString() );
		}
	    } catch (Exception e2) {e2.printStackTrace();}
	}

	for (i = 0; i < nbindividu1; i++) {
	    boolean found = false;
	    int best = 0;
	    double max = threshold;
	    for (j = 0; j < nbindividu2; j++) {
		if (individualmatrix[i][j] > max) {
		    found = true;
		    best = j;
		    max = individualmatrix[i][j];
		}
	    }
	    try {
		if (found && max > threshold) {
		    Al.addAlignCell((OWLIndividual) individulist1.get(i),
				    (OWLIndividual) individulist2.get(best), "=", max);
		    //	System.out.println( individulist1.get(i).toString()  + " = " +   individulist2.get(best).toString() );
		}
	    } catch (Exception e2) {e2.printStackTrace();}
	}
	return (Al);
    }


    /* //JE: ARE THESE METHODS OF ANY USE? ? ? */

    /**
     * @return the Similiraty Measure object
     */
    public Similarity getSimilarity(){return (sim);}
    
    public double getProMatrix(int i, int j){return propmatrix[i][j];}
    
    public double getClassMatrix(int i, int j){return classmatrix[i][j];}

    public double getIndividualMatrix(int i,int j){return individualmatrix[i][j];}

    /**
     * @return the number of class for the ontology 1
     */
    public int getnbclass1(){return nbclass1;}

    /**
     * @return the number of class for the ontology 2
     */
    public int getnbclass2(){return nbclass2;}

    /**
     * @return the number of propriete for the ontology 1
     */
    public int getnbprop1 (){return nbprop1;}

    /**
     * @return the number of properties for the ontology 2
     */
    public int getnbprop2 (){return nbprop2;}

    /**
     * @return the number of Individual for the ontology 1
     */
    public int getnbindividu1(){return nbindividu1;}

    /**
     * @return the number of individual for the ontology 2
     */
    public int getnbindividu2(){return nbindividu2;}
		
}
