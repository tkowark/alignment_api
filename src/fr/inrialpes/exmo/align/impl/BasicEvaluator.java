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
 * This function implements a simple weighted symetric difference.
 * There are many different things to compute in such a function...
 * Add classification per type of objects (Ind, Class, Prop...)
 *
 * @author Jerome Euzenat
 * @version $Id$ 
 */

public class BasicEvaluator implements Evaluator
{
    // NOTE(JE): It will be very easy to compute the score on:
    // - Classes, Properties, Individuals separately
    // And to aggregate them in the final value...
    private double classScore = 0.;
    private double propertyScore = 0.;
    private double individualScore = 0.;
    protected double result = 0.;
    protected Alignment align1;
    protected Alignment align2;

    /** Creation **/
    public BasicEvaluator( Alignment align1, Alignment align2 ){
	this.align1 = align1;
	this.align2 = align2;
    }

    public double evaluate () throws OWLException {
	int n1 = align1.nbCells();
	int n2 = align2.nbCells();
	int d1 = n1; // nb of cells with no counterpart in align1
	int d2 = n2; // nb of cells with no counterpart in align2
	result = 0.;

	for (Enumeration e = align1.getElements() ; e.hasMoreElements() ;) {
	    Cell c1 = (Cell)e.nextElement();
	    Cell c2 = (Cell)align2.getAlignCell1((OWLEntity)c1.getObject1());
	    if ( c2 != null ){
		if ( c1.getObject2() == c2.getObject2() ) {
		    result = result + Math.abs(c2.getMeasure() - c1.getMeasure());
		    d1--;}}
	}
	for (Enumeration e = align2.getElements() ; e.hasMoreElements() ;) {
	    Cell c2 = (Cell)e.nextElement();
	    Cell c1 = (Cell)align1.getAlignCell1((OWLEntity)c2.getObject1());
	    if ( c1 != null ){
		if ( c2.getObject2().equals(c1.getObject2()) ) {
		    result = result + Math.abs(c1.getMeasure() - c2.getMeasure());
		    d2--;}}
	}
	
	result = (d1 + d2 + result) / (n1 + n2);
	return(result);
    }

    public void write( PrintStream writer ) throws java.io.IOException {
	writer.print("<rdf:RDF>\n  <Evaluation class=\"BasicEvaluator\">\n    <result>");
 	writer.print(result);
 	writer.print("</result>\n  </Evaluation>\n</rdf:RDF>\n");
    }

}


