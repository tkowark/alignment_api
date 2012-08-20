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

import fr.inrialpes.exmo.align.impl.edoal.Expression;
import java.io.PrintWriter;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

public class SPARQLConstructRendererVisitor extends GraphPatternRendererVisitor implements AlignmentVisitor{


    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String,String> nslist = null;
    boolean embedded = false;
    boolean split = false;
    String splitdir = "";
		
    private List<String> listBGP1;
    private List<String> listBGP2;
	 
    private List<String> listCond1;
    private List<String> listCond2;
    
	public SPARQLConstructRendererVisitor(PrintWriter writer) {
		super(writer);
	}   
	
	public SPARQLConstructRendererVisitor(PrintWriter writer, String sub, String pred, String obj) {
		super(writer);
		
	}

	public void init(Properties p) {
	    if ( p.getProperty( "embedded" ) != null 
		 && !p.getProperty( "embedded" ).equals("") ) embedded = true;
	    split = ( p.getProperty( "split" ) != null && !p.getProperty( "split" ).equals("") );
	    if ( p.getProperty( "dir" ) != null && !p.getProperty( "dir" ).equals("") )
		splitdir = p.getProperty( "dir" )+"/";
	    if ( p.getProperty( "indent" ) != null )
		INDENT = p.getProperty( "indent" );
	    if ( p.getProperty( "newline" ) != null )
		NL = p.getProperty( "newline" );
	}

	public void visit(Alignment align) throws AlignmentException {

    	if ( subsumedInvocableMethod( this, align, Alignment.class ) ) return;

    	alignment = align;    	
    	
    	for( Cell c : align ){ c.accept( this ); };
    	
	}	

	public void visit(Cell cell) throws AlignmentException {
		
    	if ( subsumedInvocableMethod( this, cell, Cell.class ) ) return;
    	// default behaviour
    	this.cell = cell;      	
    	String query="";
    	URI u1 = cell.getObject1AsURI(alignment);
    	URI u2 = cell.getObject2AsURI(alignment);
    	if ( ( u1 != null && u2 != null)
    	     || alignment.getLevel().startsWith("2EDOAL") ){ //expensive test
    	        	    	
	    	resetVariables("s", "o");
    		((Expression)(cell.getObject1())).accept( this );
    		listBGP1 = new ArrayList<String>(getBGP());
    		listCond1 = new ArrayList<String>(getCondition());
    			    			    		
    		resetVariables("s", "o");	    		
    		((Expression)(cell.getObject2())).accept( this );
    		listBGP2 = new ArrayList<String>(getBGP());
    		listCond2 = new ArrayList<String>(getCondition());
    		
		if(!listBGP1.get(listBGP1.size()-1).contains("UNION") &&
		   !listBGP1.get(listBGP1.size()-1).contains("FILTER") &&
		   !listBGP1.get(listBGP1.size()-1).contains("MINUS")){
		    for ( Enumeration e = prefixList.keys() ; e.hasMoreElements(); ) {
			String k = (String)e.nextElement();
			query += "PREFIX "+prefixList.get(k)+":<"+k+">"+NL;
		    }
		    query += "CONSTRUCT {"+NL;
		    query += listBGP1.get(listBGP1.size()-1)+NL;
		    
		    query += "}"+NL;
		    query += "WHERE {"+NL;
		    query += listBGP2.get(listBGP2.size()-1)+NL;    	    		
		    query += "}"+NL;
		    if( split ) {
			createQueryFile( splitdir, query );
		    } else {
			indentedOutputln(query);
		    }
		}
		query="";
    		
		/*
    		if(!listBGP2.get(listBGP2.size()-1).contains("UNION") &&
		   !listBGP2.get(listBGP2.size()-1).contains("FILTER") &&
		   !listBGP2.get(listBGP2.size()-1).contains("MINUS")){
		    for ( Enumeration e = prefixList.keys() ; e.hasMoreElements(); ) {
			String k = (String)e.nextElement();
			query += "PREFIX "+prefixList.get(k)+":<"+k+">"+NL;
		    }
		    query += "CONSTRUCT {"+NL;
		    query += listBGP2.get(listBGP2.size()-1)+NL;
		    
		    query += "}"+NL;
		    query += "WHERE {"+NL;
		    query += listBGP1.get(listBGP1.size()-1)+NL;    	    		
		    query += "}"+NL;
		    if(System.getProperty("Split")=="true")
			createQueryFile(query);
		    else
			indentedOutputln(query);
		}    		   
		*/	       		    	
    	}
	}

	public void visit( Relation rel ) throws AlignmentException {
		if ( subsumedInvocableMethod( this, rel, Relation.class ) ) return;
		// default behaviour
		// rel.write( writer );
	}
	
}
