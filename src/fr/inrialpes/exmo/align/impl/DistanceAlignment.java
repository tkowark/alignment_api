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

import java.lang.ClassNotFoundException;
import java.lang.ClassCastException;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.Comparator;

import fr.inrialpes.exmo.align.onto.LoadedOntology;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Parameters;

/**
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class DistanceAlignment extends ObjectAlignment implements AlignmentProcess
{
    Similarity sim;

    /** Creation **/
    public DistanceAlignment() {}

    // JE: OntoRewr (LoadedOntology -> one step above)
    //public DistanceAlignment( LoadedOntology onto1, LoadedOntology onto2 ){
	// Init must now be triggered explicitely
	//    	init( onto1, onto2 );
    //};

    public void setSimilarity( Similarity s ) { sim = s; }
    public Similarity getSimilarity() { return sim; }

    public void addAlignDistanceCell( Object ob1, Object ob2, String relation, double measure) throws AlignmentException {
	addAlignCell( ob1, ob2, relation, 1-measure );
    }
    public double getAlignedDistance1( Object ob ) throws AlignmentException {
	return (1 - getAlignedStrength1(ob));
    };
    public double getAlignedDistance2( Object ob ) throws AlignmentException {
	return (1 - getAlignedStrength2(ob));
    };

    /**
     * Process matching
     * - create distance data structures,
     * - compute distance or similarity
     * - extract alignment
     **/
    public void align( Alignment alignment, Parameters params ) throws AlignmentException {
	loadInit( alignment );
	if (  params.getParameter("type") != null ) 
	    setType((String)params.getParameter("type"));
	// This is a 1:1 alignment in fact
	else setType("11");
	if ( sim == null )
	    throw new AlignmentException("DistanceAlignment: requires a similarity measure");

	sim.initialize( ontology1(), ontology2(), init );
	//sim.initialize( getOntologyObject1(), getOntologyObject2(), init );
	sim.compute( params );
	if ( params.getParameter("printMatrix") != null ) printDistanceMatrix(params);
	extract( getType(), params );
    }

    public static Parameters getParameters() {
	Parameters p = new BasicParameters();
	p.setParameter("type","11");
	return p;
    }

    /**
     * Prints the distance matrix
     */
    public void printDistanceMatrix( Parameters params ){
	System.out.println("\\documentclass{article}\n");
	System.out.println("\\usepackage{graphics}\n");
	System.out.println("\\begin{document}\n");
	System.out.println("\\begin{figure}");
	sim.printClassSimilarityMatrix("tex");
	System.out.println();
	sim.printPropertySimilarityMatrix("tex");
	System.out.println("\\caption{Class distance with measure "+(String)params.getParameter("stringFunction")+"}");
	System.out.println("\\end{figure}");
	System.out.println("\n\\end{document}");
    }

    /**
     * Suppresses the distance matrix
     */
    public void cleanUp() {
	sim = null;
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

	//System.err.println("The type is "+type+" with length = "+type.length());
	if ( type.equals("?*") || type.equals("1*") || type.equals("?+") || type.equals("1+") ) return extractqs( threshold, params );
	else if ( type.equals("??") || type.equals("1?") || type.equals("?1") || type.equals("11") ) return extractqq( threshold, params );
	else if ( type.equals("*?") || type.equals("+?") || type.equals("*1") || type.equals("+1") ) return extractqs( threshold, params );
	else if ( type.equals("**") || type.equals("+*") || type.equals("*+") || type.equals("++") ) return extractqs( threshold, params );
	// The else should be an error message
	else throw new AlignmentException("Unknown alignment type: "+type);
    }

    /**
     * Extract the alignment of a ?* type
     * Complexity: O(n^2)
     */
    public Alignment extractqs( double threshold, Parameters params) {
      double max = 0.;
      boolean found = false;
      double val = 0;

      try {
	  // Extract for properties
	  ConcatenatedIterator pit1 = new 
	      ConcatenatedIterator(ontology1().getObjectProperties().iterator(),
				   ontology1().getDataProperties().iterator());
	  for ( ; pit1.hasNext(); ) {
	      Object prop1 = pit1.next();
	      found = false; max = threshold; val = 0;
	      Object prop2 = null;
	      ConcatenatedIterator pit2 = new 
		  ConcatenatedIterator(ontology2().getObjectProperties().iterator(),
				       ontology2().getDataProperties().iterator());
	      for ( ; pit2.hasNext(); ) {
		  Object current = pit2.next();
		  val = 1 - sim.getPropertySimilarity(prop1,current);
		  if ( val > max) {
		      found = true; max = val; prop2 = current;
		  }
	      }
	      if ( found ) addAlignCell(prop1,prop2, "=", max);
	  }
	  // Extract for classes
	  for (Iterator it1 = ontology1().getClasses().iterator(); it1.hasNext(); ) {
	      Object class1 = it1.next();
	      found = false; max = threshold; val = 0;
	      Object class2 = null;
	      for (Iterator it2 = ontology2().getClasses().iterator(); it2.hasNext(); ) {
		  Object current = it2.next();
		  val = 1 - sim.getClassSimilarity(class1,current);
		  if (val > max) {
		      found = true; max = val; class2 = current;
		  }
	      }
	      if ( found ) addAlignCell(class1, class2, "=", max);
	  }
	  // Extract for individuals
	  // This does not work, at least for the OAEI 2005 tests
	  if (  params.getParameter("noinst") == null ){
	      for (Iterator it1 = ontology1().getIndividuals().iterator(); it1.hasNext();) {
		  Object ind1 = it1.next();
		  if ( ontology1().getEntityURI( ind1 ) != null ) {
		      found = false; max = threshold; val = 0;
		      Object ind2 = null;
		      for (Iterator it2 = ontology2().getIndividuals().iterator(); it2.hasNext(); ) {
			  Object current = it2.next();
			  if ( ontology2().getEntityURI( current ) != null ) {
			      val = 1 - sim.getIndividualSimilarity(ind1,current);
			      if (val > max) {
				  found = true; max = val; ind2 = current;
			      }
			  }
		      }
		      if ( found ) addAlignCell(ind1,ind2, "=", max);
		  }
	      }
	  }
      } catch (AlignmentException alex) { alex.printStackTrace(); }
      return((Alignment)this);
    }

    /**
     * Extract the alignment of a ?? type
     * 
     * exact algorithm using the Hungarian method
     */
    public Alignment extractqq( double threshold, Parameters params) {
	try {
	    // A STRAIGHTFORWARD IMPLEMENTATION
	    // (redoing the matrix instead of getting it)
	    // For each kind of stuff (cl, pr, ind)
	    // Create a matrix
	    int nbclasses1 = ontology1().nbClasses();
	    int nbclasses2 = ontology2().nbClasses();
	    double[][] matrix = new double[nbclasses1][nbclasses2];
	    Object[] class1 = new Object[nbclasses1];
	    Object[] class2 = new Object[nbclasses2];
	    int i = 0;
	    for (Iterator it1 = ontology1().getClasses().iterator(); it1.hasNext(); i++) {
		class1[i] = it1.next();
	    }
	    int j = 0;
	    for (Iterator it2 = ontology2().getClasses().iterator(); it2.hasNext(); j++) {
		class2[j] = it2.next();
	    }
	    for( i = 0; i < nbclasses1; i++ ){
		for( j = 0; j < nbclasses2; j++ ){
		    matrix[i][j] = 1 - sim.getClassSimilarity(class1[i],class2[j]);
		}
	    }
	    // Pass it to the algorithm
	    int[][] result = HungarianAlgorithm.hgAlgorithm( matrix, "max" );
	    // Extract the result
	    for( i=0; i < result.length ; i++ ){
		// The matrix has been destroyed
		double val = 1 - sim.getClassSimilarity(class1[result[i][0]],class2[result[i][1]]);
		// JE: here using strict-> is a very good idea.
		// it means that alignments with 0. similarity
		// will be excluded from the best match. 
		if( val > threshold ){
		    addCell( new ObjectCell( (String)null, class1[result[i][0]], class2[result[i][1]], BasicRelation.createRelation("="), val ) );
		}
	    }
	} catch (AlignmentException alex) { alex.printStackTrace(); }
	// For properties
	try{
	    int nbprop1 = ontology1().nbProperties();
	    int nbprop2 = ontology2().nbProperties();
	    double[][] matrix = new double[nbprop1][nbprop2];
	    Object[] prop1 = new Object[nbprop1];
	    Object[] prop2 = new Object[nbprop2];
	    int i = 0;
	    ConcatenatedIterator pit1 = new 
		ConcatenatedIterator(ontology1().getObjectProperties().iterator(),
				     ontology1().getDataProperties().iterator());
	    for (; pit1.hasNext(); i++) {
		prop1[i] = pit1.next();
	    }
	    int j = 0;
	    ConcatenatedIterator pit2 = new 
		ConcatenatedIterator(ontology2().getObjectProperties().iterator(),
				     ontology2().getDataProperties().iterator());
	    for (; pit2.hasNext(); j++) {
		prop2[j] = pit2.next();
	    }
	    for( i = 0; i < nbprop1; i++ ){
		for( j = 0; j < nbprop2; j++ ){
		    matrix[i][j] = 1 - sim.getPropertySimilarity(prop1[i],prop2[j]);
		}
	    }
	    // Pass it to the algorithm
	    int[][] result = HungarianAlgorithm.hgAlgorithm( matrix, "max" );
	    // Extract the result
	    for( i=0; i < result.length ; i++ ){
		// The matrix has been destroyed
		double val = 1 - sim.getPropertySimilarity(prop1[result[i][0]],prop2[result[i][1]]);
		// JE: here using strict-> is a very good idea.
		// it means that alignments with 0. similarity
		// will be excluded from the best match. 
		if( val > threshold ){
		    addCell( new ObjectCell( (String)null, prop1[result[i][0]], prop2[result[i][1]], BasicRelation.createRelation("="), val ) );
		}
	    }
	} catch (AlignmentException alex) { alex.printStackTrace(); }
	return((Alignment)this);
    }

    /**
     * Naive algorithm:
     * 1) dump the part of the matrix distance above threshold in a sorted set
     * 2) traverse the sorted set and each time a correspondence involving two
     *    entities that have no correspondence is encountered, add it to the 
     *    alignment.
     * Complexity: O(n^2.logn)
     * Pitfall: no global optimality is warranted
     * for instance if there is the following matrix:
     * (a,a')=1., (a,b')=.9, (b,a')=.9, (b,b')=.1
     * This algorithm will select the first and last correspondances of
     * overall similarity 1.1, while the optimum is the second solution
     * with overall of 1.8.
     */
    public Alignment extractqqNaive( double threshold, Parameters params) {
	Object ent1=null, ent2=null;
	double val = 0;
	//TreeSet could be replaced by something else
	//The comparator must always tell that things are different!
	/*SortedSet cellSet = new TreeSet(
			    new Comparator() {
				public int compare( Object o1, Object o2 )
				    throws ClassCastException{
				    if ( o1 instanceof Cell
					 && o2 instanceof Cell ) {
					if ( ((Cell)o1).getStrength() > ((Cell)o2).getStrength() ){
					    return -1;
					} else { return 1; }
				    } else {
					throw new ClassCastException();
					}}});*/
      SortedSet<Cell> cellSet = new TreeSet<Cell>(
			    new Comparator<Cell>() {
				public int compare( Cell o1, Cell o2 )
				    throws ClassCastException{
				    try {
					//System.err.println(o1.getObject1()+" -- "+o1.getObject2()+" // "+o2.getObject1()+" -- "+o2.getObject2());
					if ( o1.getStrength() > o2.getStrength() ){
					    return -1;
					} else if ( o1.getStrength() < o2.getStrength() ){
					    return 1;
					} else if ( ontology1().getEntityName( o1.getObject1() ) == null
						    || ontology2().getEntityName( o2.getObject1() ) == null ) {
					    return -1;
					} else if ( ontology1().getEntityName( o1.getObject1()).compareTo( ontology2().getEntityName( o2.getObject1() ) ) > 0 ) {
					    return -1;
					} else if ( ontology1().getEntityName( o1.getObject1()).compareTo( ontology2().getEntityName( o2.getObject1() ) ) < 0 ) {
					    return 1;
					} else if ( ontology1().getEntityName( o1.getObject2() ) == null
						    || ontology2().getEntityName( o2.getObject2() ) == null ) {
					    return -1;
					} else if ( ontology1().getEntityName( o1.getObject2()).compareTo( ontology2().getEntityName( o2.getObject2() ) ) > 0 ) {
					    return -1;
					// Assume they have different names
					} else { return 1; }
				    } catch ( AlignmentException e) { 
					e.printStackTrace(); return 0;}
				}
			    }
			    );

      try {
	  // Get all the matrix above threshold in the SortedSet
	  // Plus a map from the objects to the cells
	  // O(n^2.log n)
	  ConcatenatedIterator pit1 = new 
	      ConcatenatedIterator(ontology1().getObjectProperties().iterator(),
				   ontology1().getDataProperties().iterator());
	  for (; pit1.hasNext(); ) {
	      ent1 = pit1.next();
	      ConcatenatedIterator pit2 = new 
		  ConcatenatedIterator(ontology2().getObjectProperties().iterator(),
					ontology2().getDataProperties().iterator());
	      for (; pit2.hasNext(); ) {
		  ent2 = pit2.next();
		  val = 1 - sim.getPropertySimilarity( ent1, ent2 );
		  if ( val > threshold ){
		      cellSet.add( new ObjectCell( (String)null, ent1, ent2, BasicRelation.createRelation("="), val ) );
		  }
	      }
	  }
	  for (Iterator it1 = ontology1().getClasses().iterator(); it1.hasNext(); ) {
	      ent1 = it1.next();
	      for (Iterator it2 = ontology2().getClasses().iterator(); it2.hasNext(); ) {
		  ent2 = it2.next();
		  val = 1 - sim.getClassSimilarity( ent1, ent2 );
		  if ( val > threshold ){
		      cellSet.add( new ObjectCell( (String)null, ent1, ent2, BasicRelation.createRelation("="), val ) );
		  }
	      }
	  }
	  // OLA with or without instances
	  if (  params.getParameter("noinst") == null ){
	      for (Iterator it1 = ontology1().getIndividuals().iterator(); it1.hasNext();) {
		  ent1 = it1.next();
		  if ( ontology1().getEntityURI( ent1 ) != null ) {

		      for (Iterator it2 = ontology2().getIndividuals().iterator(); it2.hasNext(); ) {
			  ent2 = it2.next();
			  if ( ontology2().getEntityURI( ent2 ) != null ) {
			      val = 1 - sim.getIndividualSimilarity( ent1, ent2 );
			      if ( val > threshold ){
				  cellSet.add( new ObjectCell( (String)null, ent1, ent2, BasicRelation.createRelation("="), val ) );
			      }
			  }
		      }
		  }
	      }
	  }

	  // O(n^2)
	  for( Iterator it = cellSet.iterator(); it.hasNext(); ){
	      Cell cell = (Cell)it.next();
	      ent1 = cell.getObject1();
	      ent2 = cell.getObject2();
	      if ( (getAlignCells1( ent1 ) == null) && (getAlignCells2( ent2 ) == null) ){
		  // The cell is directly added!
		  addCell( cell );
	      }
	  };

      } catch (AlignmentException alex) {
	  alex.printStackTrace();
      };
      return((Alignment)this);
    }

}
