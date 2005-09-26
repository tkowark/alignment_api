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
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLProperty;

import java.lang.Math;
import java.lang.ClassNotFoundException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.io.PrintWriter;
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
 * The result is 2*w(A\cap B)/|A|+|B|
 * in which w(.) is the sum of the complement of the strength diference between same cells
 * i.e., \Sum_{c\in A, c'\in R; c=c'} (1 - | c.strength - c'.strength |)
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
	
	for (Enumeration e = align1.getElements() ; e.hasMoreElements() ;) {
	    Cell c1 = (Cell)e.nextElement();
	    Set s2 = (Set)align2.getAlignCells1((OWLEntity)c1.getObject1());
	    if( s2 != null ){
		// should be put to the length...of the set
		if ( c1.getObject1() instanceof OWLClass )
		    nbClassCell = nbClassCell + s2.size() ;
		else if ( c1.getObject1() instanceof OWLProperty )
		    nbPropCell = nbPropCell + s2.size();
		else nbIndCell = nbIndCell + s2.size();
		for( Iterator it2 = s2.iterator(); it2.hasNext() && c1 != null; ){
		    Cell c2 = (Cell)it2.next();
		    //try {			
			//URI uri1 = ((OWLEntity)c1.getObject2()).getURI();
			//URI uri2 = ((OWLEntity)c2.getObject2()).getURI();	
			// if (c1.getobject2 == c2.getobject2)
			//if (uri1.toString().equals(uri2.toString())) {
			if ( c1.getObject2() == c2.getObject2() ) {
			    if ( c1.getObject2() instanceof OWLClass ) {
				classScore = classScore + 1 - Math.abs(c2.getStrength() - c1.getStrength());
			    } else if ( c1.getObject2() instanceof OWLProperty ) {
				propScore = propScore + 1 - Math.abs(c2.getStrength() - c1.getStrength());
			    } else {
				indScore = indScore + 1 - Math.abs(c2.getStrength() - c1.getStrength());}}}}}
		
	for (Enumeration e = align2.getElements() ; e.hasMoreElements() ;) {
	    Cell c2 = (Cell)e.nextElement();
	    Set s1 = (Set)align1.getAlignCells2((OWLEntity)c2.getObject1());
	    if( s1 != null ){
		// should be put to the length...of the set
		if ( c2.getObject1() instanceof OWLClass )
		    nbClassCell = nbClassCell + s1.size() ;
		else if ( c2.getObject1() instanceof OWLProperty )
		    nbPropCell = nbPropCell + s1.size();
		else nbIndCell = nbIndCell + s1.size();
		for( Iterator it1 = s1.iterator(); it1.hasNext() && c2 != null; ){
		    Cell c1 = (Cell)it1.next();
		    //try {			
			//URI uri2 = ((OWLEntity)c2.getObject1()).getURI();
			//URI uri1 = ((OWLEntity)c1.getObject1()).getURI();	
			// if (c2.getobject1 == c1.getobject1)
			//if (uri2.toString().equals(uri1.toString())) {
			if ( c2.getObject1() == c1.getObject1() ) {
			    if ( c2.getObject1() instanceof OWLClass ) {
				classScore = classScore + 1 - Math.abs(c1.getStrength() - c2.getStrength());
			    } else if ( c2.getObject1() instanceof OWLProperty ) {
				propScore = propScore + 1 - Math.abs(c1.getStrength() - c2.getStrength());
			    } else {
				indScore = indScore + 1 - Math.abs(c1.getStrength() - c2.getStrength());}}}}}
		
	// Beware, this must come first
	result = (classScore+propScore+indScore) / (nbClassCell+nbPropCell+nbIndCell);
	classScore = classScore / nbClassCell;
	propScore = propScore / nbPropCell;
	indScore = indScore / nbIndCell;
	return(result);
    }

    public void write( PrintWriter writer ) throws java.io.IOException {
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
