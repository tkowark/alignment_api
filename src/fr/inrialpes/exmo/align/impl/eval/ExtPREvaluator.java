/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2004-2006
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
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.impl.model.OWLClassImpl;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLException;

import java.lang.Math;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.io.PrintWriter;
import java.io.IOException;

import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
/**
 * Implement extended precision and recall between alignments.
 * These are the measures corresponding to [Ehrig&Euzenat2005].
 * The implementation is based on that of PRecEvaluator.
 *
 * @author Jerome Euzenat
 * @version $Id$ 
 */

public class ExtPREvaluator extends BasicEvaluator {

    private OWLOntology onto1;
    private OWLOntology onto2;

    private double symALPHA = .5;
    private double editALPHA = .4;
    private double editBETA = .6;
    private double oriented = .5;

    private double symprec = 0.;
    private double symrec = 0.;
    private double effprec = 0.;
    private double effrec = 0.;
    private double orientprec = 0.;
    private double orientrec = 0.;

    private int nbexpected = 0;
    private int nbfound = 0;

    private double symsimilarity = 0;
    private double effsimilarity = 0;
    private double orientsimilarity = 0;

    /** Creation **/
    public ExtPREvaluator(Alignment align1, Alignment align2) {
	super(align1, align2);
	onto1 = (OWLOntology)align1.getOntology1();
	onto2 = (OWLOntology)align1.getOntology2();
    }

    public double getSymPrecision() { return symprec; }
    public double getSymRecall() {	return symrec; }
    public double getSymSimilarity() { return symsimilarity; }

    public double getEffPrecision() { return effprec; }
    public double getEffRecall() {	return effrec; }
    public double getEffSimilarity() { return effsimilarity; }

    public double getOrientPrecision() { return orientprec; }
    public double getOrientRecall() {	return orientrec; }
    public double getOrientSimilarity() { return orientsimilarity; }

    public int getExpected() { return nbexpected; }
    public int getFound() { return nbfound; }

    /**
     * This is a partial implementation of [Ehrig & Euzenat 2005]
     * because the relations are not taken into account
     * (they are supposed to be always =) 
     */
    public double eval(Parameters params) throws AlignmentException {
	nbexpected = align1.nbCells();
	nbfound = align2.nbCells();

	for ( Enumeration e = align1.getElements(); e.hasMoreElements();) {
	    Cell c1 = (Cell)e.nextElement();
	    Set s2 = (Set)align2.getAlignCells1((OWLEntity)c1.getObject1());
	    if( s2 != null ){
		for( Iterator it2 = s2.iterator(); it2.hasNext() && c1 != null; ){
		    Cell c2 = (Cell)it2.next();
		    try {			
			URI uri1 = ((OWLEntity)c1.getObject2()).getURI();
			URI uri2 = ((OWLEntity)c2.getObject2()).getURI();	
			// if (c1.getobject2 == c2.getobject2)
			if ( uri1.toString().equals(uri2.toString()) ) {
			    symsimilarity = symsimilarity + 1.;
			    c1 = null; // out of the loop.
			}
		    } catch (Exception exc) { exc.printStackTrace(); }
		}
		// if nothing has been found
		// JE: Full implementation would require computing a matrix
		// of distances between both set of correspondences and
		// running the Hungarian method...
		Enumeration enum = align2.getElements();
		if ( c1 != null ) {
		    symsimilarity = symsimilarity + computeSymSimilarity(c1,enum);
		}
	    }
	}

	// What is the definition if:
	// nbfound is 0 (p, r are 0)
	// nbexpected is 0 [=> nbcorrect is 0] (r=NaN, p=0[if nbfound>0, NaN otherwise])
	// precision+recall is 0 [= nbcorrect is 0]
	// precision is 0 [= nbcorrect is 0]
	symprec = symsimilarity / (double) nbfound;
	symrec = symsimilarity / (double) nbexpected;
	effsimilarity = symsimilarity;
	effprec = effsimilarity / (double) nbfound;
	effrec = effsimilarity / (double) nbexpected;
	orientsimilarity = symsimilarity;
	orientprec = orientsimilarity / (double) nbfound;
	orientrec = orientsimilarity / (double) nbexpected;
	//System.err.println(">>>> " + nbcorrect + " : " + nbfound + " : " + nbexpected);
	return (result);
    }

    protected double computeSymSimilarity( Cell c1, Enumeration s2 ){
	int minval = 0;
	int val = 0;
	try{ 
	    for( ; s2.hasMoreElements(); ){
		Cell c2 = (Cell)s2.nextElement();
		if ( ((OWLEntity)c1.getObject1()).getURI().toString().equals(((OWLEntity)c2.getObject1()).getURI().toString()) ){
		    val = relativePosition( (OWLEntity)c1.getObject2(), (OWLEntity)c2.getObject2(), onto2 );
		    if ( val != 0 && val < minval ) minval = val;
		} else if ( ((OWLEntity)c1.getObject2()).getURI().toString().equals(((OWLEntity)c2.getObject2()).getURI().toString()) ){
		    val = relativePosition( (OWLEntity)c1.getObject1(), (OWLEntity)c2.getObject1(), onto1 );
		    if ( val != 0 && val < minval ) minval = val;
		}
	    }
	    return symALPHA; //^minval;
	} catch (Exception e) { return 0; }
    }

    protected int relativePosition( OWLEntity o1, OWLEntity o2, OWLOntology onto ){
	try {
	    if ( o1 instanceof OWLClass && o2 instanceof OWLClass ){
		isSuperClass( (OWLClass)o2, (OWLClass)o1, onto );
	    } else if ( o1 instanceof OWLProperty && o2 instanceof OWLProperty ){
		if ( isSuperProperty( (OWLProperty)o2, (OWLProperty)o1, onto ) ) { return -1; }
		else if ( isSuperProperty( (OWLProperty)o1, (OWLProperty)o2, onto ) ) { return 1; }
		else { return 0; }
	    } else if ( o1 instanceof OWLIndividual && o2 instanceof OWLIndividual ){
		return 0;
		//if () { return -1; }
		//else if () { return 1; }
		//else return 0;
	    }
	} catch (OWLException e) { e.printStackTrace(); }
	return 0;
    }

    public int superClassPosition( OWLClass class1, OWLClass class2, OWLOntology onto ) throws OWLException {
	int result = - isSuperClass( (OWLClass)class2, (OWLClass)class1, onto );
	if ( result == 0 )
	    result = isSuperClass( (OWLClass)class1, (OWLClass)class2, onto );
	return result;
    }

    public int isSuperClass( OWLClass class1, OWLClass class2, OWLOntology ontology ) throws OWLException {
	URI uri1 = class1.getURI();
	Set superclasses = new HashSet();
	int level = 0;
	superclasses.addAll(class2.getSuperClasses( ontology ));

	while ( !superclasses.isEmpty() ){
	    Iterator it = superclasses.iterator();
	    level++;
	    superclasses.clear();
	    for( ; it.hasNext() ; ){
		OWLClass entity = (OWLClass)it.next();
		//x.contains( class2 );
		URI uri2 = entity.getURI();	
		//if ( entity == class2 ) return true;
		if ( uri1.toString().equals(uri2.toString()) ) {
		    return level;
		} else {
		    superclasses.addAll(entity.getSuperClasses( ontology ));
		}
	    }
	}
	// get the 
	return 0;
    }

    public boolean isSuperProperty( OWLProperty prop1, OWLProperty prop2, OWLOntology ontology ) throws OWLException {
	URI uri1 = prop1.getURI();
	for( Iterator it = prop2.getSuperProperties( ontology ).iterator(); it.hasNext() ; ){
	    OWLEntity entity = (OWLEntity)it.next();
	    //x.contains( prop2 );
	    URI uri2 = entity.getURI();	
	    //if ( entity == prop2 ) return true;
	    if ( uri1.toString().equals(uri2.toString()) ) {
	        return true;
	    }
	}
	return false;
    }


    /**
     * This now output the results in Lockheed format.
     */
    public void write(PrintWriter writer) throws java.io.IOException {
	writer.println("<?xml version='1.0' encoding='utf-8' standalone='yes'?>");
	writer.println("<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n  xmlns:map='http://www.atl.external.lmco.com/projects/ontology/ResultsOntology.n3#'>");
	writer.println("  <map:output rdf:about=''>");
	//if ( ) {
	//    writer.println("    <map:algorithm rdf:resource=\"http://co4.inrialpes.fr/align/algo/"+align1.get+"\">");
	//}
	try {
	    writer.println("    <map:input1 rdf:resource=\""+((OWLOntology)(align1.getOntology1())).getURI()+"\">");
	    writer.println("    <map:input2 rdf:resource=\""+((OWLOntology)(align1.getOntology2())).getURI()+"\">");
	} catch (OWLException e) { e.printStackTrace(); };
	writer.print("    <map:symmetricprecision>");
	writer.print(symprec);
	writer.print("</map:symmetricprecision>\n    <map:symmetricrecall>");
	writer.print(symrec);
	writer.print("</map:symmetricrecall>\n    <map:effortbasedprecision>");
	writer.print(effprec);
	writer.print("</map:effortbasedprecision>\n    <map:effortbasedrecall>");
	writer.print(effrec);
	writer.print("</map:effortbasedrecall>\n    <map:orientedprecision>");
	writer.print(orientprec);
	writer.print("</map:orientedprecision>\n    <map:orientedrecall>");
	writer.print(orientrec);
	writer.print("</map:orientedrecall>\n  </map:output>\n</rdf:RDF>\n");
    }

}

