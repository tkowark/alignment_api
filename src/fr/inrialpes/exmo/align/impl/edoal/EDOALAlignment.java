/*
 * $Id$
 *
 * Sourceforge version 1.6 - 2008 - was OMWGAlignment
 * Copyright (C) INRIA, 2007-2013
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

package fr.inrialpes.exmo.align.impl.edoal;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Collection;
import java.util.Set;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.Extensions;

import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor;

/**
 * <p>This class is an encapsulation of BasicAlignement so that
 * it creates the structures required by the MappingDocument within
 * the BasicAlignment</p>
 * JE 2009: Maybe ObjectAlignment could even be better
 * 
 */
public class EDOALAlignment extends BasicAlignment {
    final static Logger logger = LoggerFactory.getLogger( EDOALAlignment.class );

    /*
     * An eventual initial alignment
     *
     */
    protected EDOALAlignment init = null;

    /*
     * The list of variables declared in this alignment
     * //EDOALPattern
     */
    protected Hashtable<String,Variable> variables;

    public EDOALAlignment() {
	setLevel("2EDOAL");
	setXNamespace( Namespace.EDOAL.shortCut, Namespace.EDOAL.prefix );
	variables = new Hashtable<String,Variable>();
    }

    public void accept( TypeCheckingVisitor visitor ) throws AlignmentException {
	visitor.visit(this);
    }

    public void init( Object onto1, Object onto2 ) throws AlignmentException {
    	if ( (onto1 == null) || (onto2 == null) )
	    throw new AlignmentException("The source and target ontologies must not be null");
	Ontology o1 = null;
	if ( onto1 instanceof Ontology ) o1 = (Ontology)onto1;
	else o1 = loadOntology( (URI)onto1 );
	Ontology o2 = null;
	if ( onto2 instanceof Ontology ) o2 = (Ontology)onto2;
	else o2 = loadOntology( (URI)onto2 );
	super.init( o1, o2 );
    }

    public void loadInit( Alignment al ) throws AlignmentException {
	if ( al instanceof EDOALAlignment ) {
	    init = (EDOALAlignment)al;
	} else {
	    throw new AlignmentException( "EDOAL required as initial alignment");
	}
    }

    /*
     * Dealing with variables
     */

    public Variable recordVariable( String name, Expression expr ) {
	Variable var = variables.get( name );
	if ( var == null ) {
	    var = new Variable( name );
	    variables.put( name, var );
	}
	var.addOccurence( expr );
	return var;
    }

    /*
     * Dealing with correspondences
     */

    /** Cell methods **/
    public Cell addAlignCell( EDOALCell rule ) throws AlignmentException {
	addCell( rule );
	return rule;
    }

    public Cell addAlignCell(String id, Object ob1, Object ob2, Relation relation, double measure, Extensions extensions ) throws AlignmentException {
         if ( !( ob1 instanceof Expression && ob2 instanceof Expression ) )
            throw new AlignmentException("arguments must be Expressions");
	 return super.addAlignCell( id, ob1, ob2, relation, measure, extensions);
	};
    public Cell addAlignCell(String id, Object ob1, Object ob2, Relation relation, double measure) throws AlignmentException {
         if ( !( ob1 instanceof Expression && ob2 instanceof Expression ) )
            throw new AlignmentException("arguments must be Expressions");
	return super.addAlignCell( id, ob1, ob2, relation, measure);
	};
    public Cell addAlignCell(Object ob1, Object ob2, String relation, double measure) throws AlignmentException {
 
        if ( !( ob1 instanceof Expression && ob2 instanceof Expression ) )
            throw new AlignmentException("arguments must be Expressions");
	return super.addAlignCell( ob1, ob2, relation, measure);
    };
    public Cell addAlignCell(Object ob1, Object ob2) throws AlignmentException {
 
        if ( !( ob1 instanceof Expression && ob2 instanceof Expression ) )
            throw new AlignmentException("arguments must be Expressions");
	return super.addAlignCell( ob1, ob2 );
    };
    public Cell createCell(String id, Object ob1, Object ob2, Relation relation, double measure) throws AlignmentException {
	return (Cell)new EDOALCell( id, (Expression)ob1, (Expression)ob2, relation, measure);
    }

    public Set<Cell> getAlignCells1(Object ob) throws AlignmentException {
	if ( ob instanceof Expression ){
	    return super.getAlignCells1( ob );
	} else {
	    throw new AlignmentException("argument must be Expression");
	}
    }
    public Set<Cell> getAlignCells2(Object ob) throws AlignmentException {
	if ( ob instanceof Expression ){
	    return super.getAlignCells2( ob );
	} else {
	    throw new AlignmentException("argument must be Expression");
	}
    }

    // Deprecated: implement as the one retrieving the highest strength correspondence (
    public Cell getAlignCell1(Object ob) throws AlignmentException {
	if ( Annotations.STRICT_IMPLEMENTATION == true ){
	    throw new AlignmentException("deprecated (use getAlignCells1 instead)");
	} else {
	    if ( ob instanceof Expression ){
		return super.getAlignCell1( ob );
	    } else {
		throw new AlignmentException("argument must be Expression");
	    }
	}
    }

    public Cell getAlignCell2(Object ob) throws AlignmentException {
	if ( Annotations.STRICT_IMPLEMENTATION == true ){
	    throw new AlignmentException("deprecated (use getAlignCells2 instead)");
	} else {
	    if ( ob instanceof Expression ){
		return super.getAlignCell2( ob );
	    } else {
		throw new AlignmentException("argument must be Expression");
	    }
	}
    }

    /*
     * Dealing with ontology Ids from an Alignment API standpoint
     */
    public URI getOntology1URI() { return onto1.getURI(); };

    public URI getOntology2URI() { return onto2.getURI(); };

    public void setOntology1(Object ontology) throws AlignmentException {
	if ( ontology instanceof Ontology ){
	    super.setOntology1( ontology );
	} else {
	    throw new AlignmentException("arguments must be Ontology");
	};
    };

    public void setOntology2(Object ontology) throws AlignmentException {
	if ( ontology instanceof Ontology ){
	    super.setOntology2( ontology );
	} else {
	    throw new AlignmentException("arguments must be Ontology");
	};
    };

    /**
     * This is a clone with the URI instead of Object objects
     * This conversion will drop any correspondences using something not identified by an URI
     * For converting to ObjectAlignment, first convert to URIAlignment and load as an ObjectAlignment
     * The same code as for ObjectAlignment works...
     */
    public URIAlignment toURIAlignment() throws AlignmentException {
	return toURIAlignment( false );
    }
    public URIAlignment toURIAlignment( boolean strict ) throws AlignmentException {
	URIAlignment align = new URIAlignment();
	align.init( getOntology1URI(), getOntology2URI() );
	align.setType( getType() );
	align.setLevel( getLevel() );
	align.setFile1( getFile1() );
	align.setFile2( getFile2() );
	align.setExtensions( convertExtension( "EDOALURIConverted", "http://exmo.inrialpes.fr/align/impl/edoal/EDOALAlignment#toURI" ) );
	for (Enumeration e = getElements(); e.hasMoreElements();) {
	    Cell c = (Cell)e.nextElement();
	    try {
		align.addAlignCell( c.getId(), c.getObject1AsURI(this), c.getObject2AsURI(this), c.getRelation(), c.getStrength() );
	    } catch (AlignmentException aex) {
		// Ignore every cell that does not work
		if ( strict ) {
		    throw new AlignmentException( "Cannot convert to URIAlignment" );
		}
	    }
	};
	return align;
    }

    /**
     * convert an URI alignment into a corresponding EDOALAlignment
     * The same could be implemented for ObjectAlignent if necessary
     */
    // This works for any BasicAlignment but will return an empty
    // alignment if they do not have proper URI dereferencing.
    // It would be better to implement this for ObjectAlignment
    // and to use return toEDOALAlignment( ObjectAlignment.toObjectAlignment( al ) );
    // for URIAlignment
    //
    // Basic -> URI
    //       -> Object
    //       -> EDOAL
    public static LoadedOntology loadOntology( URI onto ) throws AlignmentException {
	if ( onto == null ) throw new AlignmentException("The source and target ontologies must not be null");
	try {
	    OntologyFactory fact = OntologyFactory.getFactory();
	    return fact.loadOntology( onto );
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Cannot load ontologies", owex );
	}
    }
    public static LoadedOntology loadOntology( Ontology onto ) throws AlignmentException {
	if ( onto == null ) throw new AlignmentException("The source and target ontologies must not be null");
	if ( onto instanceof LoadedOntology ) return (LoadedOntology)onto;
	try {
	    OntologyFactory fact = OntologyFactory.getFactory();
	    return fact.loadOntology( onto );
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Cannot load ontologies", owex );
	}
    }

    public static EDOALAlignment toEDOALAlignment( URIAlignment al ) throws AlignmentException {
	return toEDOALAlignment( (BasicAlignment)al );
    }
    public static EDOALAlignment toEDOALAlignment( ObjectAlignment al ) throws AlignmentException {
	logger.debug( "Converting ObjectAlignment to EDOALAlignment" );
	EDOALAlignment alignment = new EDOALAlignment();
	// They are obviously loaded
	alignment.init( al.getOntologyObject1(), al.getOntologyObject2() );
	alignment.convertToEDOAL( al );
	return alignment;
    }
    public static EDOALAlignment toEDOALAlignment( BasicAlignment al ) throws AlignmentException {
	logger.debug( "Converting BasicAlignment to EDOALAlignment" );
	EDOALAlignment alignment = new EDOALAlignment();
	LoadedOntology onto1 = loadOntology( al.getOntologyObject1() );
	LoadedOntology onto2 = loadOntology( al.getOntologyObject2() );
	alignment.init( onto1, onto2 );
	alignment.convertToEDOAL( al );
	return alignment;
    }
    /**
     * The EDOALAlignment has LoadedOntologies as ontologies
     */
    public void convertToEDOAL( BasicAlignment al ) throws AlignmentException {
	setType( al.getType() );
	setExtensions( al.convertExtension( "toEDOAL", "fr.inrialpes.exmo.align.edoal.EDOALAlignment#toEDOAL" ) );
	LoadedOntology<Object> o1 = (LoadedOntology<Object>)getOntologyObject1(); // [W:unchecked]
	LoadedOntology<Object> o2 = (LoadedOntology<Object>)getOntologyObject2(); // [W:unchecked]
	for ( Cell c : al ) {
	    try {
		Cell newc = addAlignCell( c.getId(), 
			       createEDOALExpression( o1, c.getObject1AsURI( al ) ),
			       createEDOALExpression( o2, c.getObject2AsURI( al ) ),
			       c.getRelation(), 
			       c.getStrength() );
		Collection<String[]> exts = c.getExtensions();
		if ( exts != null ) {
		    for ( String[] ext : exts ){
			newc.setExtension( ext[0], ext[1], ext[2] );
		    }
		}
	    } catch ( AlignmentException aex ) {
		logger.debug( "IGNORED Exception (continue importing)", aex );
	    } catch ( OntowrapException owex ) {
		throw new AlignmentException( "Cannot dereference entity", owex );
	    }
	}
    }

    private static Id createEDOALExpression( LoadedOntology<Object> o, URI u ) throws OntowrapException, AlignmentException {
	Object e = o.getEntity( u );
	if ( o.isClass( e ) ) {
	    return new ClassId( u );
	} else if ( o.isDataProperty( e ) ) {
	    return new PropertyId( u );
	} else if ( o.isObjectProperty( e ) ) {
	    return new RelationId( u );
	} else if ( o.isIndividual( e ) ) {
	    return new InstanceId( u );
	} else throw new AlignmentException( "Cannot interpret URI "+u );
    }


    /**
     * Generate a copy of this alignment object
     */
    // JE: this is a mere copy of the method in BasicAlignement
    // It has two difficulties
    // - it should call the current init() and not that of BasicAlignement
    // - it should catch the AlignmentException that it is supposed to raise
    public Object clone() {
	EDOALAlignment align = new EDOALAlignment();
	try {
	    align.init( (Ontology)getOntology1(), (Ontology)getOntology2() );
	} catch ( AlignmentException e ) {};
	align.setType( getType() );
	align.setLevel( getLevel() );
	align.setFile1( getFile1() );
	align.setFile2( getFile2() );
	align.setExtensions( convertExtension( "cloned", this.getClass().getName()+"#clone" ) );
	try {
	    align.ingest( this );
	} catch (AlignmentException ex) { 
	    logger.debug( "IGNORED Exception", ex );
	}
	return align;
    }

 }
