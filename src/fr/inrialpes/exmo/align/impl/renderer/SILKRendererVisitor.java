/*
 * $Id$
 *
 * Copyright (C) INRIA, 2012
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

import java.io.PrintWriter;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.edoal.Expression;

import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class SILKRendererVisitor extends GraphPatternRendererVisitor implements AlignmentVisitor{

    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String,String> nslist = null;
    private boolean embedded = false;
    private boolean ignoreerrors = false;
    private boolean blanks = false;
    private boolean weakens = false;
	
    private static Namespace DEF = Namespace.ALIGNMENT;
	
    private List<String> listBGP1;
    private List<String> listBGP2;
	 
    private List<String> listCond1;
    private List<String> listCond2;
    
    public SILKRendererVisitor(PrintWriter writer) {
	super(writer);
    }   
	
    public SILKRendererVisitor(PrintWriter writer, String sub, String pred, String obj) {
	super(writer);
    }

    public void init( Properties p ) {
	if ( p.getProperty( "embedded" ) != null && !p.getProperty( "embedded" ).equals("") ) 
	    embedded = true;
	if ( p.getProperty( "blanks" ) != null && !p.getProperty( "blanks" ).equals("") ) 
	    blanks = true;
	if ( p.getProperty( "weakens" ) != null && !p.getProperty( "weakens" ).equals("") ) 
	    weakens = true;
	if ( p.getProperty( "ignoreerrors" ) != null && !p.getProperty( "ignoreerrors" ).equals("") ) 
	    ignoreerrors = true;
	if ( p.getProperty( "indent" ) != null )
	    INDENT = p.getProperty( "indent" );
	if ( p.getProperty( "newline" ) != null )
	    NL = p.getProperty( "newline" );
    }

    public void visit(Alignment align) throws AlignmentException {

    	if ( subsumedInvocableMethod( this, align, Alignment.class ) ) return;
    	// default behaviour
    	String extensionString = "";
    	alignment = align;
    	nslist = new Hashtable<String,String>();
	nslist.put( Namespace.RDF.prefix , Namespace.RDF.shortCut );
	nslist.put( Namespace.XSD.prefix , Namespace.XSD.shortCut );
    	// Get the keys of the parameter
    	int gen = 0;
    	for ( String[] ext : align.getExtensions() ) {
    	    String prefix = ext[0];
    	    String name = ext[1];
    	    String tag = nslist.get(prefix);
    	    //if ( tag.equals("align") ) { tag = name; }
    	    if ( prefix.equals( Namespace.ALIGNMENT.uri ) ) { tag = name; }
    	    else {
    		if ( tag == null ) {
    		    tag = "ns"+gen++;
    		    nslist.put( prefix, tag );
    		}
    		tag += ":"+name;
    	    }
    	    extensionString += INDENT+"<"+tag+">"+ext[2]+"</"+tag+">"+NL;
    	}
    	if ( embedded == false ) {
    	    writer.print("<?xml version='1.0' encoding='utf-8");
    	    writer.print("' standalone='no'?>"+NL);
    	}
    	indentedOutputln("<SILK>");
	increaseIndent();
    	indentedOutputln("<Prefixes>");
	increaseIndent();
    	indentedOutputln("<Prefix id=\"rdf\" namespace=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" />");
    	indentedOutputln("<Prefix id=\"rdfs\" namespace=\"http://www.w3.org/2000/01/rdf-schema#\" />");
    	indentedOutputln("<Prefix id=\"owl\" namespace=\"http://www.w3.org/2002/07/owl#\" />");
    	for ( Enumeration e = prefixList.keys() ; e.hasMoreElements(); ) {
		    String k = (String)e.nextElement();
		    indentedOutputln("<Prefix id=\""+k+" namespace=\""+prefixList.get(k)+"\" />");
	}
	decreaseIndent();
    	indentedOutputln("</Prefixes>"+NL);
    	indentedOutputln("<DataSources>");
	increaseIndent();
	decreaseIndent();
    	indentedOutputln("</DataSources>"+NL);
    	indentedOutputln("<Interlinks>");
	increaseIndent();
    	for( Cell c : align ){ c.accept( this ); };
    	decreaseIndent();
    	indentedOutputln("</Interlinks>");
    	decreaseIndent();
    	writer.print("</SILK>"+NL);
    }	
	
    public void visit(Cell cell) throws AlignmentException {
    	if ( subsumedInvocableMethod( this, cell, Cell.class ) ) return;
    	// default behaviour
    	this.cell = cell;      	

	// JE: must be improved because this is an URI
	String id = cell.getId();
	if ( id == null || id.equals("") ){
	    Random rand = new Random(System.currentTimeMillis());
	    id = "RandomId"+Math.abs(rand.nextInt(1000));
	}
    	
    	URI u1 = cell.getObject1AsURI(alignment);
    	URI u2 = cell.getObject2AsURI(alignment);
    	if ( ( u1 != null && u2 != null)
    	     || alignment.getLevel().startsWith("2EDOAL") ){ //expensive test
    	    
	    // JE: why this test?
    	    if ( alignment.getLevel().startsWith("2EDOAL") ) {
    	    	
    	    	resetVariables("s", "o");
		((Expression)(cell.getObject1())).accept( this );
	    		
		List<String> tempList = new ArrayList<String>(getBGP());
		listBGP1 = new ArrayList<String>(tempList);
		tempList = new ArrayList<String>(getCondition());
		listCond1 = new ArrayList<String>(tempList);
	    	
		resetVariables("x", "y");	    		
		((Expression)(cell.getObject2())).accept( this );
		tempList = new ArrayList<String>(getBGP());
		listBGP2 = new ArrayList<String>(tempList);
		tempList = new ArrayList<String>(getCondition());
		listCond2 = new ArrayList<String>(tempList);
	    	
		// JE: The link id should be either the cell id or a gensym
		indentedOutputln("<Interlink id=\""+id+"\">");
		increaseIndent();
		indentedOutputln("<LinkType>owl:sameAs</LinkType>");
		indentedOutputln("<SourceDataSet datasource=\"\"" + " var=\"s\">");
		increaseIndent();
		indentedOutputln("<RestrictTo>");
		
		indentedOutput(listBGP1.get(listBGP1.size()-1));
	    	
		indentedOutputln("</RestrictTo>");
		decreaseIndent();
		indentedOutputln("</SourceDataSet>");
		
		indentedOutputln("<TargetDataSet datasource=\"\"" + " var=\"x\">");
		increaseIndent();
		indentedOutputln("<RestrictTo>");

		indentedOutput(listBGP2.get(listBGP2.size()-1));
	    		
		indentedOutputln("</RestrictTo>");
		decreaseIndent();
		indentedOutputln("</TargetDataSet>"+NL);

		indentedOutputln("<LinkageRule>");
		increaseIndent();
		decreaseIndent();
		indentedOutputln("</LinkageRule>");
		indentedOutputln("<Filter />");
		indentedOutputln("<Outputs>");	    		
		increaseIndent();
		indentedOutputln("<Output type=\"file\">");
		increaseIndent();
		// JE: The file name should be in function of the link-id
		indentedOutputln("<Param name=\"file\" value=\""+id+".nt\"/>");
		indentedOutputln("<Param name=\"format\" value=\"ntriples\"/>");
		decreaseIndent();
		indentedOutputln("</Output>");
		decreaseIndent();
		indentedOutputln("</Outputs>");
		decreaseIndent();
		indentedOutputln("</Interlink>"+NL);

    	    }    		    		
    	}
    }

    public void visit( Relation rel ) throws AlignmentException {
		if ( subsumedInvocableMethod( this, rel, Relation.class ) ) return;
		// default behaviour
		// rel.write( writer );
    }
	
}
