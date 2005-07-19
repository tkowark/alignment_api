/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2004-2005
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

package fr.inrialpes.exmo.align.impl.eval;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Evaluator;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.BasicEvaluator;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;

import java.lang.Math;
import java.util.Enumeration;
import java.io.PrintWriter;
import java.io.IOException;

import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Compute the precision recall graph on 11 points
 * The first alignment is thus the expected one.
 *
 * @author Jerome Euzenat
 * @version $Id$ 
 */

public class PRGraphEvaluator extends BasicEvaluator {
    // The eleven values of precision and recall
    private double precision[];
    private double recall[];

    /** Creation **/
    public PRGraphEvaluator(Alignment align1, Alignment align2) {
	super(align1, align2);
	precision = new double[11];
	recall = new double[11];
    }

    /**
     *
     * The formulas of P and R are standard:
     * given a reference alignment A
     * given an obtained alignment B
     * which are sets of cells (linking one entity of ontology O to another of ontolohy O').
     *
     * P = |A inter B| / |B|
     * R = |A inter B| / |A|
     * F = 2PR/(P+R)
     * with inter = set intersection and |.| cardinal.
     *
     * They now depend not on all the results but on the results with
     * confidence above each unit.
     * |A| never varies
     * |B| varies each time (and can be decremented when we decrement the
     * set of alignments in A inter B.
     *
     * In the implementation |B|=nbfound, |A|=nbexpected and |A inter B|=nbcorrect.
     */
    public double eval(Parameters params) throws AlignmentException {
	int nbexpected = align1.nbCells();
	int nbfound[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	int nbcorrect[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	// Record the number of found slice by slice
	for (Enumeration e = align2.getElements(); e.hasMoreElements();) {
	    Cell c2 = (Cell) e.nextElement();
	    int j = (int)( c2.getStrength() / .1 );
	    System.err.println(">>>> " + c2.getObject1() + " : " + c2.getObject1() + " : " + c2.getStrength()+" ("+j);

	    //increment the found corresponding;
	    (nbfound[j])++;
	}
	
	// Record the number of correct slice by slice
	for (Enumeration e = align1.getElements(); e.hasMoreElements();) {
	    Cell c1 = (Cell) e.nextElement();
	    try {			
		Cell c2 = (Cell) align2.getAlignCell1((OWLEntity) c1.getObject1());	
		if (c2 != null) {
		    URI uri1 = ((OWLEntity) c1.getObject2()).getURI();
		    URI uri2 = ((OWLEntity) c2.getObject2()).getURI();	
		    // if (c1.getobject2 == c2.getobject2)
		    if (uri1.toString().equals(uri2.toString())) {
			int j = (int)( c2.getStrength() / .1 );
			//increment the correct corresponding;
			(nbcorrect[j])++;
		    }
		}
	    } catch (Exception exc) {
		// Bad URI should not happen there
	    }
	}

	// Compute precision record for each slice
	// What is the definition if:
	// nbfound is 0 (p, r are 0)
	// nbexpected is 0 [=> nbcorrect is 0] (r=NaN, p=0[if nbfound>0, NaN otherwise])
	// precision+recall is 0 [= nbcorrect is 0]
	// precision is 0 [= nbcorrect is 0]
	for ( int i = 10; i >= 0; i-- ){
	    System.err.println(">>>> " + nbcorrect[i] + " : " + nbfound[i] + " : " + nbexpected);
	    precision[i] = (double) nbcorrect[i] / (double) nbfound[i];
	    recall[i] = (double) nbcorrect[i] / (double) nbexpected;
	    if ( i > 0 ) {
		nbcorrect[i-1] = nbcorrect[i-1] + nbcorrect[i];
		nbfound[i-1] = nbfound[i-1] + nbfound[i];
	    }
	}

	return (result);
    }

    /**
     * This now output the Lockheed format. However, the lookheed format
     * was intended to compare two merged ontologies instead of two alignment.
     * So it refered to the:
     * - input ontology A
     * - input ontology B
     * - alignement algorithm (used for obtaining what ????).
     * While we compare two alignments (so the source and the reference to these
     * algorithms should be within the alignment structure.
     */
    public void write(PrintWriter writer) throws java.io.IOException {
	writer.println("<?xml version='1.0' encoding='utf-8' standalone='yes'?>");
	writer.println("<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n  xmlns:map='http://www.atl.external.lmco.com/projects/ontology/ResultsOntology.n3#'>");
	writer.println("  <map:output rdf:about=''>");
	// Missing items:
	// writer.println("    <map:algorithm rdf:resource=\"\">");
	// writer.println("    <map:intutA rdf:resource=\"\">");
	// writer.println("    <map:inputB rdf:resource=\"\">");
	for( int i=0; i <= 10; i++ ){
	    writer.print("    <map:step>\n      <map:precision>");
	    writer.print(precision[i]);
	    writer.print("</map:precision>\n      <map:recall>");
	    writer.print(recall[i]);
	    writer.print("</map:recall>\n    </map:step>\n");
	}
	writer.print("  </map:output>\n</rdf:RDF>\n");
    }

    public double getPrecision(int i) { return precision[i]; }
    public double getRecall(int i) {	return recall[i]; }
}

