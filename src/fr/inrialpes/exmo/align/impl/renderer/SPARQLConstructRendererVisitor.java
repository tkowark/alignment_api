/*
 * $Id$
 *
 * Copyright (C) INRIA, 2012-2014
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

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.impl.edoal.EDOALCell;
import fr.inrialpes.exmo.align.impl.edoal.Expression;
import fr.inrialpes.exmo.align.impl.edoal.Transformation;

import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class SPARQLConstructRendererVisitor extends GraphPatternRendererVisitor implements AlignmentVisitor {

    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String,String> nslist = null;

    boolean embedded = false;

    boolean split = false;                                  // split each query in a file
    String splitdir = "";                                   // directory where to put query files

    private String content_Corese = "";                     // resulting string for Corese
    
    public SPARQLConstructRendererVisitor(PrintWriter writer) {
	super(writer);
    }

    /**
     * Initialises the parameters of the renderer
     */
    public void init(Properties p) {
	if ( p.getProperty( "embedded" ) != null 
	     && !p.getProperty( "embedded" ).equals("") ) embedded = true;
	if ( p.getProperty( "blanks" ) != null && !p.getProperty( "blanks" ).equals("") ) 
	    blanks = true;
	if ( p.getProperty( "weakens" ) != null && !p.getProperty( "weakens" ).equals("") ) 
	    weakens = true;
	if ( p.getProperty( "ignoreerrors" ) != null && !p.getProperty( "ignoreerrors" ).equals("") ) 
	    ignoreerrors = true;
	if ( p.getProperty( "corese" ) != null && !p.getProperty( "corese" ).equals("") ) 
	    corese = true;
	split = ( p.getProperty( "split" ) != null && !p.getProperty( "split" ).equals("") );
	if ( p.getProperty( "dir" ) != null && !p.getProperty( "dir" ).equals("") )
	    splitdir = p.getProperty( "dir" )+"/";
	if ( p.getProperty( "indent" ) != null ) INDENT = p.getProperty( "indent" );
	if ( p.getProperty( "newline" ) != null ) NL = p.getProperty( "newline" );
    }
    
    public void visit( Alignment align ) throws AlignmentException {
    	if ( subsumedInvocableMethod( this, align, Alignment.class ) ) return;
	if ( align instanceof EDOALAlignment ) {
	    alignment = align;
	} else {
	    try {
		alignment = EDOALAlignment.toEDOALAlignment( (BasicAlignment)align );
	    } catch ( AlignmentException alex ) {
		throw new AlignmentException("SPARQLSELECTRenderer: cannot render simple alignment. Need an EDOALAlignment", alex );
	    }
	}
    	content_Corese = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL;
    	content_Corese += "<!DOCTYPE rdf:RDF [" + NL;
    	content_Corese += "<!ENTITY rdf \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" + NL;
    	content_Corese += "<!ENTITY rdfs \"http://www.w3.org/2000/01/rdf-schema#\">" + NL;
    	content_Corese += "<!ENTITY rul \"http://ns.inria.fr/edelweiss/2011/rule#\">" + NL;
    	content_Corese += "]>" + NL;
    	content_Corese += "<rdf:RDF xmlns:rdfs=\"&rdfs;\" xmlns:rdf=\"&rdf;\" xmlns = \'&rul;\' >" + NL + NL + NL;
    	for( Cell c : alignment ){ c.accept( this ); };
    	content_Corese += "</rdf:RDF>" + NL;
    	if ( corese ) {
	    if( split ) {			
		createQueryFile( splitdir, content_Corese );
	    } else {
		writer.println( content_Corese );
	    }    	
    	}
    }	
    
    public void visit( Cell cell ) throws AlignmentException {
    	if ( subsumedInvocableMethod( this, cell, Cell.class ) ) return;
    	// default behaviour
    	this.cell = cell;
    	URI u1 = cell.getObject1AsURI( alignment );
    	URI u2 = cell.getObject2AsURI( alignment );
    	if ( ( u1 != null && u2 != null)
    	     || alignment.getLevel().startsWith("2EDOAL") ){ //expensive test
	    // Here the generation is dependent on global variables
	    resetVariables("s", "o");
	    ((Expression)(cell.getObject1())).accept( this );
	    String GP1 = getGP();    		
	    List<String> listGP1 = new ArrayList<String>(getBGP());    		
	    resetVariables("s", "o");	    		
	    ((Expression)(cell.getObject2())).accept( this );
	    String GP2 = getGP();
	    List<String> listGP2 = new ArrayList<String>(getBGP());
	    // End of global variables
	    
	    generateConstruct( GP1, listGP1, GP2, listGP2 );
	    generateConstruct( GP2, listGP2, GP1, listGP1 );
    	}
    }
    
    public void visit( Relation rel ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, rel, Relation.class ) ) return;
	// default behaviour
	// rel.write( writer );
    }

    protected void generateConstruct( String GP1, List<String> listGP1, String GP2, List<String> listGP2 ) {
    	String query = "";
	if ( !GP1.contains("UNION") && !GP1.contains("FILTER") ) {
	    query = createConstruct( GP1, GP2 );
	    if ( corese ) content_Corese += createCoreseQuery( query );
	} else if ( weakens ) {		
	    String tmp = "";
	    for ( String str : listGP1 ) {
		if ( !str.contains("UNION") && !str.contains("FILTER") ) tmp += str;
	    }
	    if ( !tmp.equals("") ) query = createConstruct( tmp, GP2 );
	} else if ( ignoreerrors ) {
	    query = createConstruct( GP1, GP2 );
	}
	if ( corese ) return;
	if ( split ) {
	    createQueryFile( splitdir, query );
	} else {
	    writer.println( query );
	}
    }

    protected String createConstruct( String GP1, String GP2 ) {
	return createPrefix( prefixList )+"CONSTRUCT {"+NL+GP1+"}"+NL+"WHERE {"+NL+GP2+"}"+NL;
    }

    protected String createCoreseQuery( String query ) {
	return "<rule>"+NL+"<body>"+NL+"<![CDATA["+NL+query+"]]>"+NL+"</body>"+NL+"</rule>"+NL+NL;
    }

    protected String createPrefix( Hashtable<String,String> preflist ) {
	String result = "";
	for ( String k : preflist.keySet() ) {
	    result += "PREFIX "+preflist.get(k)+":<"+k+">"+NL;
	}
	return result;
    }

}
