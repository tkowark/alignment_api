/*
 * $Id$
 *
 * Copyright (C) INRIA, 2004-2010
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

import fr.inrialpes.exmo.align.parser.SyntaxElement;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.BasicEvaluator;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

import java.util.Enumeration;
import java.util.Properties;
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

public class ExtPREvaluator extends BasicEvaluator implements Evaluator {

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
    public ExtPREvaluator(Alignment align1, Alignment align2) throws AlignmentException {
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
    public double eval( Properties params ) throws AlignmentException {
	return eval( params, (Object)null );
    }
    public double eval( Properties params, Object cache ) throws AlignmentException {
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
		    try {
			URI uri1 = onto2.getEntityURI( c1.getObject2() );
			URI uri2 = onto2.getEntityURI( c2.getObject2() );	
			// if ( uri1.equals( uri2 ) )
			if ( uri1.toString().equals(uri2.toString()) ) {
			    symsimilarity += 1.;
			    effsimilarity += 1.;
			    orientsimilarity += 1.;
			    c1 = null; // out of the loop.
			}
		    } catch ( OntowrapException owex ) {
			// This may be ignored as well
			throw new AlignmentException( "Cannot find entity URI", owex );
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
	} catch( OntowrapException aex ) { return 0;
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
	try {
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
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Cannot access class hierarchy", owex );
	}
    }

    public boolean isSuperProperty( Object prop1, Object prop2, HeavyLoadedOntology<Object> ontology ) throws AlignmentException {
	try {
	    return ontology.getSuperProperties( prop2, OntologyFactory.DIRECT, OntologyFactory.ANY, OntologyFactory.ANY ).contains( prop1 );
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Cannot interpret isSuperProperty", owex );
	}
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
    @SuppressWarnings("unchecked")
    public int isSuperClass( Object class1, Object class2, HeavyLoadedOntology<Object> ontology ) throws AlignmentException {
	try {
	    URI uri1 = ontology.getEntityURI( class1 );
	    Set<?> bufferedSuperClasses = null;
	    Set<Object> superclasses = (Set<Object>) ontology.getSuperClasses( class1, OntologyFactory.DIRECT, OntologyFactory.ANY, OntologyFactory.ANY );
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
			    superclasses.addAll(ontology.getSuperClasses( entity, OntologyFactory.DIRECT, OntologyFactory.ANY, OntologyFactory.ANY ) );
			}
		    }
		}
	    }
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Cannot find entity URI", owex );
	}
	// get the 
	return 0;
    }


    /**
     * This now output the results in Lockheed format.
     */
    public void write(PrintWriter writer) throws java.io.IOException {
	writer.println("<?xml version='1.0' encoding='utf-8' standalone='yes'?>");
	writer.println("<"+SyntaxElement.RDF.print()+" xmlns:"+Namespace.RDF.shortCut+"='"+Namespace.RDF.prefix+"'\n  xmlns:"+Namespace.ATLMAP.shortCut+"='"+Namespace.ATLMAP.prefix+"'>");
	writer.println("  <"+Namespace.ATLMAP.shortCut+":output "+SyntaxElement.RDF_ABOUT.print()+"=''>");
	//if ( ) {
	//    writer.println("    <"+Namespace.ATLMAP.shortCut+":algorithm "+SyntaxElement.RDF_RESOURCE.print()+"=\"http://co4.inrialpes.fr/align/algo/"+align1.get+"\">");
	//}
	writer.println("    <"+Namespace.ATLMAP.shortCut+":input1 "+SyntaxElement.RDF_RESOURCE.print()+"=\""+((ObjectAlignment)align1).getOntologyObject1().getURI()+"\">");
	writer.println("    <"+Namespace.ATLMAP.shortCut+":input2 "+SyntaxElement.RDF_RESOURCE.print()+"=\""+((ObjectAlignment)align1).getOntologyObject2().getURI()+"\">");
	writer.print("    <"+Namespace.ATLMAP.shortCut+":symmetricprecision>");
	writer.print(symprec);
	writer.print("</"+Namespace.ATLMAP.shortCut+":symmetricprecision>\n    <"+Namespace.ATLMAP.shortCut+":symmetricrecall>");
	writer.print(symrec);
	writer.print("</"+Namespace.ATLMAP.shortCut+":symmetricrecall>\n    <"+Namespace.ATLMAP.shortCut+":effortbasedprecision>");
	writer.print(effprec);
	writer.print("</"+Namespace.ATLMAP.shortCut+":effortbasedprecision>\n    <"+Namespace.ATLMAP.shortCut+":effortbasedrecall>");
	writer.print(effrec);
	writer.print("</"+Namespace.ATLMAP.shortCut+":effortbasedrecall>\n    <"+Namespace.ATLMAP.shortCut+":orientedprecision>");
	writer.print(orientprec);
	writer.print("</"+Namespace.ATLMAP.shortCut+":orientedprecision>\n    <"+Namespace.ATLMAP.shortCut+":orientedrecall>");
	writer.print(orientrec);
	writer.print("</"+Namespace.ATLMAP.shortCut+":orientedrecall>\n  </"+Namespace.ATLMAP.shortCut+":output>\n</"+SyntaxElement.RDF.print()+">\n");
    }

    public Properties getResults() {
	Properties results = new Properties();
	results.setProperty( "symmetric precision", Double.toString( symprec ) );
	results.setProperty( "symmetric recall", Double.toString( symrec ) );
	results.setProperty( "symmetric similarity", Double.toString( symsimilarity ) );
	results.setProperty( "effort-based precision", Double.toString( effprec ) );
	results.setProperty( "effort-based recall", Double.toString( effrec ) );
	results.setProperty( "effort-based similarity", Double.toString( effsimilarity ) );
	results.setProperty( "oriented precision", Double.toString( orientprec ) );
	results.setProperty( "oriented recall", Double.toString( orientrec ) );
	results.setProperty( "oriented similarity", Double.toString( orientsimilarity ) );
	results.setProperty( "nbexpected", Integer.toString( nbexpected ) );
	results.setProperty( "nbfound", Integer.toString( nbfound ) );
	return results;
    }
}

