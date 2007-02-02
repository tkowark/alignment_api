/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2005, 2007
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

import java.util.Enumeration;
import java.util.Random;
import java.io.PrintWriter;
import java.net.URI;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.OWLAPIAlignment;
import fr.inrialpes.exmo.align.impl.rel.*;

/**
 * Renders an alignment as a new ontology merging these.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */


public class SEKTMappingRendererVisitor implements AlignmentVisitor
{
    PrintWriter writer = null;
    Alignment alignment = null;
    Cell cell = null;
    // I hate using random generator for generating symbols (address would be better)
    Random generator = null;

    public SEKTMappingRendererVisitor( PrintWriter writer ){
	this.writer = writer;
	generator = new Random();
    }

    public void visit( Alignment align ) throws AlignmentException {
	alignment = align;
	if ( !(align instanceof OWLAPIAlignment) )
	    throw new AlignmentException("SEKTMappingRenderer: cannot render simple alignment. Turn them into OWLAlignment, by toOWLAPIAlignement()");
	writer.print("MappingDocument(<\""+"\">\n");
	writer.print("  source(<\""+align.getOntology1URI().toString()+"\">)\n");
	writer.print("  target(<\""+align.getOntology2URI().toString()+"\">)\n");

	for( Enumeration e = align.getElements() ; e.hasMoreElements(); ){
	    Cell c = (Cell)e.nextElement();
	    c.accept( this );
	} //end for
	writer.print(")\n");
    }
    public void visit( Cell cell ) throws AlignmentException {
	this.cell = cell;
	String id = "s"+generator.nextInt(100000);
	OWLOntology onto1 = (OWLOntology)alignment.getOntology1();
	//OWLOntology onto2 = (OWLOntology)alignment.getOntology2();
	try {
	    URI entity1URI = cell.getObject1AsURI();
	    URI entity2URI = cell.getObject2AsURI();
	    if ( (OWLEntity)onto1.getClass( entity1URI ) != null ) { // A class
		writer.print("  classMapping( <\"#"+id+"\">\n");
		cell.getRelation().accept( this );
		writer.print("    <\""+entity1URI.toString()+"\">\n");
		writer.print("    <\""+entity2URI.toString()+"\">\n");
		
		writer.print("  )\n");
	    } else if ( (OWLEntity)onto1.getDataProperty( entity1URI ) != null ) { // A Dataproperty
		writer.print("  relationMapping( <\"#"+id+"\">\n");
		cell.getRelation().accept( this );
		writer.print("    <\""+entity1URI.toString()+"\">\n");
		writer.print("    <\""+entity2URI.toString()+"\">\n");
		writer.print("  )\n");
	    } else if ( (OWLEntity)onto1.getObjectProperty( entity1URI ) != null ) { // An ObjectProperty
		writer.print("  attributeMapping( <\"#"+id+"\">\n");
		cell.getRelation().accept( this );
		writer.print("    <\""+entity1URI.toString()+"\">\n");
		writer.print("    <\""+entity2URI.toString()+"\">\n");
		writer.print("  )\n");
	    } else if ( (OWLEntity)onto1.getIndividual( entity1URI ) != null ) { // An individual (but check this)
		writer.print("  instanceMapping( <\"#"+id+"\">\n");
		cell.getRelation().accept( this );
		writer.print("    <\""+entity1URI.toString()+"\">\n");
		writer.print("    <\""+entity2URI.toString()+"\">\n");
		writer.print("  )\n");
	    }
	    writer.print("\n");
	} catch (OWLException e) { throw new AlignmentException("getURI problem", e); };
    }

    public void visit( EquivRelation rel ) throws AlignmentException {
	writer.print("    bidirectional\n");
    }
    public void visit( SubsumeRelation rel ) throws AlignmentException {
	writer.print("    unidirectional\n");
    }
    public void visit( SubsumedRelation rel ) throws AlignmentException {
	writer.print("    unidirectional\n");
    }
    public void visit( IncompatRelation rel ) throws AlignmentException {
	writer.print("    unidirectional\n");
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
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	} catch (InvocationTargetException e) { 
	    e.printStackTrace();
	}
	//	} catch (Exception e) { throw new AlignmentException("Dispatching problem ", e); };
    };
}
