/*
 * $Id$
 *
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
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Evaluator;
import org.semanticweb.owl.align.Parameters;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;

import java.lang.Math;
import java.lang.ClassNotFoundException;
import java.util.Enumeration;
import java.io.PrintStream;
import java.io.IOException;

import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Evaluate proximity between two alignments.
 * This function implements a simple weighted symetric difference.
 * The highest the value the closest are the alignments:
 * 1: the alignments are exactly the same, with the same strenghts
 * 0: the alignments do not share a single cell
 *
 * @author Jerome Euzenat
 * @version $Id$ 
 */

public class SymMeanEvaluator extends BasicEvaluator
{
    private double classScore = 0.;
    private double propScore = 0.;
    private double indScore = 0.;

    /** Creation **/
    public SymMeanEvaluator( Alignment align1, Alignment align2 ){
	super(align1,align2);
    }

    public double eval( Parameters params ) throws AlignmentException {
	int nbClassCell = 0;
	int nbPropCell = 0;
	int nbIndCell = 0;
	result = 0.;
	classScore = 0.;
	propScore = 0.;
	indScore = 0.;
	
	try {
	    for (Enumeration e = align1.getElements() ; e.hasMoreElements() ;) {
		Cell c1 = (Cell)e.nextElement();
		if ( Class.forName("org.semanticweb.owl.model.OWLClass").isInstance(c1.getObject1()) )
		    nbClassCell++;
		else if ( Class.forName("org.semanticweb.owl.model.OWLProperty").isInstance(c1.getObject1()) )
		    nbPropCell++;
		else nbIndCell++;
		Cell c2 = (Cell)align2.getAlignCell1((OWLEntity)c1.getObject1());
		if ( c2 != null ){
		    if ( c1.getObject2() == c2.getObject2() ) {
			if ( Class.forName("org.semanticweb.owl.model.OWLClass").isInstance(c1.getObject1()) ) {
			    classScore = classScore + 1 - Math.abs(c2.getStrength() - c1.getStrength());
			} else if ( Class.forName("org.semanticweb.owl.model.OWLProperty").isInstance(c1.getObject1()) ) {
			    propScore = propScore + 1 - Math.abs(c2.getStrength() - c1.getStrength());
			} else {
			    indScore = indScore + 1 - Math.abs(c2.getStrength() - c1.getStrength());}}}
	    }
	    for (Enumeration e = align2.getElements() ; e.hasMoreElements() ;) {
		Cell c2 = (Cell)e.nextElement();
		if ( Class.forName("org.semanticweb.owl.model.OWLClass").isInstance(c2.getObject1()))
		    nbClassCell++;
		else if ( Class.forName("org.semanticweb.owl.model.OWLProperty").isInstance(c2.getObject1()) )
		    nbPropCell++;
		else nbIndCell++;
		Cell c1 = (Cell)align1.getAlignCell1((OWLEntity)c2.getObject1());
		if ( c1 != null ){
		    if ( c2.getObject2() == c1.getObject2() ) {
			if ( Class.forName("org.semanticweb.owl.model.OWLClass").isInstance(c2.getObject1()) ) {
			    classScore = classScore + 1 - Math.abs(c1.getStrength() - c2.getStrength());
			} else if ( Class.forName("org.semanticweb.owl.model.OWLProperty").isInstance(c2.getObject1()) ) {
			    propScore = propScore + 1 - Math.abs(c1.getStrength() - c2.getStrength());
			} else {
			    indScore = indScore + 1 - Math.abs(c1.getStrength() - c2.getStrength());}}}
	    }
	} catch (ClassNotFoundException e) { e.printStackTrace(); }

	// Beware, this must come first
	result = (classScore+propScore+indScore) / (nbClassCell+nbPropCell+nbIndCell);
	classScore = classScore / nbClassCell;
	propScore = propScore / nbPropCell;
	indScore = indScore / nbIndCell;
	return(result);
    }

    public void write( PrintStream writer ) throws java.io.IOException {
	writer.print("<rdf:RDF>\n  <Evaluation class=\"SymMeanEvaluator\">\n    <class>");
 	writer.print(classScore);
	writer.print("</class>\n    <properties>");
 	writer.print(propScore);
	writer.print("</properties>\n    <individuals>");
 	writer.print(indScore);
	writer.print("</individuals>\n    <result>");
 	writer.print(result);
 	writer.print("</result>\n  </Evaluation>\n</rdf:RDF>\n");
    }

}
