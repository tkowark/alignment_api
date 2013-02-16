/*
 * $Id$
 *
 * Copyright (C) INRIA, 2009-2013
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
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.BasicEvaluator;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.ObjectCell;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.rel.*;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;

import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

//import fr.paris8.iut.info.iddl.IDDLReasoner;
//import fr.paris8.iut.info.iddl.conf.Semantics;

// HermiT implementation

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyIRIMapperImpl;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.BufferingMode;

import org.semanticweb.HermiT.Reasoner;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.Thread;
import java.lang.Runnable;

import java.util.Properties;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.io.PrintWriter;
import java.net.URI;

/**
 * Evaluate proximity between two alignments.
 * This function implements Precision/Recall. The first alignment
 * is thus the expected one.
 *
 * @author Jerome Euzenat
 * @version $Id$ 
 */

public class SemPRecEvaluator extends PRecEvaluator implements Evaluator {

    private int nbfoundentailed = 0; // nb of returned cells entailed by the reference alignment
    private int nbexpectedentailed = 0; // nb of reference cells entailed by returned alignment

    //private Semantics semantics = Semantics.DL; // the semantics used for interpreting alignments

    /** Creation
     * Initiate Evaluator for precision and recall
     * @param align1 : the reference alignment
     * @param align2 : the alignment to evaluate
     **/
    public SemPRecEvaluator( Alignment al1, Alignment al2) throws AlignmentException {
	super(((BasicAlignment)al1).toURIAlignment(), ((BasicAlignment)al2).toURIAlignment());
	try {
	    if ( al1 instanceof ObjectAlignment ) {
		align1 = al1;
	    } else {
		align1 = ObjectAlignment.toObjectAlignment((URIAlignment)align1);
	    }
	    if ( al2 instanceof ObjectAlignment ) {
		align2 = al2;
	    } else {
		align2 = ObjectAlignment.toObjectAlignment((URIAlignment)align2);
	    }
	} catch ( AlignmentException aex ) {
	    throw new AlignmentException( "SemPRecEvaluator can only work on ObjectAlignments", aex );
	}
    }

    public void init( Object sem ){
	super.init(); // ??
	nbexpectedentailed = 0;
	nbfoundentailed = 0;
	// Better use Properties
	//if ( sem instanceof Semantics ) {
	//    semantics = (Semantics)sem;
	//}
    }

    /**
     *
     * The formulas are standard:
     * given a reference alignment A
     * given an obtained alignment B
     * which are sets of cells (linking one entity of ontology O to another of ontolohy O').
     *
     * P = |A inter B| / |B|
     * R = |A inter B| / |A|
     * F = 2PR/(P+R)
     * with inter = set intersection and |.| cardinal.
     *
     * In the implementation |B|=nbfound, |A|=nbexpected and |A inter B|=nbcorrect.
     * 
     * This takes semantivs as a parameter which should be a litteral of fr.paris8.iut.info.iddl.conf.Semantics
     */
    public double eval( Properties params ) throws AlignmentException {
	init( params.getProperty( "semantics" ) );
	nbfound = align2.nbCells();
	nbexpected = align1.nbCells();

	nbfoundentailed = nbEntailedCorrespondences( (ObjectAlignment)align1, (ObjectAlignment)align2 );
	nbexpectedentailed = nbEntailedCorrespondences( (ObjectAlignment)align2, (ObjectAlignment)align1 );
	
	precision = (double)nbfoundentailed / (double)nbfound;
	recall = (double)nbexpectedentailed / (double)nbexpected;
	return computeDerived();
    }

    public int getFoundEntailed() { return nbfoundentailed; }
    public int getExpectedEntailed() { return nbexpectedentailed; }

    public Properties getResults() {
	Properties results = super.getResults();
	results.setProperty( "nbexpectedentailed", Integer.toString( nbexpectedentailed ) );
	results.setProperty( "nbfoundentailed", Integer.toString( nbfoundentailed ) );
	return results;
    }

    public int nbEntailedCorrespondences( ObjectAlignment al1, ObjectAlignment al2 ) throws AlignmentException {
	//System.err.println( "Computing correctness" );
	//IDDLReasoner reasoner = new IDDLReasoner( semantics );
	//loadOntology( reasoner, ((BasicAlignment)al1).getOntologyObject1() );
	//loadOntology( reasoner, ((BasicAlignment)al1).getOntologyObject2() );
	//reasoner.addAlignment( al1 );
	loadPipedAlignedOntologies( al1 );
	if ( !reasoner.isConsistent() ) return al2.nbCells(); // everything is entailed
	//System.err.println( al1+" is consistent" );
	int entailed = 0;
	for ( Cell c2 : al2 ) {
	    // create alignment
	    //Alignment al = new ObjectAlignment();
	    //al.init( align2.getOntology1URI(), align2.getOntology2URI() );
	    // add the cell
	    //al.addAlignCell( c2.getObject1(), c2.getObject2(), c2.getRelation().getRelation(), 1. );
	    //System.err.println( c2.getObject1()+" "+c2.getRelation().getRelation()+" "+c2.getObject2() );
	    //if ( reasoner.isEntailed( al ) ) {
	    try {
		if ( reasoner.isEntailed( correspToAxiom( al2, (ObjectCell)c2 ) ) ) {
		    //System.err.println( "      --> entailed" );
		    entailed++;
		}
	    } catch ( AlignmentException aex ) { // type mismatch -> 0
		//System.err.println( "Cannot be translated." );
	    }
	}
	return entailed;
    }

    /**
     * It would be useful to do better since we have the two ontologies here
     */
    protected OWLOntologyManager manager = null;
    protected OWLReasoner reasoner = null;

    /* 
     * Loads the Aligned ontologies without intermediate file
     */
    public void loadPipedAlignedOntologies( final ObjectAlignment align ) throws AlignmentException {
	PipedInputStream in = new PipedInputStream();
	try {
	final PipedOutputStream out = new PipedOutputStream(in);
	    new Thread(
		       new Runnable(){
			   public void run() {
			       PrintWriter writer;
			       try {
				   writer = new PrintWriter (
								     new BufferedWriter(
											new OutputStreamWriter( out, "UTF-8" )), true);
			       } catch ( Exception ex ) {
				   return; //throw new AlignmentException( "Cannot load alignments because of I/O errors" );
			       }
			       OWLAxiomsRendererVisitor renderer = new OWLAxiomsRendererVisitor( writer );
			       renderer.init( new Properties() );
			       // Generate the ontology as OWL Axioms
			       try {
				   align.render( renderer );
			       } catch ( AlignmentException aex ) {
				   return;
			       } finally {
				   writer.flush();
				   writer.close();
			       }	    
			   }
		       }
		       ).start();
	} catch ( Exception ex ) {};//java.io.UnsupportedEncodingException

	manager = OWLManager.createOWLOntologyManager();
	//System.err.println( al.getOntology1URI()+" ----> "+al.getFile1() );
	//System.err.println( al.getOntology2URI()+" ----> "+al.getFile2() );
	manager.addIRIMapper(new SimpleIRIMapper( IRI.create( align.getOntology1URI() ), 
						  IRI.create( align.getFile1() ) ) );
	manager.addIRIMapper(new SimpleIRIMapper( IRI.create( align.getOntology2URI() ), 
						  IRI.create( align.getFile2() ) ) );
	try {
	    manager.loadOntologyFromOntologyDocument( IRI.create( align.getFile1() ) );
	    manager.loadOntologyFromOntologyDocument( IRI.create( align.getFile2() ) );
	    // Load the ontology stream
	    OWLOntology ontology = manager.loadOntologyFromOntologyDocument( in );
	    reasoner = new Reasoner( ontology );
	} catch ( OWLOntologyCreationException ooce ) { 
	    ooce.printStackTrace(); 
	}
    }

    /* 
     * Loads the Aligned ontologies through an intermediate file
     */
    public void loadFileAlignedOntologies( ObjectAlignment align ) throws AlignmentException {
	// Render the alignment
	PrintWriter writer = null;
	File merged = null;
	try {
	    merged = File.createTempFile( "spreval",".owl");
	    merged.deleteOnExit();
	    writer = new PrintWriter ( new FileWriter( merged, false ), true );
	    OWLAxiomsRendererVisitor renderer = new OWLAxiomsRendererVisitor(writer);
	    //renderer.init( new Properties() );
	    align.render(renderer);
	} catch (UnsupportedEncodingException uee) {
	    uee.printStackTrace();
	} catch (AlignmentException ae) {
	    ae.printStackTrace();
	} catch (IOException ioe) { 
	    ioe.printStackTrace();
	} finally {
	    if ( writer != null ) {
		writer.flush();
		writer.close();
	    }
	}

	// Load the ontology 
	manager = OWLManager.createOWLOntologyManager();
	try {
	    manager.addIRIMapper(new SimpleIRIMapper( IRI.create( align.getOntology1URI() ), 
						      IRI.create( align.getFile1() ) ) );
	    manager.addIRIMapper(new SimpleIRIMapper( IRI.create( align.getOntology2URI() ), 
						      IRI.create( align.getFile2() ) ) );
	    manager.loadOntologyFromOntologyDocument( IRI.create( align.getFile1() ) );
	    manager.loadOntologyFromOntologyDocument( IRI.create( align.getFile2() ) );
	    OWLOntology ontology = manager.loadOntologyFromOntologyDocument( merged );
	    reasoner = new Reasoner( ontology );
	} catch (OWLOntologyCreationException ooce) {
	    ooce.printStackTrace(); 
	}
    }

    // In fact, it should be possible to do all EDOAL
    public OWLAxiom correspToAxiom( ObjectAlignment al, ObjectCell corresp ) throws AlignmentException {
	OWLDataFactory owlfactory = manager.getOWLDataFactory();

	LoadedOntology onto1 = al.ontology1();
	LoadedOntology onto2 = al.ontology2();
	// retrieve entity1 and entity2
	// create the axiom in function of their labels
	Object e1 = corresp.getObject1();
	Object e2 = corresp.getObject2();
	Relation r = corresp.getRelation();
	try {
	if ( onto1.isClass( e1 ) ) {
	    if ( onto2.isClass( e2 ) ) {
		OWLClass entity1 = owlfactory.getOWLClass( IRI.create( onto1.getEntityURI( e1 ) ) );
		OWLClass entity2 = owlfactory.getOWLClass( IRI.create( onto2.getEntityURI( e2 ) ) );
		if ( r instanceof EquivRelation ) {
		    return owlfactory.getOWLEquivalentClassesAxiom( entity1, entity2 );
		} else if ( r instanceof SubsumeRelation ) {
		    return owlfactory.getOWLSubClassOfAxiom( entity2, entity1 );
		} else if ( r instanceof SubsumedRelation ) {
		    return owlfactory.getOWLSubClassOfAxiom( entity1, entity2 );
		} else if ( r instanceof IncompatRelation ) {
		    return owlfactory.getOWLDisjointClassesAxiom( entity1, entity2 );
		}
	    } else if ( onto2.isIndividual( e2 ) && ( r instanceof HasInstanceRelation ) ) {
		return owlfactory.getOWLClassAssertionAxiom( owlfactory.getOWLClass( IRI.create( onto1.getEntityURI( e1 ) ) ),  
							     owlfactory.getOWLNamedIndividual( IRI.create( onto2.getEntityURI( e2 ) ) ) );
	    }
	} else if ( onto1.isDataProperty( e1 ) && onto2.isDataProperty( e2 ) ) {
		OWLDataProperty entity1 = owlfactory.getOWLDataProperty( IRI.create( onto1.getEntityURI( e1 ) ) );
		OWLDataProperty entity2 = owlfactory.getOWLDataProperty( IRI.create( onto2.getEntityURI( e2 ) ) );
		if ( r instanceof EquivRelation ) {
		    return owlfactory.getOWLEquivalentDataPropertiesAxiom( entity1, entity2 );
		} else if ( r instanceof SubsumeRelation ) {
		    return owlfactory.getOWLSubDataPropertyOfAxiom( entity2, entity1 );
		} else if ( r instanceof SubsumedRelation ) {
		    return owlfactory.getOWLSubDataPropertyOfAxiom( entity1, entity2 );
		} else if ( r instanceof IncompatRelation ) {
		    return owlfactory.getOWLDisjointDataPropertiesAxiom( entity1, entity2 );
		}
	} else if ( onto1.isObjectProperty( e1 ) && onto2.isObjectProperty( e2 ) ) {
		OWLObjectProperty entity1 = owlfactory.getOWLObjectProperty( IRI.create( onto1.getEntityURI( e1 ) ) );
		OWLObjectProperty entity2 = owlfactory.getOWLObjectProperty( IRI.create( onto2.getEntityURI( e2 ) ) );
		if ( r instanceof EquivRelation ) {
		    return owlfactory.getOWLEquivalentObjectPropertiesAxiom( entity1, entity2 );
		} else if ( r instanceof SubsumeRelation ) {
		    return owlfactory.getOWLSubObjectPropertyOfAxiom( entity2, entity1 );
		} else if ( r instanceof SubsumedRelation ) {
		    return owlfactory.getOWLSubObjectPropertyOfAxiom( entity1, entity2 );
		} else if ( r instanceof IncompatRelation ) {
		    return owlfactory.getOWLDisjointObjectPropertiesAxiom( entity1, entity2 );
		}
	} else if ( onto1.isIndividual( e1 ) ) {
	    if ( onto2.isIndividual( e2 ) ) {
		OWLIndividual entity1 = owlfactory.getOWLNamedIndividual( IRI.create( onto1.getEntityURI( e1 ) ) );
		OWLIndividual entity2 = owlfactory.getOWLNamedIndividual( IRI.create( onto2.getEntityURI( e2 ) ) );
		if ( r instanceof EquivRelation ) {
		    return owlfactory.getOWLSameIndividualAxiom( entity1, entity2 );
		} else if ( r instanceof IncompatRelation ) {
		    return owlfactory.getOWLDifferentIndividualsAxiom( entity1, entity2 );
		}
	    } else if ( onto2.isClass( e2 ) && ( r instanceof InstanceOfRelation ) ) {
		return owlfactory.getOWLClassAssertionAxiom( owlfactory.getOWLClass( IRI.create( onto2.getEntityURI( e2 ) ) ), 
							     owlfactory.getOWLNamedIndividual( IRI.create( onto1.getEntityURI( e1 ) ) ) );
	    }
	    }
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Error interpreting URI "+owex );
	}
	throw new AlignmentException( "Cannot convert correspondence "+corresp );
    }

    // This method can be suppressed
    public boolean isConsistent( OWLReasoner reasoner ) {
	return reasoner.isConsistent();
    }

    // This method can be suppressed
    public boolean isEntailed( OWLAxiom axiom ) {
	return reasoner.isEntailed( axiom );
    }

    //public void loadOntology( IDDLReasoner reasoner, Object onto ) {
    //	System.err.println( reasoner +" -- "+onto );
    //	Ontology oo = (Ontology)onto;
    //	URI f = oo.getFile();
    //	if ( f == null ) f = oo.getURI();
    //	reasoner.addOntology( f );
    //}

}

