/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2007
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

import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Set;
import java.net.URI;

import org.xml.sax.SAXException;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.util.OWLManager;
import org.semanticweb.owl.util.OWLConnection;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.io.owl_rdf.OWLRDFErrorHandler;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.Parameters;

/**
 * Represents an OWL ontology alignment. An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information about those
 * objects.
 *
 * @author Jérôme Euzenat
 * @version $Id$
 */

public class OWLAPIAlignment extends BasicAlignment {

    //protected OWLOntology onto1 = null;

    //protected OWLOntology onto2 = null;

    protected OWLAPIAlignment init = null;

    public OWLAPIAlignment() {
    }

    public void init(Object onto1, Object onto2) throws AlignmentException {
	init( onto1, onto2, (OntologyCache)null );
    }

    public void init(Object onto1, Object onto2, Object ontologies) throws AlignmentException {
	OntologyCache cache = null;
	if ( ontologies instanceof OntologyCache ) cache = (OntologyCache)ontologies;
	else cache = (OntologyCache)null;
	if ( onto1 instanceof OWLOntology && onto2 instanceof OWLOntology ){
	    super.init( onto1, onto2, ontologies );
	} else if ( onto1 instanceof URI && onto2 instanceof URI ) {
	    // JE: This contains the File and not the URI!
	    setFile1( (URI)onto1 );
	    setFile2( (URI)onto2 );
	    try {
		super.init( loadOntology( getFile1(), cache ),
			    loadOntology( getFile2(), cache ) );
	    } catch (OWLException e) {
		throw new AlignmentException( "Cannot load ontologies", e );
	    } catch (SAXException e) {
		throw new AlignmentException( "Cannot load ontologies", e );
	    }
	    // We should set the URI to that of the ontologies
	} else {
	    throw new AlignmentException("arguments must be OWLOntology or URI");
	};
    }

    public void loadInit( Alignment al ) throws AlignmentException {
	loadInit( al, (OntologyCache)null );
    }

    public void loadInit( Alignment al, OntologyCache ontologies ) throws AlignmentException {
	if ( al instanceof URIAlignment ) {
	    try { init = toOWLAPIAlignment( (URIAlignment)al, ontologies );
	    } catch (SAXException e) { e.printStackTrace(); 
	    } catch (OWLException e) { e.printStackTrace(); }
	} else if ( al instanceof OWLAPIAlignment ) {
	    init = (OWLAPIAlignment)al;
	}
    }

    public URI getOntology1URI() throws AlignmentException {
	try {
	    return ((OWLOntology)onto1).getLogicalURI();
	} catch ( OWLException e ) {
	    throw new AlignmentException( "URI conversion error for "+onto1, e );
	}
    };

    public URI getOntology2URI() throws AlignmentException {
	try {
	    return ((OWLOntology)onto2).getLogicalURI();
	} catch ( OWLException e ) {
	    throw new AlignmentException( "URI conversion error for "+onto2, e );
	}
    };

    public void setOntology1(Object ontology) throws AlignmentException {
	if ( ontology instanceof OWLOntology ){
	    super.setOntology1( ontology );
	} else {
	    throw new AlignmentException("arguments must be OWLOntology");
	};
    };

    public void setOntology2(Object ontology) throws AlignmentException {
	if ( ontology instanceof OWLOntology ){
	    super.setOntology2( ontology );
	} else {
	    throw new AlignmentException("arguments must be OWLOntology");
	};
    };

    /** Cell methods **/
    public Cell addAlignCell(String id, Object ob1, Object ob2, Relation relation, double measure) throws AlignmentException {
         if ( !( ob1 instanceof OWLEntity && ob2 instanceof OWLEntity ) )
            throw new AlignmentException("arguments must be OWLEntities");
	return super.addAlignCell( id, ob1, ob2, relation, measure);
	};
    public Cell addAlignCell(Object ob1, Object ob2, String relation, double measure) throws AlignmentException {
 
        if ( !( ob1 instanceof OWLEntity && ob2 instanceof OWLEntity ) )
            throw new AlignmentException("arguments must be OWLEntities");
	return super.addAlignCell( ob1, ob2, relation, measure);
    };
    public Cell addAlignCell(Object ob1, Object ob2) throws AlignmentException {
 
        if ( !( ob1 instanceof OWLEntity && ob2 instanceof OWLEntity ) )
            throw new AlignmentException("arguments must be OWLEntities");
	return super.addAlignCell( ob1, ob2 );
    };
    public Cell createCell(String id, Object ob1, Object ob2, Relation relation, double measure) throws AlignmentException {
	return (Cell)new OWLAPICell( id, (OWLEntity)ob1, (OWLEntity)ob2, relation, measure);
    }

    public Set getAlignCells1(Object ob) throws AlignmentException {
	if ( ob instanceof OWLEntity ){
	    return super.getAlignCells1( ob );
	} else {
	    throw new AlignmentException("argument must be OWLEntity");
	}
    }
    public Set getAlignCells2(Object ob) throws AlignmentException {
	if ( ob instanceof OWLEntity ){
	    return super.getAlignCells2( ob );
	} else {
	    throw new AlignmentException("argument must be OWLEntity");
	}
    }

    // Deprecated: implement as the one retrieving the highest strength correspondence (
    public Cell getAlignCell1(Object ob) throws AlignmentException {
	if ( BasicAlignment.STRICT_IMPLEMENTATION == true ){
	    throw new AlignmentException("deprecated (use getAlignCells1 instead)");
	} else {
	    if ( ob instanceof OWLEntity ){
		return super.getAlignCell1( ob );
	    } else {
		throw new AlignmentException("argument must be OWLEntity");
	    }
	}
    }

    public Cell getAlignCell2(Object ob) throws AlignmentException {
	if ( BasicAlignment.STRICT_IMPLEMENTATION == true ){
	    throw new AlignmentException("deprecated (use getAlignCells2 instead)");
	} else {
	    if ( ob instanceof OWLEntity ){
		return super.getAlignCell2( ob );
	    } else {
		throw new AlignmentException("argument must be OWLEntity");
	    }
	}
    }

    /**
     * Generate a copy of this alignment object
     */
    // JE: this is a mere copy of the method in BasicAlignement
    // It has two difficulties
    // - it should call the current init() and not that of BasicAlignement
    // - it should catch the AlignmentException that it is supposed to raise
    public Object clone() {
	OWLAPIAlignment align = new OWLAPIAlignment();
	try {
	    align.init( (OWLOntology)getOntology1(), (OWLOntology)getOntology2() );
	} catch ( AlignmentException e ) {};
	align.setType( getType() );
	align.setLevel( getLevel() );
	align.setFile1( getFile1() );
	align.setFile2( getFile2() );
	for ( Enumeration e = extensions.getNames() ; e.hasMoreElements(); ){
	    String label = (String)e.nextElement();
	    align.setExtension( label, getExtension( label ) );
	}
	align.getExtensions().unsetParameter( "id" );
	try {
	    align.ingest( this );
	} catch (AlignmentException ex) { ex.printStackTrace(); }
	return align;
    }

    /**
     * This is a clone with the URI instead of OWLAPI objects
     *
     */
    public URIAlignment toURIAlignment() throws AlignmentException {
	URIAlignment align = new URIAlignment();
	align.init( getOntology1URI(), getOntology2URI() );
	align.setType( getType() );
	align.setLevel( getLevel() );
	align.setFile1( getFile1() );
	align.setFile2( getFile2() );
	for ( Enumeration e = extensions.getNames() ; e.hasMoreElements(); ){
	    String label = (String)e.nextElement();
	    align.setExtension( label, getExtension( label ) );
	}
	for (Enumeration e = getElements(); e.hasMoreElements();) {
	    Cell c = (Cell)e.nextElement();
	    try {
		align.addAlignCell( c.getId(), c.getObject1AsURI(), c.getObject2AsURI(), c.getRelation(), c.getStrength() );
	    } catch (AlignmentException aex) {
		// Sometimes URIs are null, this is ignore
	    }
	};
	return align;
    }

    // Here it becomes necessary to load OWL
    static public OWLAPIAlignment toOWLAPIAlignment( URIAlignment al, OntologyCache ontologies ) throws AlignmentException, SAXException, OWLException {
	OWLAPIAlignment alignment = new OWLAPIAlignment();
	//alignment.setFile1( al.getFile1() );
	//alignment.setFile2( al.getFile2() );
	alignment.init( al.getFile1(), al.getFile2(), ontologies );
	alignment.setType( al.getType() );
	alignment.setLevel( al.getLevel() );
	for ( Enumeration e = al.getExtensions().getNames() ; e.hasMoreElements(); ){
	    String label = (String)e.nextElement();
	    alignment.setExtension( label, al.getExtension( label ) );
	}
	OWLOntology onto1 = (OWLOntology)alignment.getOntology1();
	OWLOntology onto2 = (OWLOntology)alignment.getOntology2();
	for (Enumeration e = al.getElements(); e.hasMoreElements();) {
	    Cell c = (Cell)e.nextElement();
	    alignment.addAlignCell( c.getId(), 
				    getEntity( onto1, c.getObject1AsURI() ),
				    getEntity( onto2, c.getObject2AsURI() ),
				    c.getRelation(), 
				    c.getStrength() );
	};
	return alignment;
    }

    private static OWLEntity getEntity( OWLOntology ontology, URI uri ) throws OWLException, SAXException {
	OWLEntity result = (OWLEntity)ontology.getClass( uri );
	if ( result == null ) result = (OWLEntity)ontology.getDataProperty( uri );
	if ( result == null ) result = (OWLEntity)ontology.getObjectProperty( uri );
	if ( result == null ) result = (OWLEntity)ontology.getIndividual( uri );
	return result;
    }

    /** Can be used for loading the ontology if it is not available **/
    //private static OWLOntology loadOntology( URI ref, Hashtable ontologies ) throws SAXException, OWLException {
    private static OWLOntology loadOntology( URI ref, OntologyCache ontologies ) throws SAXException, OWLException {
	if ( (ontologies != null) && ( ontologies.getOntology( ref ) != null ) ) {
	    return ontologies.getOntology( ref );
	} else {
	    OWLOntology parsedOnt = null;
	    OWLRDFParser parser = new OWLRDFParser();
	    OWLRDFErrorHandler handler = new OWLRDFErrorHandler(){
		    public void owlFullConstruct( int code, String message ) 
			throws SAXException {
		    }
		    public void owlFullConstruct(int code, String message, Object o)
			throws SAXException {
		    }
		    public void error( String message ) throws SAXException {
			throw new SAXException( message.toString() );
		    }
		    public void warning( String message ) throws SAXException {
			System.err.println("WARNING: " + message);
		    }
		};
	    parser.setOWLRDFErrorHandler( handler );
	    parser.setConnection( OWLManager.getOWLConnection() );
	    parsedOnt = parser.parseOntology( ref );
	    if ( ontologies != null )
		ontologies.recordOntology( ref, parsedOnt );
	    //    ontologies.put( ref.toString(), parsedOnt );
	    return parsedOnt;
	}
    }

}

