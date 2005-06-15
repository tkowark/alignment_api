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

import java.lang.ClassNotFoundException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.io.PrintStream;
import java.io.IOException;
import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.Parameters;
import fr.inrialpes.exmo.align.impl.Similarity;

/**
 * Represents an OWL ontology alignment. An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information
 * about those objects.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */


public class DistanceAlignment extends BasicAlignment
{
    Similarity sim;

    /** Creation **/
    public DistanceAlignment( OWLOntology onto1, OWLOntology onto2 ){
    	init( onto1, onto2 );
    };

    public void setSimilarity( Similarity s ) { sim = s; }
    public Similarity getSimilarity() { return sim; }

    public void addAlignDistanceCell( Object ob1, Object ob2, String relation, double measure) throws AlignmentException {
	addAlignCell( ob1, ob2, relation, 1-measure );
    }
    public double getAlignedDistance1( Object ob ) throws AlignmentException {
	return (1 - getAlignedStrength1(ob));
    };
    public double getAlignedDistance2( Object ob ) throws AlignmentException{
	return (1 - getAlignedStrength1(ob));
    };

    /**
     * Extract the alignment form the Similarity
     * FRom OLA
     */
    public Alignment extract(String type, Parameters param) {
      int i = 0, j = 0;
      double max = 0.;
      double threshold = 0.;
      boolean found = false;
      double val = 0;

      if (  param.getParameter("threshold") != null )
	  threshold = ((Double) param.getParameter("threshold")).doubleValue();

      try {
	  for (Iterator it1 = onto1.getObjectProperties().iterator(); it1.hasNext(); ) {
	      OWLProperty prop1 = (OWLProperty)it1.next();
	      found = false; max = threshold; val = 0;
	      OWLProperty prop2 = null;
	      for (Iterator it2 = onto2.getObjectProperties().iterator(); it2.hasNext(); ) {
		  OWLProperty current = (OWLProperty)it2.next();
		  val = sim.getSimilarity(prop1.getURI(),current.getURI());
		  if ( val > max) {
		      found = true; max = val; prop2 = current;
		  }
	      }
	      for (Iterator it2 = onto2.getDataProperties().iterator(); it2.hasNext();) {
		  OWLProperty current = (OWLProperty)it2.next();
		  val = sim.getSimilarity(prop1.getURI(),current.getURI());
		  if ( val > max) {
		      found = true; max = val; prop2 = current;
		  }
	      }
	      if (found && max > threshold) {
		  addAlignCell(prop1,prop2, "=", max);
	      }
	  }
	  for (Iterator it1 = onto1.getDataProperties().iterator(); it1.hasNext(); ) {
	      OWLProperty prop1 = (OWLProperty)it1.next();
	      found = false; max = threshold; val = 0;
	      OWLProperty prop2 = null;
	      for (Iterator it2 = onto2.getObjectProperties().iterator(); it2.hasNext(); ) {
		  OWLProperty current = (OWLProperty)it2.next();
		  val = sim.getSimilarity(prop1.getURI(),current.getURI());
		  if ( val > max) {
		      found = true; max = val; prop2 = current;
		  }
	      }
	      for (Iterator it2 = onto2.getDataProperties().iterator(); it2.hasNext();) {
		  OWLProperty current = (OWLProperty)it2.next();
		  val = sim.getSimilarity(prop1.getURI(),current.getURI());
		  if ( val > max) {
		      found = true; max = val; prop2 = current;
		  }
	      }
	      if (found && max > threshold) {
		  addAlignCell(prop1,prop2, "=", max);
	      }
	  }

	for (Iterator it1 = onto1.getClasses().iterator(); it1.hasNext(); ) {
	    OWLClass class1 = (OWLClass)it1.next();
	    found = false; max = threshold; val = 0;
	    OWLClass class2 = null;
	    for (Iterator it2 = onto2.getClasses().iterator(); it2.hasNext(); ) {
		OWLClass current = (OWLClass)it2.next();
		val = sim.getSimilarity(class1.getURI(),current.getURI());
		if (val > max) {
		    found = true; max = val; class2 = current;
		}
	    }
	    if (found && max > threshold) {
		addAlignCell(class1,class2, "=", max);
	    }
	}
	
	for (Iterator it1 = onto1.getIndividuals().iterator(); it1.hasNext();) {
	    OWLIndividual ind1 = (OWLIndividual)it1.next();
	    found = false; max = threshold; val = 0;
	    OWLIndividual ind2 = null;
	    for (Iterator it2 = onto2.getIndividuals().iterator(); it2.hasNext(); ) {
		OWLIndividual current = (OWLIndividual)it2.next();
		val = sim.getSimilarity(ind1.getURI(),current.getURI());
		if (val > max) {
		    found = true; max = val; ind2 = current;
		}
	    }
	    if (found && max > threshold) {
		addAlignCell(ind1,ind2, "=", max);
	    }
	}
      } catch (Exception e2) {e2.printStackTrace();}
      return((Alignment)this);
    }

	// This mechanism should be parametric!
	// Select the best match
	// There can be many algorithm for these:
	// n:m: get all of those above a threshold
	// 1:1: get the best discard lines and columns and iterate
	// Here we basically implement ?:* because the algorithm
	// picks up the best matching object above threshold for i.

    protected void selectBestMatch( int nbobj1, Vector list1, int nbobj2, Vector list2, double[][] matrix, double threshold, Object way) throws AlignmentException {
	if (debug > 0) System.err.print("Storing class alignment\n");
	
	for ( int i=0; i<nbobj1; i++ ){
	    boolean found = false;
	    int best = 0;
	    double max = threshold;
	    for ( int j=0; j<nbobj2; j++ ){
		if ( matrix[i][j] < max) {
		    found = true;
		    best = j;
		    max = matrix[i][j];
		}
	    }
	    if ( found ) { addAlignDistanceCell( (OWLEntity)list1.get(i), (OWLEntity)list2.get(best), "=", max ); }
	}
    }

}
