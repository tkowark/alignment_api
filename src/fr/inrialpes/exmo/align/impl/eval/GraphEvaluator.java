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
import java.util.SortedSet;
import java.util.Comparator;
import java.util.Vector;
import java.io.PrintWriter;
import java.net.URI;

/**
 * GraphEvaluator: an abstraction that is used for providing evaluation curves
 * instead of values (or sets of values)
 * Pair: only used for recording sets of points in a curve
 *
 * @author Jerome Euzenat
 * @version $Id$ 
 */

public abstract class GraphEvaluator extends BasicEvaluator {

    public Vector<Pair> points = null;

    /** Creation:
     * A priori, evaluators can deal with any kind of alignments.
     * However, it will not work if these are not of the same type.
     **/
    public GraphEvaluator( Alignment align1, Alignment align2 ) {
	super(align1, align2);
	if ( align1.getClass() != align2.getClass() ) {
	    // This should throw an exception...
	}
	points = new Vector<Pair>();
    }

    /**
     * Compute precision and recall graphs.
     * The algorithm is as follows:
     * 1) Order the pairs of the found alignment.
     * 2) For 
     */
    public double eval( Properties params ) throws AlignmentException {
	return eval( params, (Object)null );
    }

    public SortedSet<Cell> orderAlignment() {
	// Create a sorted structure in which putting the cells
	// TreeSet could be replaced by something else
	SortedSet<Cell> cellSet = new TreeSet<Cell>(
			    new Comparator<Cell>() {
				public int compare( Cell o1, Cell o2 )
				    throws ClassCastException {
				    try {
					//System.err.println(((Cell)o1).getObject1()+" -- "+((Cell)o1).getObject2()+" // "+o2.getObject1()+" -- "+o2.getObject2());
				    if ( o1 instanceof Cell && o2 instanceof Cell ) {
					if ( o1.getStrength() > o2.getStrength() ){
					    return -1;
					} else if ( o1.getStrength() < o2.getStrength() ){
					    return 1;
					//The comparator must always tell that things are different!
					} else if ( (o1.getObject1AsURI(align1).getFragment() == null)
						    || (o2.getObject1AsURI(align2).getFragment() == null) ) {
					    return -1;
					} else if ( o1.getObject1AsURI(align1).getFragment().compareTo(o2.getObject1AsURI(align2).getFragment()) > 0) {
					    return -1;
					} else if ( o1.getObject1AsURI(align1).getFragment().compareTo(o2.getObject1AsURI(align2).getFragment()) < 0 ) {
					    return 1;
					} else if ( (o1.getObject2AsURI(align1).getFragment() == null)
						    || (o2.getObject2AsURI(align2).getFragment() == null) ) {
					    return -1;
					} else if ( o1.getObject2AsURI(align1).getFragment().compareTo(o2.getObject2AsURI(align2).getFragment()) > 0) {
					    return -1;
					// We assume that they have different names
					} else { return 1; }
				    } else { throw new ClassCastException(); }
				    } catch ( AlignmentException e) { e.printStackTrace(); return 0;}
				}
			    }
			    );

	// Set the found cells in the sorted structure
	for ( Cell c : align2 ) {
	    cellSet.add( c );
	}

	return cellSet;
    }

    /*
     * This checks if a particular cell is in the reference alignment or not.
     * This could be changed for other kind of correctness (e.g., Semantics).
     */
    public double correctCell( Cell c2, Alignment align2, Alignment refalign ) throws AlignmentException {
	Set s1 = (Set)refalign.getAlignCells1( c2.getObject1() );
	if( s1 != null ) { // for all cells matching our first entity
	    for( Iterator it1 = s1.iterator(); it1.hasNext(); ){
		Cell c1 = (Cell)it1.next();
		URI uri1 = c1.getObject2AsURI(refalign);
		URI uri2 = c2.getObject2AsURI(align2);	
		if (uri1.toString().equals(uri2.toString())) { //This cell matches a correct one
		    return 1.;
		}
	    }
	}
	return 0.;
    }

    /**
     * This output the result
     */
    public void writeXMLMap(PrintWriter writer) throws java.io.IOException {
	for( int j = 0; j < points.size(); j++ ){
	    Pair precrec = points.get(j);
	    writer.print("    <step>\n      <x>");
	    writer.print( precrec.getX() );
	    writer.print("</x>\n      <y>");
	    writer.print( precrec.getY() );
	    writer.print("</y>\n    </step>\n");
	}
    }

    /**
     * This output the result
     */
    public void writeFullPlot(PrintWriter writer) {
	for( int j = 0; j < points.size(); j++ ){
	    Pair precrec = points.get(j);
	    writer.println( precrec.getX()+" "+precrec.getY() );
	}
    }
    
    /* Write out the final interpolated recall/precision graph data.
     * One line for each recall/precision point in the form: 'R-value P-value'.
     * This is the format needed for GNUPLOT.
    public void writePlot( PrintWriter writer ) throws java.io.IOException {
        for(int i = 0; i < STEP+1; i++){
            writer.println( (double)i/10 + "\t" + precisions[i]);
	}
    }
     */

    public void writePlot( PrintWriter writer ) {
	// Print header
	int size = points.size();
	writer.println("#Curve 0, "+size+" points");
	writer.println("#x y type");
	writer.println("%% Plot generated by GraphEvaluator of alignapi");
	writer.println("%% Include in PGF tex by:\n");
	writer.println("%% \\begin{tikzpicture}[cap=round]");
	writer.println("%% \\draw[step="+size+"cm,very thin,color=gray] (-0.2,-0.2) grid ("+size+","+size+");");
	writer.println("%% \\draw[->] (-0.2,0) -- (10.2,0) node[right] {$recall$}; ");
	writer.println("%% \\draw[->] (0,-0.2) -- (0,10.2) node[above] {$precision$}; ");
	//writer.println("%% \\draw plot[mark=+,smooth] file {"+algo+".table};");
	writer.println("%% \\end{tikzpicture}");
	writer.println();
	for( int j = 0; j < size; j++ ){
	    Pair precrec = points.get(j);
	    writer.println( precrec.getX()+" "+precrec.getY() );
	}
    }

}
