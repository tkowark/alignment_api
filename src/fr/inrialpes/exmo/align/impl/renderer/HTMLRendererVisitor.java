/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2006
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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
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
 * Renders an alignment in HTML
 *
 * TODO:
 * - add CSS categories
 * - add resource chooser
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class HTMLRendererVisitor implements AlignmentVisitor
{
    
    PrintWriter writer = null;
    Alignment alignment = null;
    Cell cell = null;

    public HTMLRendererVisitor( PrintWriter writer ){
	this.writer = writer;
    }

    public void visit( Alignment align ) throws AlignmentException {
	//alignment = align;
	writer.print("<html>\n<head></head>\n<body>\n");
	writer.print("<h1></h1>\n");
	writer.print("<h2>Alignment metadata</h2>\n");
	writer.print("<table border=\"0\">\n");
	try {
	    writer.print("<tr><td>uri1</td><td>"+((OWLOntology)align.getOntology1()).getLogicalURI().toString()+"</td></tr>\n" );
	    writer.print("<tr><td>uri2</td><td>"+((OWLOntology)align.getOntology2()).getLogicalURI().toString()+"</td></tr>\n" );
	} catch (OWLException ex) { 
	    throw new AlignmentException( "getURI problem", ex);
	}
	if ( align.getFile1() != null )
	    writer.print("<tr><td>ontofile1</td><td><a href=\""+align.getFile1().toString()+"\">"+align.getFile1().toString()+"</a></td></tr>\n" );
	if ( align.getFile2() != null )
	    writer.print("<tr><td>ontofile2</td><td><a href=\""+align.getFile2().toString()+"\">"+align.getFile2().toString()+"</a></td></tr>\n" );
	writer.print("<tr><td>level</td><td>"+align.getLevel()+"</td></tr>\n" );
	writer.print("<tr><td>type</td><td>"+align.getType()+"</td></tr>\n" );
	// Get the keys of the parameter
	for( Enumeration e = align.getExtensions().getNames() ; e.hasMoreElements() ; ){
	    String tag = (String)e.nextElement();
	    writer.print("<tr><td>"+tag+"</td><td>"+align.getExtension(tag)+"</td></tr>\n");
	}
	writer.print("</table>\n");
	writer.print("<h2>Correspondences</h2>\n");
	writer.print("<table><tr><td>Id</td><td>object1</td><td>relation</td><td>strength</td><td>object2</td></tr>\n");
	for( Enumeration e = align.getElements() ; e.hasMoreElements(); ){
	    Cell c = (Cell)e.nextElement();
	    c.accept( this );
	} //end for
	writer.print("</table>\n");
	writer.print("</body>\n</html>\n");
    }
    public void visit( Cell cell ) throws AlignmentException {
	this.cell = cell;
	//OWLOntology onto1 = (OWLOntology)alignment.getOntology1();
	try {
	    writer.print("  <tr>");
	    if ( cell.getId() != null ) writer.print("<td>"+cell.getId()+"</td>");
	    else writer.print("<td></td>");
	    writer.print("<td>"+((OWLEntity)cell.getObject1()).getURI().toString()+"</td><td>");
	    cell.getRelation().accept( this );
	    writer.print("</td><td>"+cell.getStrength()+"</td>");
	    writer.print("<td>"+((OWLEntity)cell.getObject2()).getURI().toString() +"</td></tr>");
	    //if ( !cell.getSemantics().equals("first-order") )
	    //	writer.print("      <semantics>"+cell.getSemantics()+"</semantics>\n");
	} catch ( OWLException e) { throw new AlignmentException( "getURI problem", e ); }
    }
    public void visit( Relation rel ) {
	rel.write( writer );
    };
}
