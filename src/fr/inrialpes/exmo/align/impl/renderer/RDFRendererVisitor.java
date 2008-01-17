/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2006, 2007-2008
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
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

// JE: this has been introduced here for the sole purpose of
// using the namespace facility of BasicAlignment
import fr.inrialpes.exmo.align.impl.BasicAlignment;

//import org.omwg.mediation.language.export.omwg.OmwgSyntaxFormat;
import org.omwg.mediation.language.export.rdf.RdfSyntaxFormat;
import org.omwg.mediation.parser.alignment.NamespaceDefs;
import org.omwg.mediation.language.objectmodel.api.Expression;

// JE: we need jena... only for this renderer!
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Renders an alignment in its RDF format
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class RDFRendererVisitor implements AlignmentVisitor
{
    
    PrintWriter writer = null;
    Alignment alignment = null;
    Cell cell = null;
    //OmwgSyntaxFormat oMWGformatter = null;
    RdfSyntaxFormat oMWGformatter = null;
    Model model = null;

    public RDFRendererVisitor( PrintWriter writer ){
	this.writer = writer;
	Model model = ModelFactory.createDefaultModel();
    }

    public void visit( Alignment align ) throws AlignmentException {
	alignment = align;
	writer.print("<?xml version='1.0' encoding='utf-8");
	writer.print("' standalone='no'?>\n");
	writer.print("<rdf:RDF xmlns='http://knowledgeweb.semanticweb.org/heterogeneity/alignment'\n         xml:align='http://knowledgeweb.semanticweb.org/heterogeneity/alignment'\n         xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n         xmlns:xsd='http://www.w3.org/2001/XMLSchema#'\n");
	if ( align instanceof BasicAlignment ) {
	    for ( Enumeration e = ((BasicAlignment)align).getXNamespaces().getNames() ; e.hasMoreElements(); ){
	    String label = (String)e.nextElement();
	    if ( !label.equals("rdf") && !label.equals("xsd")
		 && !label.equals("<default>") ) 
		writer.print("         xmlns:"+label+"='"+((BasicAlignment)align).getXNamespace( label )+"'\n");
	    }
	}
	writer.print(">\n");
	writer.print("<Alignment");
	if ( align.getExtension("id") != null ) {
	    writer.print(" rdf:about=\""+align.getExtension("id")+"\"");
	}
	writer.print(">\n  <xml>yes</xml>\n");
	writer.print("  <level>");
	writer.print( align.getLevel() );
	if ( align.getLevel().equals("2OMWG") ) {
	    //oMWGformatter = new OmwgSyntaxFormat();
	    oMWGformatter = new RdfSyntaxFormat();
	    // This is a trick for having namespaces output
	    //oMWGformatter.setDefaultNamespace( NamespaceDefs.ALIGNMENT );
	}
	writer.print("</level>\n  <type>");
	writer.print( align.getType() );
	writer.print("</type>\n");
	// Get the keys of the parameter
	for( Enumeration e = align.getExtensions().getNames() ; e.hasMoreElements() ; ){
	    String tag = (String)e.nextElement();
	    // Here I should reverse namespace
	    writer.print("  <"+tag+">"+align.getExtension(tag)+"</"+tag+">\n");
	}
	// JE: real new version of the format...
	/*
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
	*/
	writer.print("  <onto1>\n    <Ontology");
	if ( align.getOntology1URI() != null ) {
	    writer.print(" rdf:about=\""+align.getOntology1URI()+"\">");
	}
	writer.print("\n      <location>"+align.getFile1()+"</location>");
	if ( align instanceof BasicAlignment && ((BasicAlignment)align).getOntologyObject1().getFormalism() != null ) {
	    writer.print("\n      <formalism>\n        <Formalism align:name=\""+((BasicAlignment)align).getOntologyObject1().getFormalism()+"\" align:uri=\""+((BasicAlignment)align).getOntologyObject1().getFormURI()+"\"/>\n      </formalism>");
	    
	}
	writer.print("\n    </Ontology>\n  </onto1>\n");
	writer.print("  <onto2>\n    <Ontology");
	if ( align.getOntology2URI() != null ) {
	    writer.print(" rdf:about=\""+align.getOntology2URI()+"\">");
	}
	writer.print("\n      <location>"+align.getFile2()+"</location>");
	if ( align instanceof BasicAlignment && ((BasicAlignment)align).getOntologyObject2().getFormalism() != null ) {
	    writer.print("\n      <formalism>\n        <Formalism name=\""+((BasicAlignment)align).getOntologyObject2().getFormalism()+"\" uri=\""+((BasicAlignment)align).getOntologyObject2().getFormURI()+"\"/>\n      </formalism>");
	    
	}
	writer.print("\n    </Ontology>\n  </onto2>\n");
	for( Enumeration e = align.getElements() ; e.hasMoreElements(); ){
	    Cell c = (Cell)e.nextElement();
	    c.accept( this );
	} //end for
	writer.print("</Alignment>\n");
	writer.print("</rdf:RDF>\n");
    }
    public void visit( Cell cell ) throws AlignmentException {
	this.cell = cell;
	if ( ( cell.getObject1AsURI() != null &&
	       cell.getObject2AsURI() != null) ||
	       alignment.getLevel().equals("2OMWG") ){
	    writer.print("  <map>\n");
	    writer.print("    <Cell");
	    if ( cell.getId() != null ){
		writer.print(" rdf:about=\""+cell.getId()+"\"");
	    }
	    writer.print(">\n");
	    // Would be better to put it more generic
	    // But this should be it! (at least for this one)
	    if ( alignment.getLevel().equals("2OMWG") ) {
		writer.print("      <entity1>");
		//writer.print( oMWGformatter.export( (Expression)cell.getObject1() ) );
		writer.print( oMWGformatter.getExprNode( (Expression)cell.getObject1(), model ) );
		writer.print("</entity1>\n      <entity2>");
		//writer.print( oMWGformatter.export( (Expression)cell.getObject2() ) );
		writer.print( oMWGformatter.getExprNode( (Expression)cell.getObject2(), model ) );
		writer.print("</entity2>\n      <relation>");
		//writer.print(cell.getRelation().getRelation());
		cell.getRelation().accept( this );
		writer.print("</relation>\n");
	    } else {
		writer.print("      <entity1 rdf:resource='");
		writer.print( cell.getObject1AsURI().toString() );
		writer.print("'/>\n      <entity2 rdf:resource='");
		writer.print( cell.getObject2AsURI().toString() );
		writer.print("'/>\n      <relation>");
		cell.getRelation().accept( this );
		writer.print("</relation>\n");
	    }
	    writer.print("      <measure rdf:datatype='http://www.w3.org/2001/XMLSchema#float'>");
	    writer.print( cell.getStrength() );
	    writer.print("</measure>\n");
	    if ( cell.getSemantics() != null &&
		 !cell.getSemantics().equals("") &&
		 !cell.getSemantics().equals("first-order") )
		writer.print("      <semantics>"+cell.getSemantics()+"</semantics>\n");
	    if ( cell.getExtensions() != null ) {
		// could certainly be done better
		for ( Enumeration e = cell.getExtensions().getNames() ; e.hasMoreElements(); ){
		    String label = (String)e.nextElement();
		    // Here I should reverse namespace
		    writer.print("      <"+label+">"+cell.getExtension(label)+"</"+label+">\n");
		}
	    }
	    writer.print("    </Cell>\n  </map>\n");
	}
    }
    public void visit( Relation rel ) {
	rel.write( writer );
    };
}
