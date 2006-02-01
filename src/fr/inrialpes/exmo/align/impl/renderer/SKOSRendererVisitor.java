/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2004, 2006
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
 * @version $$ 
 */


public class SKOSRendererVisitor implements AlignmentVisitor
{
    PrintWriter writer = null;
    Alignment alignment = null;
    Cell cell = null;

    public SKOSRendererVisitor( PrintWriter writer ){
	this.writer = writer;
    }

    // This must be considered
    public void visit( Alignment align ) throws AlignmentException {
	alignment = align;
	writer.print("<rdf:RDF\n");
	writer.print("  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"); 
	writer.print("  xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n");
	writer.print("  xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">\n\n");
	for( Enumeration e = align.getElements() ; e.hasMoreElements(); ){
	    Cell c = (Cell)e.nextElement();
	    c.accept( this );
	} //end for
	writer.print("</rdf:RDF>\n");
    }

    public void visit( Cell cell ) throws AlignmentException {
	this.cell = cell;
	try {
	    OWLOntology onto1 = (OWLOntology)alignment.getOntology1();
	    URI entity1URI = ((OWLEntity)cell.getObject1()).getURI();
	    writer.print("  <skos:Concept rdf:about=\""+entity1URI.toString()+"\">\n");
	    cell.getRelation().accept( this );
	    writer.print("  </skos:Concept>\n\n");
	} catch (OWLException e) { throw new AlignmentException("getURI problem", e); }
    }
    public void visit( EquivRelation rel ) throws AlignmentException {
	try {
	    OWLOntology onto2 = (OWLOntology)alignment.getOntology2();
	    URI entity2URI = ((OWLEntity)cell.getObject2()).getURI();
	    writer.print("    <skos:related rdf:resource=\""+entity2URI.toString()+"\"/>\n");
	} catch (OWLException e) { throw new AlignmentException("getURI problem", e); }
    }
    public void visit( SubsumeRelation rel ) throws AlignmentException {
	try {
	    OWLOntology onto2 = (OWLOntology)alignment.getOntology2();
	    URI entity2URI = ((OWLEntity)cell.getObject2()).getURI();
	    writer.print("    <skos:narrower rdf:resource=\""+entity2URI.toString()+"\"/>\n");
	} catch (OWLException e) { throw new AlignmentException("getURI problem", e); }
    }
    public void visit( SubsumedRelation rel ) throws AlignmentException {
	try {
	    OWLOntology onto2 = (OWLOntology)alignment.getOntology2();
	    URI entity2URI = ((OWLEntity)cell.getObject2()).getURI();
	    writer.print("    <skos:broader rdf:resource=\""+entity2URI.toString()+"\"/>\n");
	} catch (OWLException e) { throw new AlignmentException("getURI problem", e); }
    }
    public void visit( IncompatRelation rel ) throws AlignmentException {
	throw new AlignmentException("Cannot translate in SKOS"+rel);
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
