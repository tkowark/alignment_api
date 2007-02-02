/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2004-2005, 2007
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
 *
 */

package fr.inrialpes.exmo.align.impl.eval;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.BasicEvaluator;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.Comparator;
import java.util.Vector;
import java.io.PrintWriter;
import java.net.URI;

/**
 * Compute the precision recall graph on 11 points
 * The first alignment is thus the expected one.
 *
 * @author Jerome Euzenat
 * @version $Id$ 
 * 
 * The computation is remotely inspired from the sample programme of
 * Raymond J. Mooney
 * available under GPL from http://www.cs.utexas.edu/users/mooney/ir-course/
 * 
 * Mooney also provides the averaging of these graphs over several queries:
 * unfortunatelly, the resulting graph is not anymore a Precision/Recall graph
 */

public class PRGraphEvaluator extends BasicEvaluator {

    private int STEP = 10;

    // The eleven values of precision and recall
    private double[] precisions = null;

    private Vector points;

    /** Creation **/
    public PRGraphEvaluator(Alignment align1, Alignment align2) {
	super(align1, align2);
	points = new Vector();
    }

    /**
     * Compute precision and recall graphs.
     * The algorithm is as follows:
     * 1) Order the pairs of the found alignment.
     * 2) For 
     */
    public double eval(Parameters params) throws AlignmentException {
	return eval( params, (Object)null );
    }
    public double eval(Parameters params, Object cache) throws AlignmentException {
	// Local variables
	int nbexpected = align1.nbCells();
	int nbfound = 0;
	int nbcorrect = 0;

	// unchecked
	if( params.getParameter("step") != null ){
	    STEP = ((Integer)params.getParameter("step")).intValue();
	}
	precisions = new double[ STEP+1 ];

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
      SortedSet cellSet = new TreeSet(
			    new Comparator() {
				public int compare( Object o1, Object o2 )
				    throws ClassCastException{
				    try {
					//System.err.println(((Cell)o1).getObject1()+" -- "+((Cell)o1).getObject2()+" // "+((Cell)o2).getObject1()+" -- "+((Cell)o2).getObject2());
	  //*/3.0
				    if ( o1 instanceof Cell
					 && o2 instanceof Cell ) {
					if ( ((Cell)o1).getStrength() > ((Cell)o2).getStrength() ){
					    return -1;
					} else if ( ((Cell)o1).getStrength() < ((Cell)o2).getStrength() ){
					    return 1;
					} else if ( (((Cell)o1).getObject1AsURI().getFragment() == null)
						    || (((Cell)o2).getObject1AsURI().getFragment() == null) ) {
					    return -1;
					} else if ( ((Cell)o1).getObject1AsURI().getFragment().compareTo(((Cell)o2).getObject1AsURI().getFragment()) > 0) {
					    return -1;
					} else if ( ((Cell)o1).getObject1AsURI().getFragment().compareTo(((Cell)o2).getObject1AsURI().getFragment()) < 0 ) {
					    return 1;
					} else if ( (((Cell)o1).getObject2AsURI().getFragment() == null)
						    || (((Cell)o2).getObject2AsURI().getFragment() == null) ) {
					    return -1;
					} else if ( ((Cell)o1).getObject2AsURI().getFragment().compareTo(((Cell)o2).getObject2AsURI().getFragment()) > 0) {
					    return -1;
					// We assume that they have different names
					} else { return 1; }
				    } else { throw new ClassCastException(); }
				    } catch ( AlignmentException e) { e.printStackTrace(); return 0;}
				}
			    }
			    );

      // Set the found cells in the sorted structure
      for (Enumeration e = align2.getElements(); e.hasMoreElements();) {
	  cellSet.add( e.nextElement() );
      }

      // Collect the points that change recall
      // (the other provide lower precision from the same recall
      //  and are not considered)
      points.add( new Pair( 0., 1. ) );
      for( Iterator it = cellSet.iterator(); it.hasNext(); ){
	  nbfound++;
	  Cell c2 = (Cell)it.next();
	  //*/3.0
	  Set s1 = (Set)align1.getAlignCells1( c2.getObject1() );
	  if( s1 != null ){
	      for( Iterator it1 = s1.iterator(); it1.hasNext() && c2 != null; ){
		  Cell c1 = (Cell)it1.next();
		  URI uri1 = c1.getObject2AsURI();
		  URI uri2 = c2.getObject2AsURI();	
		  // if (c1.getobject2 == c2.getobject2)
		  if (uri1.toString().equals(uri2.toString())) {
		      nbcorrect++;
		      double recall = (double)nbcorrect / (double)nbexpected;
		      double precision = (double)nbcorrect / (double)nbfound;
		      // Create a new pair to put in the list
		      points.add( new Pair( recall, precision ) );
		      c2 = null; // out of the loop.
		  }
	      }
	  }
      }
      // Now if we want to have a regular curve we must penalize those system
      // that do not reach 100% recall.
      // for that purpose, and for each other bound we add a point with the worse
      // precision which is the required recall level divided with the maximum
      // cardinality possible (i.e., the multiplication of the ontology sizes).
      points.add( new Pair( 1.0, 0. ) ); // useless because 

      // Interpolate curve points at each n-recall level
      // This is inspired form Ray Mooney's program
      // It works backward in the vector,
      //  (in the same spirit as before, the maximum value so far is retained)
      int j = points.size()-1; // index in recall-ordered vector of points
      int i = STEP; // index of the current recall interval
      double level = (double)i/STEP; // max level of that interval
      double best = 0.; // best value found for that interval
      while( j >= 0 ){
	  Pair precrec = (Pair)points.get(j);
	  while ( precrec.getX() < level ){
	      precisions[i] = best;
	      i--;
	      level = (double)i/STEP;
	  };
	  if ( precrec.getY() > best ) best = precrec.getY();
	  j--;
      }
      precisions[0] = best; // It should be 1. that's why it is now added in points.

      return 0.0; // useless
      }

    /**
     * This output the result
     */
    public void write(PrintWriter writer) throws java.io.IOException {
	writer.println("<?xml version='1.0' encoding='utf-8' standalone='yes'?>");
	writer.println("<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>");
	writer.println("  <output rdf:about=''>");
	for( int i=0; i <= STEP; i++ ){
	    writer.print("    <step>\n      <recall>");
	    writer.print((double)i/STEP);
	    writer.print("</recall>\n      <precision>");
	    writer.print(precisions[i]);
	    writer.print("</precision>\n    </step>\n");
	}
	writer.print("  </output>\n</rdf:RDF>\n");
	writePlot( writer );
    }

    /**
     * This output the result
     */
    public void writeFullPlot(PrintWriter writer) throws java.io.IOException {
	for( int j = 0; j < points.size(); j++ ){
	    Pair precrec = (Pair)points.get(j);
	    writer.println( precrec.getX()+" "+precrec.getY() );
	}
    }

    /* Write out the final interpolated recall/precision graph data.
     * One line for each recall/precision point in the form: 'R-value P-value'.
     * This is the format needed for GNUPLOT.
     */
    public void writePlot(PrintWriter writer) throws java.io.IOException {
        for(int i = 0; i < STEP+1; i++){
            writer.println( (double)i/10 + "\t" + precisions[i]);
	}
    }

    public double getPrecision( int i ){
	return precisions[i];
    }
}

class Pair {
    private double x;
    private double y;
    public Pair( double x, double y ){
	this.x = x;
	this.y = y;
    }
    public double getX(){ return x; }
    public double getY(){ return y; }
}
