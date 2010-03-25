/*
 * $Id$
 *
 * Copyright (C) INRIA, 2004-2005, 2007-2010
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

import fr.inrialpes.exmo.align.impl.BasicEvaluator;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Set;
import java.util.Vector;
import java.util.SortedSet;
import java.util.Comparator;
import java.io.PrintWriter;
import java.net.URI;

/**
 * GraphEvaluator: an abstraction that is used for providing evaluation curves
 * instead of values (or sets of values)
 * Pair: only used for recording sets of points in a curve
 *
 * GraphEvaluator is used (generically) in the following way:
 * - create a GraphEvaluator (new GraphEvaluator)
 * - fill it with the set of results that you want to evaluate
 *   (.ingest( Alignment, Alignment) and this repetively
 * - Finally create plot (.eval() )
 *
 * This abstract class provides the ingest method but not eval which has to be 
 * implemented in subclasses. ingest can be rewritten as well.
 *
 * @author Jerome Euzenat
 * @version $Id$ 
 */

public abstract class GraphEvaluator {

    /**
     * The resolution of the provided result: by STEP steps
     */
    protected int STEP = 10;

    protected int nbexpected = 0;
    protected SortedSet<EvalCell> cellSet = null;
    public Vector<Pair> points;

    /**
     * Returns the points to display in a graph
     */
    public abstract Vector<Pair> eval() throws AlignmentException;
    /**
     * Returns the points to display in a graph
     */
    public abstract Vector<Pair> eval( Properties params ) throws AlignmentException;
    /**
     * Retuns a simple global evaluation measure if any
     */
    public abstract double getGlobalResult();

    public void setStep( int i ) { 
	if ( 0 < i && i <= 100 ) STEP = i;
    }
    public int getStep() { return STEP; }

    /** Creation:
     * A priori, evaluators can deal with any kind of alignments.
     * However, it will not work if these are not of the same type.
     **/
    public GraphEvaluator() {
	initCellSet();
    }

    protected void initCellSet () {
	// Create a sorted structure in which putting the cells
	// TreeSet could be replaced by something else
	cellSet = new TreeSet<EvalCell>(
			    new Comparator<EvalCell>() {
				public int compare( EvalCell o1, EvalCell o2 )
				    throws ClassCastException {
				    //try {
					//System.err.println(((Cell)o1).getObject1()+" -- "+((Cell)o1).getObject2()+" // "+o2.getObject1()+" -- "+o2.getObject2());
				    if ( o1.cell instanceof Cell && o2.cell instanceof Cell ) {
					if ( o1.cell.getStrength() > o2.cell.getStrength() ){
					    return -1;
					} else if ( o1.cell.getStrength() < o2.cell.getStrength() ){
					    return 1;
					//The comparator must always tell that things are different!
					} else if ( o1.correct ) {
					    return -1;
					} 
					/*else if ( (o1.cell.getObject1AsURI(align1).getFragment() == null)
						    || (o2.cell.getObject1AsURI(align2).getFragment() == null) ) {
					    return -1;
					} else if ( o1.cell.getObject1AsURI(align1).getFragment().compareTo(o2.cell.getObject1AsURI(align2).getFragment()) > 0) {
					    return -1;
					} else if ( o1.cell.getObject1AsURI(align1).getFragment().compareTo(o2.cell.getObject1AsURI(align2).getFragment()) < 0 ) {
					    return 1;
					} else if ( (o1.cell.getObject2AsURI(align1).getFragment() == null)
						    || (o2.cell.getObject2AsURI(align2).getFragment() == null) ) {
					    return -1;
					} else if ( o1.cell.getObject2AsURI(align1).getFragment().compareTo(o2.cell.getObject2AsURI(align2).getFragment()) > 0) {
					    return -1;
					// We assume that they have different names
					} */ else { return 1; }
				    } else { throw new ClassCastException(); }
				    //} catch ( AlignmentException e) { e.printStackTrace(); return 0;}
				}
			    }
			    );
    }

    /*
     * Tells if the cell is found in the reference alignment
     * (without relation consideration)
     */
    public void ingest( Alignment al, Alignment ref ){
	nbexpected += ref.nbCells();
	// Set the found cells in the sorted structure
	if ( al == null ) return;
	for ( Cell c : al ) {
	    cellSet.add( new EvalCell( c, isCorrect( c, ref ) ) );
	}
    }

    public int nbCells() {
	if ( cellSet == null ) return 0;
	else return cellSet.size();
    }

    /*
     * Tells if the cell is found in the reference alignment
     * (without relation consideration)
     */
    public boolean isCorrect( Cell c, Alignment ref ) {
	try {
	    Set<Cell> s2 = ref.getAlignCells1( c.getObject1() );
	    if( s2 == null ) return false;
	    URI uri1 = c.getObject2AsURI();
	    for( Cell c2 : s2 ){
		URI uri2 = c2.getObject2AsURI();	
		if (uri1.toString().equals(uri2.toString())) {
		    return true;
		}
	    }
	} catch ( AlignmentException aex ) {
	    aex.printStackTrace(); 
	}
	return false;
    }

    /**
     * This output the resulting plot in XML
     */
    public void writeXMLMap( PrintWriter writer) throws java.io.IOException {
	for( Pair precrec: points ) {
	    writer.print("    <step>\n      <x>");
	    writer.print( precrec.getX() );
	    writer.print("</x>\n      <y>");
	    writer.print( precrec.getY() );
	    writer.print("</y>\n    </step>\n");
	}
    }

    /* Write out the final interpolated recall/precision graph data.
     * One line for each recall/precision point in the form: 'R-value P-value'.
     * This is the format needed for GNUPLOT.
     */
    public void writePlot( PrintWriter writer ) {
	for( Pair p : points ){
            writer.println( p.getX()/10 + "\t" + p.getY() );
	}
    }

}

class EvalCell {
    Cell cell = null;
    boolean correct = false;

    public EvalCell( Cell c, boolean b ){
	cell = c;
	correct = b;
    }

    public boolean correct() { return correct; }
    public Cell cell() { return cell; }
}
