/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2008
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
import java.util.Hashtable;
import java.io.PrintWriter;
import java.net.URI;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectCell;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.onto.LoadedOntology;

import org.omwg.mediation.language.export.omwg.OmwgSyntaxFormat;
import org.omwg.mediation.parser.alignment.NamespaceDefs;
import org.omwg.mediation.language.objectmodel.api.Expression;


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
    OmwgSyntaxFormat oMWGformatter = null;
    Hashtable<String,String> nslist = null;
    boolean embedded = false; // if the output is XML embeded in a structure

    public RDFRendererVisitor( PrintWriter writer ){
	this.writer = writer;
    }

    public void init( Parameters p ) {
	if ( p.getParameter( "embedded" ) != null 
	     && !p.getParameter( "embedded" ).equals("") ) embedded = true;
    }

    public void visit( Alignment align ) throws AlignmentException {
	String extensionString = "";
	alignment = align;
	nslist = new Hashtable<String,String>();
	nslist.put(Annotations.ALIGNNS,"align");
	nslist.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf");
	nslist.put("http://www.w3.org/2001/XMLSchema#","xsd");
	//nslist.put("http://www.omwg.org/TR/d7/ontology/alignment","omwg");
	// Get the keys of the parameter
	int gen = 0;
	for ( Object ext : ((BasicParameters)align.getExtensions()).getValues() ){
	    String prefix = ((String[])ext)[0];
	    String name = ((String[])ext)[1];
	    String tag = (String)nslist.get(prefix);
	    if ( tag == null ) {
		tag = "ns"+gen++;
		nslist.put( prefix, tag );
	    }
	    if ( tag.equals("align") ) { tag = name; }
	    else { tag += ":"+name; }
	    extensionString += "  <"+tag+">"+((String[])ext)[2]+"</"+tag+">\n";
	}
	if ( embedded == false ) {
	    writer.print("<?xml version='1.0' encoding='utf-8");
	    writer.print("' standalone='no'?>\n");
	}
	writer.print("<rdf:RDF xmlns='"+Annotations.ALIGNNS+"'");
	for ( Enumeration e = nslist.keys() ; e.hasMoreElements(); ) {
	    String k = (String)e.nextElement();
	    writer.print("\n         xmlns:"+nslist.get(k)+"='"+k+"'");
	}
	if ( align instanceof BasicAlignment ) {
	    for ( Enumeration e = ((BasicAlignment)align).getXNamespaces().getNames() ; e.hasMoreElements(); ){
	    String label = (String)e.nextElement();
	    if ( !label.equals("rdf") && !label.equals("xsd")
		 && !label.equals("<default>") )
		writer.print("\n         xmlns:"+label+"='"+((BasicAlignment)align).getXNamespace( label )+"'");
	    }
	}
	writer.print(">\n");
	writer.print("<Alignment");
	String idext = align.getExtension( Annotations.ALIGNNS, Annotations.ID );
	if ( idext != null ) {
	    writer.print(" rdf:about=\""+idext+"\"");
	}
	writer.print(">\n  <xml>yes</xml>\n");
	writer.print("  <level>");
	writer.print( align.getLevel() );
	if ( align.getLevel().equals("2OMWG") ) {
	    oMWGformatter = new OmwgSyntaxFormat( true, "  " );
	    oMWGformatter.setDefaultNamespace( NamespaceDefs.ALIGNMENT );
	    // Set the offset at which element starts (fortunately always the same)
	    oMWGformatter.setPrefixCount(4);
	}
	writer.print("</level>\n  <type>");
	writer.print( align.getType() );
	writer.print("</type>\n");
	writer.print(extensionString);
	writer.print("  <onto1>\n    <Ontology");
	if ( align.getOntology1URI() != null ) {
	    writer.print(" rdf:about=\""+align.getOntology1URI()+"\"");
	}
	writer.print(">\n      <location>"+align.getFile1()+"</location>");
	if ( align instanceof BasicAlignment && ((BasicAlignment)align).getOntologyObject1().getFormalism() != null ) {
	    writer.print("\n      <formalism>\n        <Formalism align:name=\""+((BasicAlignment)align).getOntologyObject1().getFormalism()+"\" align:uri=\""+((BasicAlignment)align).getOntologyObject1().getFormURI()+"\"/>\n      </formalism>");

	}
	writer.print("\n    </Ontology>\n  </onto1>\n");
	writer.print("  <onto2>\n    <Ontology");
	if ( align.getOntology2URI() != null ) {
	    writer.print(" rdf:about=\""+align.getOntology2URI()+"\"");
	}
	writer.print(">\n      <location>"+align.getFile2()+"</location>");
	if ( align instanceof BasicAlignment && ((BasicAlignment)align).getOntologyObject2().getFormalism() != null ) {
	    writer.print("\n      <formalism>\n        <Formalism align:name=\""+((BasicAlignment)align).getOntologyObject2().getFormalism()+"\" align:uri=\""+((BasicAlignment)align).getOntologyObject2().getFormURI()+"\"/>\n      </formalism>");
	}
	writer.print("\n    </Ontology>\n  </onto2>\n");
	for( Cell c : align ){ c.accept( this ); };
	writer.print("</Alignment>\n");
	writer.print("</rdf:RDF>\n");
    }
    public void visit( Cell cell ) throws AlignmentException {
	this.cell = cell;
	URI u1 = cell.getObject1AsURI(alignment);
	URI u2 = cell.getObject2AsURI(alignment);
	if ( ( u1 != null && u2 != null)
	     || alignment.getLevel().equals("2OMWG") ){
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
		writer.print( oMWGformatter.export( (Expression)cell.getObject1() ) );
		writer.print("\n      </entity1>\n      <entity2>");
		writer.print( oMWGformatter.export( (Expression)cell.getObject2() ) );
		writer.print("\n      </entity2>\n");
	    } else {
		writer.print("      <entity1 rdf:resource='");
		writer.print( u1.toString() );
		writer.print("'/>\n      <entity2 rdf:resource='");
		writer.print( u2.toString() );
		writer.print("'/>\n");
	    }
	    writer.print("      <relation>");
	    cell.getRelation().accept( this );
	    writer.print("</relation>\n");
	    writer.print("      <measure rdf:datatype='http://www.w3.org/2001/XMLSchema#float'>");
	    writer.print( cell.getStrength() );
	    writer.print("</measure>\n");
	    if ( cell.getSemantics() != null &&
		 !cell.getSemantics().equals("") &&
		 !cell.getSemantics().equals("first-order") )
		writer.print("      <semantics>"+cell.getSemantics()+"</semantics>\n");
	    if ( cell.getExtensions() != null ) {
		// could certainly be done better
		for ( Object ext : ((BasicParameters)cell.getExtensions()).getValues() ){
		    String uri = ((String[])ext)[0];
		    String tag = (String)nslist.get( uri );
		    if ( tag == null ){
			tag = ((String[])ext)[1];
		    } else {
			tag += ":"+((String[])ext)[1];
		    }
		    // JE: Bug: namespace may not have been declared!
		    // Here I should reverse namespace
		    writer.print("      <"+tag+">"+((String[])ext)[2]+"</"+tag+">\n");
		}
	    }
	    writer.print("    </Cell>\n  </map>\n");
	}
    }
    public void visit( Relation rel ) {
	rel.write( writer );
    };

}
