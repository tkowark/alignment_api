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

import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.parser.SyntaxElement;

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
 * Compute ROCCurves
 * The first alignment is thus the expected one
 *
 * @author Jerome Euzenat
 * @version $Id$ 
 * 
 * ROCCurves traverse the ranked list of correspondences
 * Hence, like PRGraphs, it is only relevant to this case.
 * X-axis is the number of incorrect correspondences
 * Y-axis is the number of correct correspondences
 * It is expected that the curve grows fast first and then much slower
 * This indicates the accuracy of the matcher.
 *
 * The "Surface Under Curve" (AUC) is returned as a result.
 * AUC is in fact the percentage of surface under the curve (given by N the size of the reference alignment and P the size of all pairs of ontologies, N*P is the full size), Area/N*P.
 * It is ususally interpreted as:
 * 0.9 - 1.0 excellent
 * 0.8 - 0.9 good
 * 0.7 - 0.8 fair
 * 0.6 - 0.7 poor
 * 0.0 - 0.6 bad
 *
 * The problem with these measures is that they assume that the provided alignment
 * is a complete alignment: it contains all pairs of entities hence auc are comparable
 * because they depend on the same sizes... This is not the case when alignment are
 * incomplete. Hence, this should be normalised.
 *
 * There are two ways of doing this:
 * - simply Area/N*P, but this would advantage
 * - considering the current subpart Area/N*|
 *   => both would advantage matchers with high precision
 * penalising them Area/N*P
 * - interpolating the curve: 
 * NOT EASY, TO BE IMPLEMENTED
 */

public class ROCCurveEvaluator extends GraphEvaluator {

    private double auc = 0.0;

    public ROCCurveEvaluator( Alignment align1, Alignment align2 ) {
	super(align1, align2);
    }

    /**
     * Compute ROCCurve
     */
    public double eval( Properties params ) throws AlignmentException {
	return eval( params, (Object)null );
    }
    public double eval( Properties params, Object cache ) throws AlignmentException {
	// Local variables
	int nbfound = 0;
	int area = 0;
	int x = 0;
	int y = 0;
	
	int scale = align2.nbCells();

	points = new Vector<Pair>();

	// Create a sorted structure in which putting the cells
	// TreeSet could be replaced by something else
	SortedSet<Cell> cellSet = orderAlignment();

	// Collect the points in the curve
	points.add( new Pair( 0., 0. ) ); // [Origin]
	for( Cell c2 : cellSet ) {
	    nbfound++;
	    if ( correctCell( c2, align2, align1 ) > 0. ) {
		y++; 
	    } else {
		x++;  area += y;
	    }
	    points.add( new Pair( x, y ) );
	}
	auc = (double)area / (double)nbfound;

	return auc;
    }

    /**
     * This output the result
     */
    public void write(PrintWriter writer) throws java.io.IOException {
	writer.println("<?xml version='1.0' encoding='utf-8' standalone='yes'?>");
	writer.println("<"+SyntaxElement.RDF.print()+" xmlns:"+Namespace.RDF.shortCut+"='"+Namespace.RDF.prefix+"'>");
	writer.println("  <output rdf:about=''>");
	writeXMLMap( writer );
	writer.print("    <AUC>"+auc+"</AUC>\n");
	writer.print("  </output>\n</"+SyntaxElement.RDF.print()+">\n");
    }
    
    public void writePlot(PrintWriter writer) {
	writeFullPlot( writer );
    }
    
    public double getAUC(){
	return auc;
    }
}

