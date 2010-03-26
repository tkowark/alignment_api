/*
 * $Id$
 *
 * Copyright (C) INRIA, 2010
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

package fr.inrialpes.exmo.align.parser;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;
import java.net.URI;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.Extensions;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectCell;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.Ontology; //?

import fr.inrialpes.exmo.align.parser.SyntaxElement;
import fr.inrialpes.exmo.align.parser.SyntaxElement.Constructor;

import fr.inrialpes.exmo.align.impl.edoal.Id;
import fr.inrialpes.exmo.align.impl.edoal.PathExpression;
import fr.inrialpes.exmo.align.impl.edoal.Expression;
import fr.inrialpes.exmo.align.impl.edoal.ClassExpression;
import fr.inrialpes.exmo.align.impl.edoal.ClassId;
import fr.inrialpes.exmo.align.impl.edoal.ClassConstruction;
import fr.inrialpes.exmo.align.impl.edoal.ClassRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassOccurenceRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyExpression;
import fr.inrialpes.exmo.align.impl.edoal.PropertyId;
import fr.inrialpes.exmo.align.impl.edoal.PropertyConstruction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationExpression;
import fr.inrialpes.exmo.align.impl.edoal.RelationId;
import fr.inrialpes.exmo.align.impl.edoal.RelationConstruction;
import fr.inrialpes.exmo.align.impl.edoal.RelationRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationCoDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.InstanceExpression;
import fr.inrialpes.exmo.align.impl.edoal.InstanceId;

import fr.inrialpes.exmo.align.impl.edoal.TransfService;
import fr.inrialpes.exmo.align.impl.edoal.Value;
import fr.inrialpes.exmo.align.impl.edoal.ValueExpression;
import fr.inrialpes.exmo.align.impl.edoal.Apply;
import fr.inrialpes.exmo.align.impl.edoal.Datatype;
import fr.inrialpes.exmo.align.impl.edoal.Comparator;

import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.impl.edoal.EDOALCell;
import fr.inrialpes.exmo.align.impl.edoal.EDOALRelation;

/**
 * Renders an alignment in its RDF format
 *
 * @author Jérôme Euzenat
 * @version $Id: RDFRendererVisitor.java 1335 2010-03-18 20:52:30Z euzenat $
 */

public class TypeCheckingVisitor {

    EDOALAlignment alignment = null;
    EDOALCell cell = null;
    Hashtable<String,String> nslist = null;
    boolean embedded = false; // if the output is XML embeded in a structure

    private static Namespace DEF = Namespace.ALIGNMENT;
    
    private boolean isPattern = false;
	
    /*
     * JE: These major dispatches are a pain.
     * I should learn a bit more Java about that 
     * (and at least inverse the order
     */
    // JE: Beware: THERE MAY BE EFFECTIVE STUFF MISSING THERE (CAN WE DO THE DISPATCH LOWER -- YES)
    // It is a real mess already...
    public void visit( Expression o ) throws AlignmentException {
	if ( o instanceof ClassExpression ) visit( (ClassExpression)o );
	//else if ( o instanceof TransfService ) visit( (TransfService)o );
	else if ( o instanceof RelationRestriction ) visit( (RelationRestriction)o );
	else if ( o instanceof PropertyRestriction ) visit( (PropertyRestriction)o );
	else if ( o instanceof ClassRestriction ) visit( (ClassRestriction)o );
	else if ( o instanceof PathExpression ) visit( (PathExpression)o );
	else if ( o instanceof PropertyExpression ) visit( (PropertyExpression)o );
	else if ( o instanceof InstanceExpression ) visit( (InstanceExpression)o );
	else if ( o instanceof RelationExpression ) visit( (RelationExpression)o );
	throw new AlignmentException("Cannot export abstract Expression: "+o );
    }

    public void visit( EDOALAlignment align ) throws AlignmentException {
	alignment = align;
	if ( alignment.getLevel().startsWith("2EDOALPattern") ) isPattern = true;
	    align.getOntology1URI();
	    for( Cell c : align ){ ((EDOALCell)c).accept( this ); };
    }

    public void visit( EDOALCell cell ) throws AlignmentException {
	this.cell = cell;
	if ( alignment.getLevel().startsWith("2EDOAL") ) {
	    ((Expression)(cell.getObject1())).accept( this );
	    ((Expression)(cell.getObject2())).accept( this );
	}
    }

    public void visit( EDOALRelation rel ) {
    };

    public void visit( final PathExpression p ) throws AlignmentException {
	if ( p instanceof RelationExpression ) visit( (RelationExpression)p );
	else if ( p instanceof PropertyExpression ) visit( (PropertyExpression)p );
	else throw new AlignmentException( "Cannot dispatch PathExpression "+p );
    }

    public void visit( final ClassExpression e ) throws AlignmentException {
	if ( e instanceof ClassId ) visit( (ClassId)e );
	else if ( e instanceof ClassConstruction )  visit( (ClassConstruction)e );
	else if ( e instanceof ClassRestriction )  visit( (ClassRestriction)e );
	else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }

    public void visit( final ClassId e ) throws AlignmentException {
    }

    public void visit( final ClassConstruction e ) throws AlignmentException {
	final Constructor op = e.getOperator();
	for (final ClassExpression ce : e.getComponents()) {
	    visit( ce );
	}
    }
    
    public void visit(final ClassRestriction e) throws AlignmentException {
	if ( e instanceof ClassValueRestriction ) visit( (ClassValueRestriction)e );
	else if ( e instanceof ClassTypeRestriction )  visit( (ClassTypeRestriction)e );
	else if ( e instanceof ClassDomainRestriction )  visit( (ClassDomainRestriction)e );
	else if ( e instanceof ClassOccurenceRestriction )  visit( (ClassOccurenceRestriction)e );
	else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }

    public void visit( final ClassValueRestriction c ) throws AlignmentException {
	visit( c.getRestrictionPath() );
	visit( c.getValue() );
    }

    public void visit( final ClassTypeRestriction c ) throws AlignmentException {
	visit( c.getRestrictionPath() );
	visit( c.getType() );
    }

    public void visit( final ClassDomainRestriction c ) throws AlignmentException {
	visit( c.getRestrictionPath() );
	visit( c.getDomain() );
    }

    public void visit( final ClassOccurenceRestriction c ) throws AlignmentException {
	visit( c.getRestrictionPath() );
	c.getComparator().getURI();
	c.getOccurence();
    }
    
    public void visit(final PropertyExpression e) throws AlignmentException {
	if ( e instanceof PropertyId ) visit( (PropertyId)e );
	else if ( e instanceof PropertyConstruction ) visit( (PropertyConstruction)e );
	else if ( e instanceof PropertyRestriction ) visit( (PropertyRestriction)e );
	else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }
	
    public void visit(final PropertyId e) throws AlignmentException {
    }

    public void visit(final PropertyConstruction e) throws AlignmentException {
	final Constructor op = e.getOperator();
	for ( final PathExpression pe : e.getComponents() ) {
	    visit( pe );
	}
    }
    
    public void visit(final PropertyRestriction e) throws AlignmentException {
	if ( e instanceof PropertyValueRestriction ) visit( (PropertyValueRestriction)e );
	else if ( e instanceof PropertyDomainRestriction ) visit( (PropertyDomainRestriction)e );
	else if ( e instanceof PropertyTypeRestriction ) visit( (PropertyTypeRestriction)e );
	else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }
	
    public void visit(final PropertyValueRestriction c) throws AlignmentException {
	c.getComparator().getURI();
	visit( c.getValue() );
    }

    public void visit(final PropertyDomainRestriction c) throws AlignmentException {
	visit( c.getDomain() );
    }

    public void visit(final PropertyTypeRestriction c) throws AlignmentException {
	visit( c.getType() );
    }
    
    public void visit( final RelationExpression e ) throws AlignmentException {
	if ( e instanceof RelationId ) visit( (RelationId)e );
	else if ( e instanceof RelationRestriction ) visit( (RelationRestriction)e );
	else if ( e instanceof RelationConstruction ) visit( (RelationConstruction)e );
	else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }
	
    public void visit( final RelationId e ) throws AlignmentException {
    }

    public void visit( final RelationConstruction e ) throws AlignmentException {
	final Constructor op = e.getOperator();
	for (final PathExpression re : e.getComponents()) {
	    visit( re );
	}
    }
    
    public void visit( final RelationRestriction e ) throws AlignmentException {
	if ( e instanceof RelationCoDomainRestriction ) visit( (RelationCoDomainRestriction)e );
	else if ( e instanceof RelationDomainRestriction ) visit( (RelationDomainRestriction)e );
	else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }
	
    public void visit(final RelationCoDomainRestriction c) throws AlignmentException {
	visit( c.getCoDomain() );
    }

    public void visit(final RelationDomainRestriction c) throws AlignmentException {
	visit( c.getDomain() );
    }
    
    public void visit( final InstanceExpression e ) throws AlignmentException {
	if ( e instanceof InstanceId ) visit( (InstanceId)e );
	else throw new AlignmentException( "Cannot handle InstanceExpression "+e );
    }

    public void visit( final InstanceId e ) throws AlignmentException {
    }
    
    public void visit( final Value e ) throws AlignmentException {
    }
	
    public void visit( final ValueExpression e ) throws AlignmentException {
    }
	
    public void visit( final Apply e ) throws AlignmentException {
    }
	
    public void visit( final Datatype e ) throws AlignmentException {
    }
	
}
