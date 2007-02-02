/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2004, 2007
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
import java.io.PrintWriter;
import java.net.URI;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.OWLAPIAlignment;
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
	if ( !( align instanceof OWLAPIAlignment) )
	    throw new AlignmentException("SWRLRenderer: cannot render simple alignment. Turn them into OWLAlignment, by toOWLAPIAlignement()");
	alignment = align;
	writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	writer.println("<swrlx:Ontology swrlx:name=\"generatedAl\"");
	writer.println("                xmlns:swrlx=\"http://www.w3.org/2003/11/swrlx#\"");
	writer.println("                xmlns:owlx=\"http://www.w3.org/2003/05/owl-xml\"");
	writer.println("                xmlns:ruleml=\"http://www.w3.org/2003/11/ruleml#\">");
	writer.println("  <owlx:Imports rdf:resource=\""+align.getOntology1URI().toString()+"\"/>\n");
	for( Enumeration e = align.getElements() ; e.hasMoreElements(); ){
	    Cell c = (Cell)e.nextElement();
	    c.accept( this );
	}
	writer.println("</swrlx:Ontology>");
    }

    public void visit( Cell cell ) throws AlignmentException {
	this.cell = cell;
	URI entity1URI = cell.getObject1AsURI();
	cell.getRelation().accept( this );
    }

    public void visit( EquivRelation rel ) throws AlignmentException {
	// JE: It sounds that the alignment and cell variables are taken as global...
	// But it seems that this is not the case...
	// JE: We should send warnings when dataproperties are mapped to individual properties and vice versa...
	try {
	    writer.println("  <ruleml:imp>");
	    writer.println("    <ruleml:_body>");
	    OWLOntology onto1 = (OWLOntology)alignment.getOntology1();
	    URI uri1 = cell.getObject1AsURI();
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
	    URI uri2 = cell.getObject2AsURI();
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
	} catch (OWLException e) { throw new AlignmentException("getURI problem", e); };
    }

    public void visit( SubsumeRelation rel ){};
    public void visit( SubsumedRelation rel ){};
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
