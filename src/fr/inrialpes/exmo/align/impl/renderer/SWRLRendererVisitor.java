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
 * Renders an alignment as a SWRL rule set interpreting
 *.data of the first ontology into the second one.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */


public class SWRLRendererVisitor implements AlignmentVisitor
{
    PrintWriter writer = null;
    Alignment alignment = null;
    Cell cell = null;

    public SWRLRendererVisitor( PrintWriter writer ){
	this.writer = writer;
    }

    public void visit( Alignment align ) throws AlignmentException {
	alignment = align;
	writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	writer.println("<swrlx:Ontology swrlx:name=\"generatedAl\"");
	writer.println("                xmlns:swrlx=\"http://www.w3.org/2003/11/swrlx#\"");
	writer.println("                xmlns:owlx=\"http://www.w3.org/2003/05/owl-xml\"");
	writer.println("                xmlns:ruleml=\"http://www.w3.org/2003/11/ruleml#\">");
	try {
	    writer.println("  <owlx:Imports rdf:resource=\""+((OWLOntology)align.getOntology1()).getLogicalURI().toString()+"\"/>\n");
	    for( Enumeration e = align.getElements() ; e.hasMoreElements(); ){
		Cell c = (Cell)e.nextElement();
		c.accept( this );
	    }
	} catch (OWLException e) { throw new AlignmentException("getURI problem", e); };
	writer.println("</swrlx:Ontology>");
    }

    public void visit( Cell cell ) throws AlignmentException {
	this.cell = cell;
	OWLOntology onto1 = (OWLOntology)alignment.getOntology1();
	try {
	    URI entity1URI = ((OWLEntity)cell.getObject1()).getURI();
	    if ( ((OWLEntity)onto1.getClass( entity1URI ) != null )
		 || ((OWLEntity)onto1.getDataProperty( entity1URI ) != null)
		 || ((OWLEntity)onto1.getObjectProperty( entity1URI ) != null )) { 
		cell.getRelation().accept( this );
	    }
	} catch (OWLException e) { throw new AlignmentException("getURI problem", e); };
    }

    public void visit( EquivRelation rel ) throws AlignmentException {
	// JE: It sounds that the alignment and cell variables are taken as global...
	// But it seems that this is not the case...
	// JE: We should send warnings when dataproperties are mapped to individual properties and vice versa...
	try {
	    writer.println("  <ruleml:imp>");
	    writer.println("    <ruleml:_body>");
	    OWLOntology onto1 = (OWLOntology)alignment.getOntology1();
	    OWLEntity obj1 = (OWLEntity)cell.getObject1();
	    URI uri1 = obj1.getURI();
	    if ( onto1.getClass( uri1 ) != null ){
		writer.println("      <swrl:classAtom>");
		writer.println("        <owllx:Class owllx:name=\""+uri1.toString()+"\"/>");
		writer.println("        <ruleml:var>x</ruleml:var>");
		writer.println("      </swrl:classAtom>");
	    } else if ( onto1.getDataProperty( uri1 )  != null ){
		writer.println("      <swrl:datavaluedPropertyAtom swrlx:property=\""+uri1.toString()+"\"/>");
		writer.println("        <ruleml:var>x</ruleml:var>");
		writer.println("        <ruleml:var>y</ruleml:var>");
		writer.println("      <swrl:datavaluedPropertyAtom>");
	    } else {
		writer.println("      <swrl:individualPropertyAtom swrlx:property=\""+uri1.toString()+"\"/>");
		writer.println("        <ruleml:var>x</ruleml:var>");
		writer.println("        <ruleml:var>y</ruleml:var>");
		writer.println("      </swrl:individualPropertyAtom>");
	    }
	    writer.println("    </ruleml:_body>");
	    writer.println("    <ruleml:_head>");
	    OWLOntology onto2 = (OWLOntology)alignment.getOntology2();
	    OWLEntity obj2 = (OWLEntity)cell.getObject2();
	    URI uri2 = obj2.getURI();
	    if ( onto2.getClass( uri2 ) != null ){
		writer.println("      <swrlx:classAtom>");
		writer.println("        <owllx:Class owllx:name=\""+uri2.toString()+"\"/>");
		writer.println("        <ruleml:var>x</ruleml:var>");
		writer.println("      </swrl:classAtom>");
	    } else if ( onto2.getDataProperty( uri2 )  != null ){
		writer.println("      <swrl:datavaluedPropertyAtom swrlx:property=\""+uri2.toString()+"\"/>");
		writer.println("        <ruleml:var>x</ruleml:var>");
		writer.println("        <ruleml:var>y</ruleml:var>");
		writer.println("      </swrl:datavaluedPropertyAtom>");
	    } else {
		writer.println("      <swrl:individualPropertyAtom swrlx:property=\""+uri2.toString()+"\"/>");
		writer.println("        <ruleml:var>x</ruleml:var>");
		writer.println("        <ruleml:var>y</ruleml:var>");
		writer.println("      </swrl:individualPropertyAtom>");
	    }
	    writer.println("    </ruleml:_head>");
	    writer.println("  </ruleml:imp>\n");
	} catch (Exception e) { throw new AlignmentException("getURI problem", e); };
    }

    public void visit( SubsumeRelation rel ){};
    public void visit( IncompatRelation rel ){};

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
	    } else if (Class.forName("fr.inrialpes.exmo.align.impl.rel.IncompatRelation").isInstance(rel) ) {
		mm = this.getClass().getMethod("visit",
					       new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.IncompatRelation")});
	    }
	    if ( mm != null ) mm.invoke(this,new Object[] {rel});
	} catch (Exception e) { throw new AlignmentException("Dispatching problem ", e); };
    };
}
