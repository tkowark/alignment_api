/*
 * $Id$
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

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Evaluator;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;

import java.lang.Math;
import java.util.Enumeration;
import java.io.PrintStream;
import java.io.IOException;

import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Evaluate proximity between two alignments.
 * This function implements Precision/Recall. The first alignment
 * is thus the expected one.
 *
 * @author Jerome Euzenat
 * @version $Id$ 
 */

public class PRecEvaluator extends BasicEvaluator {
    // NOTE(JE): It will be very easy to compute the score on:
    // - Classes, Properties, Individuals separately
    // And to aggregate them in the final value...
    private double precision = 0.;
    private double recall = 0.;

    /** Creation **/
    public PRecEvaluator( Alignment align1, Alignment align2 ){
	super(align1,align2);
    //	this.align1 = align1;
    //	this.align2 = align2;
    }

    // Presision: nbfound - nbexpected / nbexpected
    // Recall: nbfound / nbexpected
    // ----
    // Signal: 
    // Noise: 
    public double evaluate () throws OWLException {
	int nbexpected = align1.nbCells();
	int nbfound = align2.nbCells();
	int nbcorrect = 0; // nb of cells correctly identified
	precision = 0.;
	recall = 0.;

	for (Enumeration e = align1.getElements() ; e.hasMoreElements() ;) {
	    Cell c1 = (Cell)e.nextElement();
	    Cell c2 = (Cell)align2.getAlignCell1((OWLEntity)c1.getObject1());
	    if ( c2 != null ){
		if ( c1.getObject2() == c2.getObject2() ) {
		    //result = result + Math.abs(c2.getMeasure() - c1.getMeasure());
		    nbcorrect++;
		}
	    }
	}

	precision = (nbfound - nbcorrect) / nbexpected;
	recall = nbcorrect / nbexpected;
	result = recall / precision;
	return(result);
    }

    public void write( PrintStream writer ) throws java.io.IOException {
	writer.print("<rdf:RDF>\n  <Evaluation class=\"PRecEvaluator\">\n    <precision>");
 	writer.print(precision);
 	writer.print("</precision>\n    <recall>");
 	writer.print(recall);
 	writer.print("</recall>\n    <result>");
 	writer.print(result);
 	writer.print("</result>\n  </Evaluation>\n</rdf:RDF>\n");
    }

}


