/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2004
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

package fr.inrialpes.exmo.align.impl.renderer; 

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.rel.*;

/**
 * Renders an alignment as a new ontology merging these.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */


public class OWLAxiomsRendererVisitor implements AlignmentVisitor
{
    PrintWriter writer = null;
    Alignment alignment = null;
    Cell cell = null;

    public OWLAxiomsRendererVisitor( PrintWriter writer ){
	this.writer = writer;
    }

    public void visit( Alignment align ) throws AlignmentException {
	alignment = align;
	writer.print("<rdf:RDF\n");
	writer.print("    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n");
	writer.print("    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n");
	writer.print("    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" \n");
	writer.print("    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">\n\n");	
	try {
	    writer.print("  <owl:Ontology rdf:about=\"\">\n");
	    writer.print("    <rdfs:comment>Aligned ontollogies</rdfs:comment>\n");
	    writer.print("    <owl:imports rdf:resource=\""+((OWLOntology)align.getOntology1()).getLogicalURI().toString()+"\"/>\n");
	    writer.print("    <owl:imports rdf:resource=\""+((OWLOntology)align.getOntology2()).getLogicalURI().toString()+"\"/>\n");
	    writer.print("  </owl:Ontology>\n\n");

	    for( Enumeration e = align.getElements() ; e.hasMoreElements(); ){
		Cell c = (Cell)e.nextElement();
		c.accept( this );
	    } //end for
	} catch (OWLException e) { throw new AlignmentException("getURI problem", e); };
	
	writer.print("</rdf:RDF>\n");
    }

    public void visit( Cell cell ) throws AlignmentException {
	this.cell = cell;
	OWLOntology onto1 = null;
	URI entity1URI = null;
	try {
	    // Not very good but we failed to think subsumed from the first shot.
	    if ( Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumedRelation").isInstance(cell.getRelation()) ) {
		onto1 = (OWLOntology)alignment.getOntology2();
		entity1URI = ((OWLEntity)cell.getObject2()).getURI();
	    } else {
		onto1 = (OWLOntology)alignment.getOntology1();
		entity1URI = ((OWLEntity)cell.getObject1()).getURI();
	    }
	    if ( (OWLEntity)onto1.getClass( entity1URI ) != null ) { // A class
		writer.print("  <owl:Class rdf:about=\""+entity1URI.toString()+"\">\n");
		cell.getRelation().accept( this );
		writer.print("  </owl:Class>\n");
	    } else if ( (OWLEntity)onto1.getDataProperty( entity1URI ) != null ) { // A Dataproperty
		writer.print("  <owl:DatatypeProperty rdf:about=\""+entity1URI.toString()+"\">\n");
		cell.getRelation().accept( this );
		writer.print("  </owl:DatatypeProperty>\n");
	    } else if ( (OWLEntity)onto1.getObjectProperty( entity1URI ) != null ) { // An ObjectProperty
		writer.print("  <owl:ObjectProperty rdf:about=\""+entity1URI.toString()+"\">\n");
		cell.getRelation().accept( this );
		writer.print("  </owl:ObjectProperty>\n");
	    } else if ( (OWLEntity)onto1.getIndividual( entity1URI ) != null ) { // An individual (but check this)
		writer.print("  <owl:Thing rdf:about=\""+entity1URI.toString()+"\">\n");
		cell.getRelation().accept( this );
		writer.print("  </owl:Thing>\n");
	    }
	    writer.print("\n");
	}
	catch (OWLException e) { throw new AlignmentException("getURI problem", e); }
	catch (ClassNotFoundException e) { throw new AlignmentException("Class no found", e); };
    }
    public void visit( EquivRelation rel ) throws AlignmentException {
	OWLOntology onto2 = (OWLOntology)alignment.getOntology2();
	try {
	    URI entity2URI = ((OWLEntity)cell.getObject2()).getURI();
	    if ( (OWLEntity)onto2.getClass( entity2URI ) != null ) { // A class
		writer.print("    <owl:equivalentClass rdf:resource=\""+entity2URI.toString()+"\"/>\n");
	    } else if ( (OWLEntity)onto2.getDataProperty( entity2URI ) != null ) { // A Dataproperty
		writer.print("    <owl:equivalentProperty rdf:resource=\""+entity2URI.toString()+"\"/>\n");
	    } else if ( (OWLEntity)onto2.getObjectProperty( entity2URI ) != null ) { // An ObjectProperty
		writer.print("    <owl:equivalentProperty rdf:resource=\""+entity2URI.toString()+"\"/>\n");
	    } else if ( (OWLEntity)onto2.getIndividual( entity2URI ) != null ) { // An individual (but check this)
		writer.print("    <owl:sameAs rdf:resource=\""+entity2URI.toString()+"\"/>\n");
	    }
	} catch (OWLException e) { throw new AlignmentException("getURI problem", e); };
    }


    public void visit( SubsumeRelation rel ) throws AlignmentException {
	OWLOntology onto2 = (OWLOntology)alignment.getOntology2();
	try {
	    URI entity2URI = ((OWLEntity)cell.getObject2()).getURI();
	    if ( (OWLEntity)onto2.getClass( entity2URI ) != null ) { // A class
		writer.print("    <rdfs:subClassOf rdf:resource=\""+entity2URI.toString()+"\"/>\n");
	    } else if ( (OWLEntity)onto2.getDataProperty( entity2URI ) != null ) { // A Dataproperty
		writer.print("    <rdfs:subPropertyOf rdf:resource=\""+entity2URI.toString()+"\"/>\n");
	    } else if ( (OWLEntity)onto2.getObjectProperty( entity2URI ) != null ) { // An ObjectProperty
		writer.print("    <rdfs:subPropertyOf rdf:resource=\""+entity2URI.toString()+"\"/>\n");
	    }
	} catch (OWLException e) { throw new AlignmentException("getURI problem", e); };
    }
    public void visit( SubsumedRelation rel ) throws AlignmentException {
	OWLOntology onto1 = (OWLOntology)alignment.getOntology1();
	try {
	    URI entity1URI = ((OWLEntity)cell.getObject1()).getURI();
	    if ( (OWLEntity)onto1.getClass( entity1URI ) != null ) { // A class
		writer.print("    <rdfs:subClassOf rdf:resource=\""+entity1URI.toString()+"\"/>\n");
	    } else if ( (OWLEntity)onto1.getDataProperty( entity1URI ) != null ) { // A Dataproperty
		writer.print("    <rdfs:subPropertyOf rdf:resource=\""+entity1URI.toString()+"\"/>\n");
	    } else if ( (OWLEntity)onto1.getObjectProperty( entity1URI ) != null ) { // An ObjectProperty
		writer.print("    <rdfs:subPropertyOf rdf:resource=\""+entity1URI.toString()+"\"/>\n");
	    }
	} catch (OWLException e) { throw new AlignmentException("getURI problem", e); };
    }
    public void visit( IncompatRelation rel ) throws AlignmentException {
	OWLOntology onto2 = (OWLOntology)alignment.getOntology2();
	try {
	    URI entity2URI = ((OWLEntity)cell.getObject2()).getURI();
	    writer.print("    <owl:inverseOf rdf:resource=\""+entity2URI.toString()+"\"/>\n");
	} catch (OWLException e) { throw new AlignmentException("getURI problem", e); };
    }
    public void visit( Relation rel ) throws AlignmentException {
	// JE: I do not understand why I need this,
	// but this seems to be the case...
	try {
	    Method mm = null;
	    if ( Class.forName("fr.inrialpes.exmo.align.impl.rel.EquivRelation").isInstance(rel) ){
		mm = this.getClass().getMethod("visit",
					       new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.EquivRelation")});
	    } else if (Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumeRelation").isInstance(rel) ) {
		mm = this.getClass().getMethod("visit",
					       new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumeRelation")});
	    } else if (Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumedRelation").isInstance(rel) ) {
		mm = this.getClass().getMethod("visit",
					       new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumedRelation")});
	    } else if (Class.forName("fr.inrialpes.exmo.align.impl.rel.IncompatRelation").isInstance(rel) ) {
		mm = this.getClass().getMethod("visit",
					       new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.IncompatRelation")});
	    }
	    if ( mm != null ) mm.invoke(this,new Object[] {rel});
	} catch (Exception e) { throw new AlignmentException("Dispatching problem ", e); };
    };
}
