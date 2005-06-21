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
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.Similarity;
import fr.inrialpes.exmo.align.impl.ConcatenatedIterator;

/**
 * Represents an OWL ontology alignment. An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information
 * about those objects.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */


public class DistanceAlignment extends BasicAlignment implements AlignmentProcess
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
     * Process matching
     * - create distance data structures,
     * - compute distance or similarity
     * - extract alignment
     **/
    public void align( Alignment alignment, Parameters params ) throws AlignmentException, OWLException {
      if (  params.getParameter("type") != null ) 
	  setType((String)params.getParameter("type"));
      else setType("**");

      getSimilarity().initialize( (OWLOntology)getOntology1(), (OWLOntology)getOntology2(), alignment );
      getSimilarity().compute( params );
      extract( getType(), params );
    }

    /**
     * Extract the alignment form the Similarity
     * There are theoretically 16 types of extractors composing the
     * characteristics
     * [q]estion mark = ?, one or zero relation
     * [s]tar = *, one, zero or many relations
     * [1] = 1, exactly one relation
     * [p]lus = +, one or many relations
     * for each place of the relation. Howerver, since it is not possible from a matrics to guarantee that one object will be in at least one relation, this is restricted to the four following types:
     * ?? (covering 11, 1? and ?1)
     * ** (covering ++, *+ and +*)
     * ?* (covering 1*, 1+ and ?+)
     * *? (covering +?, *1 and +1)
     */
    public Alignment extract(String type, Parameters params) throws AlignmentException {
	double threshold = 0.;
	if (  params.getParameter("threshold") != null )
	    threshold = ((Double) params.getParameter("threshold")).doubleValue();
	
	if ( type.equals("?*") || type.equals("1*") || type.equals("?+") || type.equals("1+") ) return extractqs( threshold, params );
	else if ( type.equals("??") || type.equals("1?") || type.equals("?1") || type.equals("11") ) return extractqs( threshold, params );
	else if ( type.equals("*?") || type.equals("+?") || type.equals("*1") || type.equals("+1") ) return extractqs( threshold, params );
	else if ( type.equals("**") || type.equals("+*") || type.equals("*+") || type.equals("++") ) return extractqs( threshold, params );
	// The else should be an error message
	else throw new AlignmentException("Unknown alignment type: "+type);
    }

    /**
     * Extract the alignment of a ?* type
     */
    public Alignment extractqs( double threshold, Parameters params) {
      int i = 0, j = 0;
      double max = 0.;
      boolean found = false;
      double val = 0;

      try {
	  // Extract for properties
	  ConcatenatedIterator pit1 = new 
	      ConcatenatedIterator(onto1.getObjectProperties().iterator(),
				    onto1.getDataProperties().iterator());
	  for ( ; pit1.hasNext(); ) {
	      OWLProperty prop1 = (OWLProperty)pit1.next();
	      found = false; max = threshold; val = 0;
	      OWLProperty prop2 = null;
	      ConcatenatedIterator pit2 = new 
		  ConcatenatedIterator(onto2.getObjectProperties().iterator(),
					onto2.getDataProperties().iterator());
	      for ( ; pit2.hasNext(); ) {
		  OWLProperty current = (OWLProperty)pit2.next();
		  val = 1 - sim.getPropertySimilarity(prop1,current);
		  if ( val > max) {
		      found = true; max = val; prop2 = current;
		  }
	      }
	      if ( found ) addAlignCell(prop1,prop2, "=", max);
	  }
	  // Extract for classes
	  for (Iterator it1 = onto1.getClasses().iterator(); it1.hasNext(); ) {
	      OWLClass class1 = (OWLClass)it1.next();
	      found = false; max = threshold; val = 0;
	      OWLClass class2 = null;
	      for (Iterator it2 = onto2.getClasses().iterator(); it2.hasNext(); ) {
		  OWLClass current = (OWLClass)it2.next();
		  val = 1 - sim.getClassSimilarity(class1,current);
		  if (val > max) {
		      found = true; max = val; class2 = current;
		  }
	      }
	      if ( found ) {
		  addAlignCell(class1,class2, "=", max);
	      }
	  }
	  // Extract for individuals
	  // This does not work, at least for the OAEI 2005 tests
	  /*	  for (Iterator it1 = onto1.getIndividuals().iterator(); it1.hasNext();) {
	      OWLIndividual ind1 = (OWLIndividual)it1.next();
	      found = false; max = threshold; val = 0;
	      OWLIndividual ind2 = null;
	      for (Iterator it2 = onto2.getIndividuals().iterator(); it2.hasNext(); ) {
		  OWLIndividual current = (OWLIndividual)it2.next();
		  val = 1 - sim.getIndividualSimilarity(ind1,current);
		  if (val > max) {
		      found = true; max = val; ind2 = current;
		  }
	      }
	      System.err.println(ind1+" -- "+ind2+" = "+max);
	      if ( found ) addAlignCell(ind1,ind2, "=", max);
	      }*/
      } catch (Exception e2) {e2.printStackTrace();}
      return((Alignment)this);
    }

}
