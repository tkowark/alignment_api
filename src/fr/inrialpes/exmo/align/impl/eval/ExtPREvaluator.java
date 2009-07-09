/*
 * $Id$
 *
 * Copyright (C) INRIA, 2004-2009
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
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.BasicEvaluator;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.onto.HeavyLoadedOntology;
import fr.inrialpes.exmo.align.onto.LoadedOntology;
import fr.inrialpes.exmo.align.onto.OntologyFactory;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.io.PrintWriter;

import java.net.URI;

/**
 * Implement extended precision and recall between alignments.
 * These are the measures corresponding to [Ehrig&Euzenat2005].
 * The implementation is based on that of PRecEvaluator.
 *
 * @author Jerome Euzenat
 * @version $Id$ 
 */

public class ExtPREvaluator extends BasicEvaluator {

    private HeavyLoadedOntology<Object> onto1;
    private HeavyLoadedOntology<Object> onto2;

    private double symALPHA = .5;
    //private double editALPHA = .4;
    //private double editBETA = .6;
    //private double oriented = .5;

    private double symprec = 1.;
    private double symrec = 1.;
    private double effprec = 1.;
    private double effrec = 1.;
    private double orientprec = 1.;
    private double orientrec = 1.;

    private int nbexpected = 0;
    private int nbfound = 0;

    private double symsimilarity = 0;
    private double effsimilarity = 0;
    private double orientsimilarity = 0;

    /** Creation **/
    public ExtPREvaluator(Alignment align1, Alignment align2) {
	super(align1, align2);
    }

    public double getSymPrecision() { return symprec; }
    public double getSymRecall() { return symrec; }
    public double getSymSimilarity() { return symsimilarity; }

    public double getEffPrecision() { return effprec; }
    public double getEffRecall() { return effrec; }
    public double getEffSimilarity() { return effsimilarity; }

    public double getOrientPrecision() { return orientprec; }
    public double getOrientRecall() { return orientrec; }
    public double getOrientSimilarity() { return orientsimilarity; }

    public int getExpected() { return nbexpected; }
    public int getFound() { return nbfound; }

    /**
     * This is a partial implementation of [Ehrig & Euzenat 2005]
     * because the relations are not taken into account
     * (they are supposed to be always =) 
     */
    public double eval(Parameters params) throws AlignmentException {
	return eval( params, (Object)null );
    }
    public double eval(Parameters params, Object cache) throws AlignmentException {
	// Better to transform them instead...
	if ( !( align1 instanceof ObjectAlignment ) || !( align2 instanceof ObjectAlignment ) )
	    throw new AlignmentException( "ExtPREvaluation: requires ObjectAlignments" );
	LoadedOntology<Object> o1 = (LoadedOntology<Object>)((ObjectAlignment)align1).getOntologyObject1();
	LoadedOntology<Object> o2 = (LoadedOntology<Object>)((ObjectAlignment)align1).getOntologyObject2();
	if ( !( o1 instanceof HeavyLoadedOntology ) || !( o2 instanceof HeavyLoadedOntology ) )
	    throw new AlignmentException( "ExtPREvaluation: requires HeavyLoadedOntology" );
	onto1 = (HeavyLoadedOntology<Object>)o1;
	onto2 = (HeavyLoadedOntology<Object>)o2;
	nbexpected = align1.nbCells();
	nbfound = align2.nbCells();

	for ( Cell c1 : align1 ){
	    Set s2 = (Set)align2.getAlignCells1( c1.getObject1() );
	    if( s2 != null ){
		for( Iterator it2 = s2.iterator(); it2.hasNext() && c1 != null; ){
		    Cell c2 = (Cell)it2.next();
		    URI uri1 = onto2.getEntityURI( c1.getObject2() );
		    URI uri2 = onto2.getEntityURI( c2.getObject2() );	
		    // if (c1.getobject2 == c2.getobject2)
		    if ( uri1.toString().equals(uri2.toString()) ) {
			symsimilarity += 1.;
			effsimilarity += 1.;
			orientsimilarity += 1.;
			c1 = null; // out of the loop.
		    }
		}
		// if nothing has been found
		// JE: Full implementation would require computing a matrix
		// of distances between both set of correspondences and
		// running the Hungarian method...
		Enumeration e2 = align2.getElements();
		if ( c1 != null ) {
		    // Add guards
		    symsimilarity += computeSymSimilarity(c1,e2);
		    effsimilarity += computeEffSimilarity(c1,e2);
		    orientsimilarity += computeOrientSimilarity(c1,e2);
		}
	    }
	}

	// What is the definition if:
	// nbfound is 0 (p, r are 0)
	// nbexpected is 0 [=> nbcorrect is 0] (r=NaN, p=0[if nbfound>0, NaN otherwise])
	// precision+recall is 0 [= nbcorrect is 0]
	// precision is 0 [= nbcorrect is 0]
	if ( nbfound != 0 ) symprec = symsimilarity / (double) nbfound;
	if ( nbexpected != 0 ) symrec = symsimilarity / (double) nbexpected;
	effsimilarity = symsimilarity;
	if ( nbfound != 0 ) effprec = effsimilarity / (double) nbfound;
	if ( nbexpected != 0 ) effrec = effsimilarity / (double) nbexpected;
	orientsimilarity = symsimilarity;
	if ( nbfound != 0 ) orientprec = orientsimilarity / (double) nbfound;
	if ( nbexpected != 0 ) orientrec = orientsimilarity / (double) nbexpected;
	//System.err.println(">>>> " + nbcorrect + " : " + nbfound + " : " + nbexpected);
	return (result);
    }

    /**
     * This computes similarity depending on structural measures:
     * the similarity is symALPHA^minval, symALPHA being lower than 1.
     * minval is the length of the subclass chain.
     */
    protected double computeSymSimilarity( Cell c1, Enumeration s2 ){
	int minval = 0;
	int val = 0;
	try {
	    for( ; s2.hasMoreElements(); ){
		Cell c2 = (Cell)s2.nextElement();
		if ( onto1.getEntityURI( c1.getObject1() ).toString().equals(onto1.getEntityURI(c2.getObject1()).toString()) ){
		    val = relativePosition( c1.getObject2(), c2.getObject2(), onto2 );
		    if ( val != 0 && val < minval ) minval = val;
		} else if ( onto2.getEntityURI(c1.getObject2()).toString().equals(onto2.getEntityURI(c2.getObject2()).toString()) ){
		    val = relativePosition( c1.getObject1(), c2.getObject1(), onto1 );
		    if ( val != 0 && val < minval ) minval = val;
		}
	    }
	} catch( AlignmentException aex ) { return 0; }
	//return symALPHA; //^minval;
	return Math.pow( symALPHA, minval );
    }

    /**
     * This computes similarity depending on structural measures:
     * the similarity is symALPHA^minval, symALPHA being lower than 1.
     * minval is the length of the subclass chain.
     */
    protected double computeEffSimilarity( Cell c1, Enumeration s2 ){
	return 0.;
    }

    /**
     * This computes similarity depending on structural measures:
     * the similarity is symALPHA^minval, symALPHA being lower than 1.
     * minval is the length of the subclass chain.
     */
    protected double computeOrientSimilarity( Cell c1, Enumeration s2 ){
	return 0.;
    }

    protected int relativePosition( Object o1, Object o2, HeavyLoadedOntology<Object> onto )  throws AlignmentException {
	if ( onto.isClass( o1 ) && onto.isClass( o2 ) ){
	    isSuperClass( o2, o1, onto ); // This is the level
	} else if ( onto.isProperty( o1 ) && onto.isProperty( o2 ) ){
	    if ( isSuperProperty( o2, o1, onto ) ) { return -1; }
	    else if ( isSuperProperty( o1, o2, onto ) ) { return 1; }
	    else { return 0; }
	} else if ( onto.isIndividual( o1 ) && onto.isIndividual( o2 ) ){
	    return 0;
	    //if () { return -1; }
	    //else if () { return 1; }
	    //else return 0;
	}
	return 0;
    }

    public boolean isSuperProperty( Object prop1, Object prop2, HeavyLoadedOntology<Object> ontology ) throws AlignmentException {
	return ontology.getSuperProperties( prop2, OntologyFactory.DIRECT, OntologyFactory.ANY, OntologyFactory.ANY ).contains( prop1 );
    }


    public int superClassPosition( Object class1, Object class2, HeavyLoadedOntology<Object> onto ) throws AlignmentException {
	int result = - isSuperClass( class2, class1, onto );
	if ( result == 0 )
	    result = isSuperClass( class1, class2, onto );
	return result;
    }

    /**
     * This is a strange method which returns an integer representing how
     * directly a class is superclass of another or not.  
     *
     * This would require coputing the transitive reduction of the superClass
     * relation which is currently returned bu HeavyLoadedOntology.
     *
     * It would require to have a isDirectSubClassOf().
     */
    public int isSuperClass( Object class1, Object class2, HeavyLoadedOntology<Object> ontology ) throws AlignmentException {
	URI uri1 = ontology.getEntityURI( class1 );
	Set<Object> bufferedSuperClasses = null;
	Set<Object> superclasses = ontology.getSuperClasses( class1, OntologyFactory.DIRECT, OntologyFactory.ANY, OntologyFactory.ANY );
	int level = 0;

	while ( !superclasses.isEmpty() ){
	    bufferedSuperClasses = superclasses;
	    superclasses = new HashSet<Object>();
	    level++;
	    for( Object entity : bufferedSuperClasses ) {
		if ( ontology.isClass( entity ) ){
		    URI uri2 = ontology.getEntityURI( entity );
		    //if ( entity == class2 ) return true;
		    if ( uri1.toString().equals(uri2.toString()) ) {
			return level;
		    } else {
			superclasses.addAll( ontology.getSuperClasses( entity, OntologyFactory.DIRECT, OntologyFactory.ANY, OntologyFactory.ANY ) );
		    }
		}
	    }
	}
	// get the 
	return 0;
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
	writer.println("    <map:input1 rdf:resource=\""+((ObjectAlignment)align1).getOntologyObject1().getURI()+"\">");
	writer.println("    <map:input2 rdf:resource=\""+((ObjectAlignment)align1).getOntologyObject2().getURI()+"\">");
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

