/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2007
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

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

/**
 * Renders an alignment in its RDF format
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class XMLMetadataRendererVisitor implements AlignmentVisitor
{
    
    PrintWriter writer = null;

    public XMLMetadataRendererVisitor( PrintWriter writer ){
	this.writer = writer;
    }

    public void visit( Alignment align ) throws AlignmentException {
	writer.print("<?xml version='1.0' encoding='utf-8' standalone='yes'?>\n");
	writer.println("<Alignment>");
	writer.print("  <level>");
	writer.print( align.getLevel() );
	writer.print("</level>\n  <type>");
	writer.print( align.getType() );
	writer.print("</type>\n");
	// Get the keys of the parameter
	if ( align.getFile1() != null )
	    writer.print("  <onto1>"+align.getFile1().toString()+"</onto1>\n");
	if ( align.getFile2() != null )
	    writer.print("  <onto2>"+align.getFile2().toString()+"</onto2>\n");
	writer.print("  <uri1>");
	writer.print( align.getOntology1URI().toString() );
	writer.print("</uri1>\n");
	writer.print("  <uri2>");
	writer.print( align.getOntology2URI().toString() );
	writer.print("</uri2>\n");
	Parameters extensions = align.getExtensions();
	for ( Enumeration e = extensions.getNames(); e.hasMoreElements();) {
	    String tag = (String)e.nextElement();
	    writer.println("  <"+tag+">"+extensions.getParameter(tag)+"</"+tag+">");
	}
	writer.println("</Alignment>");
    }

    public void visit( Cell c ) {}
    public void visit( Relation r ) {}
}
