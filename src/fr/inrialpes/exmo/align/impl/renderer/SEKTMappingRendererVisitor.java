/*
 * $Id$
 *
 * Copyright (C) INRIA, 2003-2005, 2007-2009
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
import java.util.Properties;
import java.util.Random;
import java.io.PrintWriter;
import java.net.URI;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.semanticweb.owl.align.Visitable;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.rel.*;
import fr.inrialpes.exmo.align.onto.LoadedOntology;

/**
 * Renders an alignment as a new ontology merging these.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class SEKTMappingRendererVisitor implements AlignmentVisitor {
    PrintWriter writer = null;
    Alignment alignment = null;
    LoadedOntology onto1 = null;
    LoadedOntology onto2 = null;
    Cell cell = null;
    // I hate using random generator for generating symbols (address would be better)
    Random generator = null;

    public SEKTMappingRendererVisitor( PrintWriter writer ){
	this.writer = writer;
	generator = new Random();
    }

    public void init( Properties p ) {};

    public void visit( Visitable o ) throws AlignmentException {
	if ( o instanceof Alignment ) visit( (Alignment)o );
	else if ( o instanceof Cell ) visit( (Cell)o );
	else if ( o instanceof Relation ) visit( (Relation)o );
    }

    public void visit( Alignment align ) throws AlignmentException {
	if ( !(align instanceof ObjectAlignment) )
	    throw new AlignmentException("SEKTMappingRenderer: cannot render simple alignment. Turn them into ObjectAlignment, by toObjectAlignement()");
	alignment = align;
	onto1 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject1();
	onto2 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject2();
	writer.print("MappingDocument(<\""+"\">\n");
	writer.print("  source(<\""+onto1.getURI()+"\">)\n");
	writer.print("  target(<\""+onto2.getURI()+"\">)\n");

	for( Cell c : align ){
	    c.accept( this );
	} //end for
	writer.print(")\n");
    }
    public void visit( Cell cell ) throws AlignmentException {
	this.cell = cell;
	String id = String.format( "s%06d", generator.nextInt(100000) );
	Object ob1 = cell.getObject1();
	Object ob2 = cell.getObject2();
	if ( onto1.isClass( ob1 ) ) {
	    writer.print("  classMapping( <\"#"+id+"\">\n");
	    cell.getRelation().accept( this );
	    writer.print("    <\""+onto1.getEntityURI( ob1 )+"\">\n");
	    writer.print("    <\""+onto2.getEntityURI( ob2 )+"\">\n");
	    writer.print("  )\n");
	} else if ( onto1.isDataProperty( ob1 ) ) {
	    writer.print("  relationMapping( <\"#"+id+"\">\n");
	    cell.getRelation().accept( this );
	    writer.print("    <\""+onto1.getEntityURI( ob1 )+"\">\n");
	    writer.print("    <\""+onto2.getEntityURI( ob2 )+"\">\n");
	    writer.print("  )\n");
	} else if ( onto1.isObjectProperty( ob1 ) ) {
	    writer.print("  attributeMapping( <\"#"+id+"\">\n");
	    cell.getRelation().accept( this );
	    writer.print("    <\""+onto1.getEntityURI( ob1 )+"\">\n");
	    writer.print("    <\""+onto2.getEntityURI( ob2 )+"\">\n");
	    writer.print("  )\n");
	} else if ( onto1.isIndividual( ob1 ) ) {
	    writer.print("  instanceMapping( <\"#"+id+"\">\n");
	    cell.getRelation().accept( this );
	    writer.print("    <\""+onto1.getEntityURI( ob1 )+"\">\n");
	    writer.print("    <\""+onto2.getEntityURI( ob2 )+"\">\n");
	    writer.print("  )\n");
	}
	writer.print("\n");
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
