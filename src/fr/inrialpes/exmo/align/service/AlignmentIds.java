/*
 * $Id$
 *
 * Copyright (C) INRIA, 2006-2009
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package fr.inrialpes.exmo.align.service;

import org.semanticweb.owl.align.Parameters;

/**
 * Contains the messages that should be sent according to the protocol
 */

public class AlignmentIds extends Success {

    String pretty = null;

    public AlignmentIds ( int surr, Message rep, String from, String to, String cont, Parameters param ) {
	super( surr, rep, from, to, cont, param );
    }
    public AlignmentIds ( int surr, Message rep, String from, String to, String cont, Parameters param, String pretty ) {
	super( surr, rep, from, to, cont, param );
	this.pretty = pretty;
    }
    public String HTMLString(){
	String result = "Alignment Ids: <ul>";
	String id[] = content.split(" ");
	String pid[] = pretty.split(":");
 
	for ( int i = id.length-1; i >= 0; i-- ){
		//System.err.println("id["+i+"]"+id[i]);
	    result += "<li><a href=\"../html/retrieve?method=fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor&id="+id[i]+"\">";
	    result += id[i];
	    String pp = null;
	    if ( pid != null ) {
	    	try {
			pp = pid[i];
	    	        if (pp != null && !pp.equals("") && !pp.equals("null"))  {		
				result += " ("+pp+")";
			}
			//System.err.println("pid["+i+"]="+pp);
		} catch(Exception ex) { 
		}
	    } 
		
	    result += "</a></li>";
	}
	return result += "</ul>";
    }

    public String HTMLRESTString(){
	String result = "Alignment Ids: <ul>";
	String id[] = content.split(" ");
	for ( int i = id.length-1; i >= 0; i-- ){
	    result += "<li><a href=\"../rest/retrieve?method=fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor&id="+id[i]+"\">"+id[i]+"</a>";
	    result += "<table><tr>";
result += "<td><form action=\"getID\"><input type=\"hidden\" name=\"id\" value=\""+id[i]+"\"/><input type=\"submit\" name=\"action\" value=\"GetID\"  disabled=\"disabled\"/></form></td>";
result += "<td><form action=\"metadata\"><input type=\"hidden\" name=\"id\" value=\""+id[i]+"\"/><input type=\"submit\" name=\"action\" value=\"Metadata\"/></form></td>";
	    result += "</li>";
	}
	return result += "</ul>";
    }

    public String RESTString(){
	String msg = "<alignmentList>";
	String id[] = content.split(" ");
	for ( int i = id.length-1; i >= 0; i-- ){
	    if ( id[i].trim() != "" ) {
		msg += "<alid>"+id[i].trim()+"</alid>";
	    }
	}	
	msg += "</alignmentList>";
	return msg;
    }
}
