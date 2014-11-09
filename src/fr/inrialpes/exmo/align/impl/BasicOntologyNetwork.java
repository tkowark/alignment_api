/*
 * $Id$
 *
 * Copyright (C) INRIA, 2009-2010, 2014
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

import java.lang.Iterable;
import java.util.Collections;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Properties;
import java.net.URI;
import java.net.URISyntaxException;
import java.lang.reflect.Constructor;

import java.io.PrintWriter;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.RDF;

//import fr.inrialpes.exmo.ontowrap.Ontology;

import fr.inrialpes.exmo.align.parser.SyntaxElement;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.parser.RDFParser;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.Extensible;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.OntologyNetwork;

/**
 * Represents a distributed system of aligned ontologies or network of ontologies.
 *
 */

public class BasicOntologyNetwork implements OntologyNetwork, Extensible {
    final static Logger logger = LoggerFactory.getLogger( BasicOntologyNetwork.class );

    protected Hashtable<URI,OntologyTriple> ontologies;
    protected HashSet<Alignment> alignments;

    protected Extensions extensions = null;
    
    protected HashMap<URI,Map<URI,Set<Alignment>>> onto2Align;

    public BasicOntologyNetwork(){
	extensions = new Extensions();
	ontologies = new Hashtable<URI,OntologyTriple>();
	alignments = new HashSet<Alignment>();
	onto2Align = new HashMap<URI,Map<URI,Set<Alignment>>>();
    }

    public void addOntology( URI onto ){
	if ( ontologies.get( onto ) == null )
	    ontologies.put( onto, new OntologyTriple( onto ) );
    };
    public void remOntology( URI onto ) throws AlignmentException {
	OntologyTriple ot = ontologies.get( onto );
	if ( ot != null ) {
	    for( Alignment al : ot.sourceAlignments ){
		remAlignment( al );
	    }
	    for( Alignment al : ot.targettingAlignments ){
		remAlignment( al );
	    }
	    ontologies.remove( onto ); // Or set to null
	    
	    onto2Align.remove(onto);
	    for (Map<URI,Set<Alignment>> m : onto2Align.values())
		m.remove(onto);  
	}
    };
    public void addAlignment( Alignment al ) throws AlignmentException {
	URI o1 = al.getOntology1URI();
	addOntology( o1 );
	ontologies.get( o1 ).sourceAlignments.add( al );
	URI o2 = al.getOntology2URI();
	addOntology( o2 );
	ontologies.get( o2 ).targettingAlignments.add( al );
	alignments.add( al );
	
	Map<URI,Set<Alignment>> m = onto2Align.get(al.getOntology1URI());
	if (m==null) {
	    m=new HashMap<URI,Set<Alignment>>();
	    onto2Align.put(al.getOntology1URI(), m);
	}
	Set<Alignment> aligns=m.get(al.getOntology2URI());
	if (aligns==null) {
	    aligns = new HashSet<Alignment>();
	    m.put(al.getOntology2URI(), aligns);
	}
	aligns.add(al);
    }; 
    public void remAlignment( Alignment al ) throws AlignmentException {
	ontologies.get( al.getOntology1URI() ).sourceAlignments.remove( al );
	ontologies.get( al.getOntology2URI() ).targettingAlignments.remove( al );
	alignments.remove( al );
	onto2Align.get(al.getOntology1URI()).get(al.getOntology2URI()).remove(al);
    };
    public Set<Alignment> getAlignments(){
	return alignments;
    };
    public Set<URI> getOntologies() {
	return ontologies.keySet(); // ??
    };
    public Set<Alignment> getTargetingAlignments( URI onto ){
	OntologyTriple ot = ontologies.get( onto );
	if ( ot == null ) return Collections.emptySet();
	return ot.targettingAlignments;
    }
    public Set<Alignment> getSourceAlignments( URI onto ){
	OntologyTriple ot = ontologies.get( onto );
	if ( ot == null ) return Collections.emptySet();
	return ot.sourceAlignments;
    }
    /*
      The one below is more efficient (as soon as the structure is maintained)
      Thus structure is not necessary... it should have been put rather in ontoTriple!
    public Set<Alignment> getAlignments( URI onto1, URI onto2 ){
	OntologyTriple ot1 = ontologies.get( onto1 );
	OntologyTriple ot2 = ontologies.get( onto2 );
	if ( ot1 == null || ot2 == null ) return Collections.emptySet();
	return intersectAlignments( ot1.sourceAlignments, ot2.targettingAlignments );
    }
    */
    public Set<Alignment> getAlignments(URI srcOnto, URI dstOnto) {
	Map<URI,Set<Alignment>> m = onto2Align.get(srcOnto);
	if (m!=null) {
	    Set<Alignment> aligns = m.get(dstOnto);
	    if (aligns!=null) return Collections.unmodifiableSet(aligns);
	}
	return Collections.emptySet();
    }


    /**
     * Clone does some deeper cloning
     * It has the same content but a different id (no id indeed)
     */
    public BasicOntologyNetwork clone() {
	//public Object clone() {
	BasicOntologyNetwork network = new BasicOntologyNetwork();
	network.setExtensions( extensions.convertExtension( "cloned", this.getClass().getName()+"#clone" ) );
	for ( URI onto : ontologies.keySet() ) network.addOntology( onto );
	for ( Alignment al : alignments ) {
	    try { 
		network.addAlignment( al );
	    } catch (AlignmentException alex) {
		logger.debug( "IGNORED Exception : should not happen {}", al );
	    }
	}
	return network;
    }

    /**
     * Invert all alignments in a network of ontologies
     * In fact, this is closing by invert!
     * Modifies the network
     */
    public void invert() throws AlignmentException {
	Vector<Alignment> newal = new Vector<Alignment>();
	for ( Alignment al : alignments ) newal.add( al.inverse() );
	for ( Alignment al : newal ) addAlignment( al );
    }

    /**
     * Denormalizes an ontology network: suppress all empty alignments
     * Modifies the network
     */
    public void denormalize() throws AlignmentException {
	Vector<Alignment> toremove = new Vector<Alignment>();
	for ( Alignment al : alignments ) {
	    if ( al.nbCells() == 0 ) toremove.add( al );
	}
	for ( Alignment al : toremove ) remAlignment( al );
    }

    /**
     * Normalizes an ontology network for it to have exactly one alignment between each pair of ontologies.
     * Modifies the network
     */
    public void normalize() throws AlignmentException {
	for ( OntologyTriple ot1 : ontologies.values() ) {
	    for ( OntologyTriple ot2 : ontologies.values() ) {
		Set<Alignment> als = intersectAlignments( ot1.sourceAlignments, ot2.targettingAlignments );
		// Suppress them
		if ( als.size() > 1 ) {
		    Alignment norm = normalizeAlignmentSet( als, ot1.onto, ot2.onto );
		    for ( Alignment al : als ) {
			remAlignment( al );
		    }
		    // Add new
		    addAlignment( norm );
		}
	    }
	}
    }

    /**
     * Computes the transitive, reflexive, symmetric closure of a network of ontology
     * TODO.
     * Reflexive: for each ontology generate id alignment + merge it with others
     * Symmetric: for each pair of ontologies
     * Transitive: for each triple of ontologies
     */
    public void close( boolean symmetric, boolean transitive, boolean reflexive, Properties params ) throws AlignmentException {
	for ( OntologyTriple ot1 : ontologies.values() ) {
	    if ( reflexive ) {
		// generate id alignment
		// + merge it with others
	    }
	    for ( OntologyTriple ot2 : ontologies.values() ) {
		if ( symmetric ) {
		    // merge ot1-ot2 with invert( ot2-ot1)
		    // eventually replace ot2-ot1 with inverse ot1-ot2
		}
		if ( transitive ) {
		    for ( OntologyTriple ot3 : ontologies.values() ) {
			// merge ot1-ot3 with compose( ot1-ot2, ot2-ot3 )
		    }
		}
		// EXCEPT THAT FOR TRANSITIVE, THIS MUST BE REITERATED UNTIL EXHAUSTION (BETTER WAY TO DO WITH A QUEUE)

		// ...THIS IS THE BASE STEP...
		// This is the normalisation step
		Set<Alignment> als = intersectAlignments( ot1.sourceAlignments, ot2.targettingAlignments );
		// Suppress them
		if ( als.size() > 1 ) {
		    Alignment norm = normalizeAlignmentSet( als, ot1.onto, ot2.onto );
		    for ( Alignment al : als ) {
			remAlignment( al );
		    }
		    // Add new
		    addAlignment( norm );
		}
	    }
	}
    }

    protected static Alignment normalizeAlignmentSet( Set<Alignment> als, URI onto1, URI onto2 ) throws AlignmentException {
	Alignment result = null;
	if ( als.size() == 0 ) { // If no element, create new
	    result = new BasicAlignment();
	    result.init( onto1, onto2 );
	} else if ( als.size() > 1 ) { // If more than one element, merge them
	    result = null;
	    for ( Alignment al : als ) {
		if ( result == null ) {
		    result = al;
		} else {
		    result.join( al );
		}
	    }
	} else {
	    result = als.iterator().next();
	}
	return result;
    }

    protected static Set<Alignment> intersectAlignments( Set<Alignment> s1, Set<Alignment> s2 ) {
	Set<Alignment> result = new HashSet<Alignment>();
	for ( Alignment x : s1 ) {
	    if ( s2.contains( x ) ) result.add( x );
	}
	return result;
    }

    /**
     * Match ontologies in a network, using existing ontologies as input...
     * Modifies the network
     * TODO
     * Alternative definition!
     * public void match( Class<? extends AlignmentProcess> method, boolean reflexive ) throws AlignmentException {
    }
     */
    public void match( String method, boolean reflexive, boolean symmetric, Properties params ) throws AlignmentException {
	for ( OntologyTriple ot1 : ontologies.values() ) {
	    for ( OntologyTriple ot2 : ontologies.values() ) {
		if ( ot1 != ot2 || reflexive ) {
		    Set<Alignment> als = intersectAlignments( ot1.sourceAlignments, ot2.targettingAlignments );
		    Alignment init = normalizeAlignmentSet( als, ot1.onto, ot2.onto );
		    for ( Alignment al : als ) {
			remAlignment( al );
		    }
		    // Create the alignment process
		    // JE: reentrance would be useful
		    // Async is not honoured (but not here)
		    AlignmentProcess ap = null;
		    try {
			// Create alignment object
			Class<?> alignmentClass = Class.forName( method );
			Class[] cparams = {};
			Constructor alignmentConstructor = alignmentClass.getConstructor(cparams);
			Object[] mparams = {};
			ap = (AlignmentProcess)alignmentConstructor.newInstance( mparams );
			ap.init( ot1.onto, ot2.onto );
		    } catch ( Exception ex ) {
			logger.error( "Cannot create alignment {}", method );
			throw new AlignmentException( "Cannot create alignment "+method, ex );
		    }
		    // Compute alignment
		    long time = System.currentTimeMillis();
		    ap.align( init, params ); // or params?
		    long newTime = System.currentTimeMillis();
		    ap.setExtension( Namespace.EXT.uri, Annotations.TIME, Long.toString(newTime - time) );
		    // replace
		    addAlignment( ap );
		}
		if ( ot1 == ot2 && !symmetric ) break;
	    }
	}
    }

    /**
     * Applies a threshold to all alignments in a network
     * Modifies the network
     */
    public void trim( String method, double threshold ) throws AlignmentException {
	for ( Alignment al : alignments ) {
	    al.cut( method, threshold );
	}
    }

    /**
     * Intersection of two ontology networks...
     * Creates a new network
     * TODO: add extensions? i.e., partial clone as BasicAlignment
     */
    public static BasicOntologyNetwork meet( OntologyNetwork on1, OntologyNetwork on2 ) throws AlignmentException {
	BasicOntologyNetwork result = new BasicOntologyNetwork();
	Set<URI> ontologies2ndNetwork = on2.getOntologies();
	// intersect these...
	for ( URI onto1 : on1.getOntologies() ) {
	    if ( ontologies2ndNetwork.contains( onto1 ) ) {
		result.addOntology( onto1 );
	    }
	}
	// go to the networks now
	for ( URI onto1 : result.getOntologies() ) {
	    for ( URI onto2 : result.getOntologies() ) {
		// Get the alignments from both networks
		Set<Alignment> als1 = on1.getAlignments( onto1, onto2 );
		Set<Alignment> als2 = on2.getAlignments( onto1, onto2 );
		// Normalise them (yes because it is join)
		Alignment init1 = normalizeAlignmentSet( als1, onto1, onto2 );
		Alignment init2 = normalizeAlignmentSet( als2, onto1, onto2 );
		// Meet them
		result.addAlignment( init1.meet( init2 ) );
	    }
	}
	return result;
    }

    /**
     * Union of two ontology networks...
     * Creates a new network
     * TODO: same as before
     */
    public static BasicOntologyNetwork join( OntologyNetwork on1, OntologyNetwork on2 ) throws AlignmentException {
	return (BasicOntologyNetwork)null;
    }

    /**
     * Difference between two ontology networks...
     * Creates a new network
     * TODO: same as before
     */
    public static BasicOntologyNetwork diff( OntologyNetwork on1, OntologyNetwork on2 ) throws AlignmentException {
	return (BasicOntologyNetwork)null;
    }

    public Collection<String[]> getExtensions(){ return extensions.getValues(); }

    public void setExtensions( Extensions ext ){ extensions = ext; }

    public void setExtension( String uri, String label, String value ) {
	extensions.setExtension( uri, label, value );
    };

    public String getExtension( String uri, String label ) {
	return extensions.getExtension( uri, label );
    };

    /**
     * Printing
     * here we do not use renderers; may be later
     */

    protected String NL = System.getProperty("line.separator");
    protected String INDENT = "  ";
    private static Namespace DEF = Namespace.ALIGNMENT;
    public void setIndentString( String ind ) { INDENT = ind; }	
    public void setNewLineString( String nl ) { NL = nl; }	

    public void write( PrintWriter writer ) throws AlignmentException {
	writer.print("<?xml version='1.0' encoding='utf-8");
	writer.print("' standalone='no'?>"+NL+NL);
	writer.print("<"+SyntaxElement.RDF.print(DEF)+" xmlns='"+Namespace.ALIGNMENT.prefix+"'");
	writer.print(NL+INDENT+INDENT+" xml:base='"+Namespace.ALIGNMENT.prefix+"'");
	writer.print(NL+INDENT+INDENT+" xmlns:"+Namespace.RDF.shortCut+"='"+Namespace.RDF.prefix+"'");
	writer.print(NL+INDENT+INDENT+" xmlns:"+Namespace.ALIGNMENT.shortCut+"='"+Namespace.ALIGNMENT.prefix+"'");
	writer.print(NL+INDENT+INDENT+" xmlns:"+Namespace.EXT.shortCut+"='"+Namespace.EXT.prefix+"'");
	writer.print(NL+INDENT+INDENT+" xmlns:"+Namespace.XSD.shortCut+"='"+Namespace.XSD.prefix+"'");
	String idext = getExtension( Namespace.ALIGNMENT.uri, Annotations.ID );
	writer.print(">"+NL+INDENT+"<"+SyntaxElement.ONTOLOGYNETWORK.print(DEF));
	if ( idext != null ) {
	    writer.print(" "+SyntaxElement.RDF_ABOUT.print(DEF)+"='"+idext+"'");
	}
	writer.print(">"+NL);
        for ( String[] ext : getExtensions() ) {
            String prefix = ext[0];
            String name = ext[1];
	    String tag;
	    if ( prefix.equals( Namespace.EXT.prefix ) ) {
                tag = Namespace.EXT.shortCut+":"+name;
            } else {
                tag = prefix+"#"+name;
            }
	    writer.print(INDENT+"<"+tag+">"+ext[2]+"</"+tag+">"+NL);
	}
	for( URI u : ontologies.keySet() ) {
	    writer.print(INDENT+INDENT+"<"+SyntaxElement.ONONTOLOGY.print(DEF)+" rdf:resource=\'"+u+"'/>"+NL);
	}
	writer.print(NL);
	for( Alignment al : alignments ) {
	    String aluri = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID );
	    if ( aluri != null ) {
		writer.print(INDENT+INDENT+"<"+SyntaxElement.ONALIGNMENT.print(DEF)+" rdf:resource='"+aluri+"'/>"+NL);
	    } else {
		throw new AlignmentException( "Cannot print alignment without URI : "+al );
	    }
	}
	writer.print(INDENT+"</"+SyntaxElement.ONTOLOGYNETWORK.print(DEF)+">"+NL);
	writer.print("</"+SyntaxElement.RDF.print(DEF)+">"+NL);
    }

    /**
     * Parsing
     * here we use Jena
     */

    // Certainly not only URIs for testing reasons
    public static OntologyNetwork read( String uri ) throws AlignmentException {
	// Initialize the syntax description (could be restricted)
	Model rdfModel = ModelFactory.createDefaultModel();
	for ( SyntaxElement el : SyntaxElement.values() ) {
	    if ( el.isProperty == true ) {
		el.resource = rdfModel.createProperty( el.id() );
	    } else {
		el.resource = rdfModel.createResource( el.id() );
	    }
	}
	// Parse with JENA
	rdfModel.read( uri );
	// Collect data
	return parse( rdfModel );
    }

    public static OntologyNetwork read( InputStream is ) throws AlignmentException {
	if ( is == null ) throw new AlignmentException("The inputstream must not be null");
	// Initialize the syntax description (could be restricted)
	Model rdfModel = ModelFactory.createDefaultModel();
	for ( SyntaxElement el : SyntaxElement.values() ) {
	    if ( el.isProperty == true ) {
		el.resource = rdfModel.createProperty( el.id() );
	    } else {
		el.resource = rdfModel.createResource( el.id() );
	    }
	}
	// Parse with JENA
	rdfModel.read( is, null );
	// Collect data
	return parse( rdfModel );
    }

    public static OntologyNetwork parse( final Model rdfModel ) throws AlignmentException {
	BasicOntologyNetwork on = null;
	// Get the statement including alignment resource as rdf:type
	StmtIterator stmtIt = rdfModel.listStatements(null, RDF.type,(Resource)SyntaxElement.getResource("OntologyNetwork"));
	// Take the first one if it exists
	if ( !stmtIt.hasNext() ) throw new AlignmentException("There is no ontology network in the RDF document");
	Statement node = stmtIt.nextStatement();
	if (node == null) throw new NullPointerException("OntologyNetwork must not be null");
	Resource res = node.getSubject();

	try {
	    on = new BasicOntologyNetwork();
	    // getting the id of the document
	    final String id = res.getURI();
	    if ( id != null ) on.setExtension( Namespace.ALIGNMENT.uri, Annotations.ID, id );
	    
	    stmtIt = res.listProperties((Property)SyntaxElement.ONONTOLOGY.resource );
	    while ( stmtIt.hasNext() ) {
		RDFNode onto = stmtIt.nextStatement().getObject();
		if ( onto.isURIResource() ) {
		    on.addOntology( new URI( onto.asResource().getURI() ) );
		} else {
		    throw new AlignmentException( "Ontologies must be identified by URIs" );
		}
	    }
	    AlignmentParser aparser = new AlignmentParser();
	    stmtIt = res.listProperties((Property)SyntaxElement.ONALIGNMENT.resource );
	    while ( stmtIt.hasNext() ) {
		RDFNode al = stmtIt.nextStatement().getObject();
		if ( al.isURIResource() ) {
		    String aluri = al.asResource().getURI();
		    Alignment alobject = aparser.parse( aluri );
		    if ( alobject.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID ) == null ) {
			alobject.setExtension( Namespace.ALIGNMENT.uri, Annotations.ID, aluri );
		    }
		    on.addAlignment( alobject );
		} else {
		    logger.debug( "IGNORED Exception : Alignments must be identified by URIs" );
		}
	    }
	    RDFParser.parseExtensions( res, on );
	} catch ( AlignmentException alex ) {
	    throw alex;
	} catch ( URISyntaxException urisex ) {
	    throw new AlignmentException("There is some error in parsing alignment: " + res.getLocalName(), urisex);
	} finally { // Clean up memory
	    rdfModel.close();
	}
	return on;
    }
}

class OntologyTriple {

    public URI onto;
    public HashSet<Alignment> targettingAlignments;
    public HashSet<Alignment> sourceAlignments;

    OntologyTriple( URI o ){
	onto = o;
	targettingAlignments = new HashSet<Alignment>();
	sourceAlignments = new HashSet<Alignment>();
    }
}



