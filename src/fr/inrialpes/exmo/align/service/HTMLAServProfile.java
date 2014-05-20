/*
 * $Id$
 *
 * Copyright (C) INRIA, 2006-2014
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

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.BasicOntologyNetwork;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.service.msg.Message;
import fr.inrialpes.exmo.align.service.msg.ErrorMsg;
import fr.inrialpes.exmo.align.service.msg.AlignmentId;
import fr.inrialpes.exmo.align.service.msg.AlignmentIds;
import fr.inrialpes.exmo.align.service.msg.EvaluationId;
import fr.inrialpes.exmo.align.service.msg.OntologyNetworkId;
import fr.inrialpes.exmo.align.service.msg.UnknownOntologyNetwork;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.OntologyNetwork;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.net.URI;
import java.net.URISyntaxException;
import java.lang.Integer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTMLAServProfile: an HTML provile for the Alignment server
 */

public class HTMLAServProfile implements AlignmentServiceProfile {
    final static Logger logger = LoggerFactory.getLogger( HTMLAServProfile.class );

    private AServProtocolManager manager;

    private String serverURL;
    private int localId = 0;

    public static final int MAX_FILE_SIZE = 10000;

    public static final String HEADER = "<style type=\"text/css\">body { font-family: sans-serif } button {background-color: #DDEEFF; margin-left: 1%; border: #CCC 1px solid;}</style>";

    public void init( Properties params, AServProtocolManager manager ) throws AServException {
	this.manager = manager;
	serverURL = manager.serverURL()+"/html/";
    }

    public boolean accept( String prefix ) {
	if ( prefix.equals("admin") || prefix.equals("html") || prefix.equals("ontonet")) return true;
	else return false;
    }

    public String process( String uri, String prefix, String perf, Properties header, Properties params ) {
	if ( prefix.equals("html") ) {
	    return htmlAnswer( uri, perf, header, params );
	} else if ( prefix.equals("admin") ) {
	    return adminAnswer( uri, perf, header, params );
	} else if ( prefix.equals("ontonet") ) {
	    return ontologyNetworkAnswer( uri, perf, header, params );
	}else return about();
    }

    public void close(){
    }
    
    // ==================================================
    // API parts
    // ==================================================

    protected static String about() {
	return "<h1>Alignment server</h1><center>"+AlignmentService.class.getPackage().getImplementationTitle()+" "+AlignmentService.class.getPackage().getImplementationVersion()+"<br />"
	    + "<center><a href=\"html/\">Access</a></center>"
	    + "(C) INRIA, 2006-2014<br />"
	    + "<a href=\"http://alignapi.gforge.inria.fr\">http://alignapi.gforge.inria.fr</a><br />"
	    + "</center>";
    }

    /**
     * HTTP administration interface
     * Allows some limited administration of the server through HTTP
     */
    public String adminAnswer( String uri, String perf, Properties header, Properties params ) {
	//logger.trace( "ADMIN[{}]", perf);
	String msg = "";
        if ( perf.equals("listmethods") ){
	    msg = "<h1>Embedded classes</h1>\n<h2>Methods</h2><ul compact=\"1\">";
	    for( String m : manager.listmethods() ) {
		msg += "<li>"+m+"</li>";
	    }
	    msg += "</ul>";
	    msg += "<h2>Renderers</h2><ul compact=\"1\">";
	    for( String m : manager.listrenderers() ) {
		msg += "<li>"+m+"</li>";
	    }
	    msg += "</ul>";
	    msg += "<h2>Services</h2><ul compact=\"1\">";
	    for( String m : manager.listservices() ) {
		msg += "<li>"+m+"</li>";
	    }
	    msg += "</ul>";
	    msg += "<h2>Evaluators</h2><ul compact=\"1\">";
	    for( String m : manager.listevaluators() ) {
		msg += "<li>"+m+"</li>";
	    }
	    msg += "</ul>";
	    // JE: This is unused because the menu below directly refers to /wsdl
	    // This does not really work because the wsdl is hidden in the HTML
        } else if ( perf.equals("wsdl") ){
	    msg = "<pre>"+WSAServProfile.wsdlAnswer( false )+"</pre>";
	} else if ( perf.equals("argline") ){
	    msg = "<h1>Command line arguments</h1>\n<pre>\n"+manager.argline()+"\n<pre>\n";
	} else if ( perf.equals("prmsqlquery") ){
	    msg = "<h1>SQL query</h1><form action=\"sqlquery\">Query:<br /><textarea name=\"query\" rows=\"20\" cols=\"80\">SELECT \nFROM \nWHERE </textarea> (sql)<br /><small>An SQL SELECT query</small><br /><input type=\"submit\" value=\"Query\"/></form>";
	} else if ( perf.equals("sqlquery") ){
	    String answer = manager.query( params.getProperty("query") );
	    msg = "<pre>"+answer+"</pre>";
	} else if ( perf.equals("about") ){
	    msg = about();
	} else if ( perf.equals("shutdown") ){
	    manager.shutdown();
	    msg = "<h1>Server shut down</h1>";
	} else if ( perf.equals("prmreset") ){
	    manager.reset();
	    msg = "<h1>Alignment server reset from database</h1>";
	} else if ( perf.equals("prmflush") ){
	    manager.flush();
	    msg = "<h1>Cache has been flushed</h1>";
	} else if ( perf.equals("addservice") ){
	    msg = perf;
	} else if ( perf.equals("addmethod") ){
	    msg = perf;
	} else if ( perf.equals("addrenderer") ){
	    msg = perf;
	} else if ( perf.equals("") ) {
	    msg = "<h1>Alignment server administration</h1>";
	    msg += "<form action=\"listmethods\"><button title=\"List embedded plug-ins\" type=\"submit\">Embedded classes</button></form>";
	    msg += "<form action=\"/wsdl\"><button title=\"WSDL Description\" type=\"submit\">WSDL Description</button></form>";
	    msg += "<form action=\"prmsqlquery\"><button title=\"Query the SQL database (unavailable)\" type=\"submit\">SQL Query</button></form>";
	    msg += "<form action=\"prmflush\"><button title=\"Free memory by unloading correspondences\" type=\"submit\">Flush caches</button></form>";
	    msg += "<form action=\"prmreset\"><button title=\"Restore launching state (reload from database)\" type=\"submit\">Reset server</button></form>";
	    //	    msg += "<form action=\"shutdown\"><button title=\"Shutdown server\" type=\"submit\">Shutdown</button></form>";
	    msg += "<form action=\"..\"><button title=\"About...\" type=\"submit\">About</button></form>";
	    msg += "<form action=\"../html/\"><button style=\"background-color: lightpink;\" title=\"Back to user menu\" type=\"submit\">User interface</button></form>";
	} else {
	    msg = "Cannot understand: "+perf;
	}
	return "<html><head>"+HEADER+"</head><body>"+msg+"<hr /><center><small><a href=\".\">Alignment server administration</a></small></center></body></html>";
    }

    
    /**
     * HTTP ontology networks interface
     * Allows the ontology networks management through HTTP
     */
    public String ontologyNetworkAnswer( String uri, String perf, Properties header, Properties params ) {
	logger.trace( "ONTONET[{}]", perf);
	String msg = "";
	String eSource = "on";
        if ( perf.equals("listnetwork") ){
        	
        	Collection<OntologyNetwork> ontologyNetworks = manager.ontologyNetworks();
        	BasicOntologyNetwork noo = null;
        	String id = "";
        	String pid = "";
    	    msg = "<h1>Available networks</h1>";
    	    //msg += "<form action=\"listNetOnto\"><ul compact=\"1\">";
    	    msg += "<form action=\"listnetwork\"><ul compact=\"1\">";
    	    for ( OntologyNetwork oNetwork : ontologyNetworks ) {
    	    	noo = (BasicOntologyNetwork) oNetwork;
    	    	id = noo.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID );
    			pid = noo.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
    			if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
     			msg += "<li><a href=\"" + id +"\">" + pid + "</a></li>";
    		    }
		   msg += "</ul></form>";

        } else if ( perf.equals("listNetOnto") ){   //TODO eliminate this
		   //Message answer = manager.renderHTMLON( params );
		   Message answer = manager.renderHTMLNetwork( params );
		    if ( answer instanceof ErrorMsg ) {
			msg = testErrorMessages( answer, params, eSource );
		    } else {
		    	return answer.getContent();
		    }
		      
        } else if ( perf.equals("listNetOnto2") ){
        	
        	msg = "<h1>Ontologies of the Network</h1>";
        	msg += "<form action=\"listNetAlig\">";
		    String uriON = params.getProperty("uriON");  //send as parameter uriON for listNetAlig
		    int numOnto = 0;
		    Collection<URI> networkOntology = manager.networkOntologyUri(uriON);
		    msg += "<p>" + uriON + "   ";
		   	msg += "&nbsp;<input type=\"submit\" value=\"List alignments\"/></form><ul compact=\"1\"></p>";
		   	msg += "<p><tr><th><b>Total ontologies: </b>" + networkOntology.size() + "</th></tr></p>";
		    for ( URI onto : networkOntology ) {
		    	numOnto ++;
				msg += "<li><a href=\""+onto.toString()+"\"> ("+String.format("%05d", numOnto)+") "+onto.toString()+"</a></li>";
		    }
		    //msg += "&nbsp;<input type=\"submit\" value=\"List alignments\"/></form><ul compact=\"1\">";
		    
        } else if ( perf.equals("listNetAlig") ){
        	
        	msg = "<h1>Alignments of the Network</h1>";
		    String uriON = params.getProperty("uri");   
		    Set<Alignment> networkAlignments = manager.networkAlignmentUri(uriON);
		    int numAlig = 0;
		    msg += "<p>" + uriON + "</p>";
		    msg += "<p><tr><th><b>Total alignments: </b>" + networkAlignments.size() + "</th></tr></p>";
		    for (Alignment al : networkAlignments) {
		    	numAlig ++;
		    	msg += "&nbsp;<li><a href=\""+"idAlign" + "\"> ("+String.format("%05d", numAlig)+") " +"idAlign:"+"</a>&nbsp;&nbsp;&nbsp; "+ al.getFile1() + "&nbsp;&nbsp;&nbsp;" + al.getFile2() + "</li>";
		    	} 
	    
        } else if ( perf.equals("prmloadonet") ){
        	//TODO add two more parameters TYPE of file (json/html, etc) and STRUCTURE of the file
        	msg = "<h1>Load an ontology network</h1>";
        	msg += "<form action=\"loadonet\">";
    	    msg += "Network URL: <input type=\"text\" name=\"url\" size=\"80\"/><br />";
    	    msg += "<small>This is the URL of ontology network. It must be reachable by the server (i.e., file://localhost/absolutePathTo/file or file:///absolutePathTo/file if localhost omitted)</small><br />";
    	    msg += "Pretty name: <input type=\"text\" name=\"pretty\" size=\"80\"/><br />";
    	    msg += "<input type=\"submit\" value=\"Load\"></form>";
    	    msg += "Ontology network file: <form enctype=\"multipart/form-data\" action=\"loadonet\" method=\"POST\">";
    	    msg += "<input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\""+MAX_FILE_SIZE+"\"/>";
    	    msg += "<input name=\"content\" type=\"file\" size=\"35\"><br />";
    	    msg += "Pretty name: <input type=\"text\" name=\"pretty\" size=\"80\"/><br />";
    	    msg += "<input type=\"submit\" Value=\"Upload\">";
    	    msg +=  " </form>";
        } else if ( perf.equals("loadonet") ) {
        	
        	Message answer = manager.loadonet( params );
    	    if ( answer instanceof ErrorMsg ) {
    	    	msg = testErrorMessages( answer, params, eSource );
    	    } else {
    		msg = "<h1>Ontology Network loaded</h1>";
    		msg += displayAnswerON( answer, params );
    	    }
	} else if ( perf.equals("storeonet") ){

			// here should be done the switch between store and load/store
		    String id = params.getProperty("id");
		    String url = params.getProperty("url");
		    if ( url != null && !url.equals("") ) {
			Message answer = manager.loadonet( params );
			if ( answer instanceof ErrorMsg ) {
			    msg = testErrorMessages( answer, params, eSource );
			    } else {
			    	id = answer.getContent();
			    	}
			}
		    if ( id != null ){ // Store it
			Message answer = manager.storeonet( params );
			if ( answer instanceof ErrorMsg ) {
			    msg = testErrorMessages( answer, params, eSource );
			    } else {
			    	msg = "<h1>Ontology Network stored</h1>";
			    	msg += displayAnswer( answer, params );
			    	}
			}	
			
    } else if ( perf.equals("listonet") ){
   /* 	to be done
    	
		    msg = "<h1>Ontology Network listed</h1>"; 
		    //msg += manager.listONetwork(params);
		    msg += "onet";
		    Collection<OntologyNetworkSet> onsV = manager.ontologyNetworkSetV();
		    Object onetwork = manager.onetworksT();  //send parameter uri
		    
		    msg += "<p><b>URI :</b>"+ onsV.getValue() +"<br>";
		    //msg += "<p><b>URI :</b>"+ onetwork.toString() +"<br>"; //falta un getName
		    //msg += "<b>Name :</b>"+ onetwork.toString()+"<br>";
		   
		    Hashtable<URI,NetOntology<Object>> oonetworkTable = manager.oonetworksT();
		    
		    msg += "<tr><th><b>Total ontologies :</b>"+ oonetworkTable.size()+"</th></tr></p>";
		    msg += "<table><tr><th>URI(ID)</th><th>NAME</th><th>FILE</th></tr>";
		    Set<Entry<URI, NetOntology<Object>>> set = oonetworkTable.entrySet();
            Iterator<Entry<URI, NetOntology<Object>>> it = set.iterator();
            
            while (it.hasNext()) {
              Map.Entry<URI, NetOntology<Object>> entry = (Map.Entry<URI, NetOntology<Object>>) it.next();
              NetOntology<Object> oonetwork = entry.getValue();
              oonetwork.getFile();
              msg += "<tr><td>" + entry.getKey() + "</td><td>" + oonetwork.getName() + "</td><td>" + oonetwork.getFile() + "</td></tr>";
              //msg += "<tr><td>" + entry.getKey() + "</td><td>" + oonetwork.oonName + "</td><td>" + oonetwork.oonFile + "</td></tr>";

            }
            
		    msg += "</table>";
		    //msg += displayAnswerON( answer, params );
		    //msg += displayAnswerON( (Message)null, params );
		//}
		 * 
		 */
    } else if ( perf.equals("prmstoreonet") ){

	    	String sel = params.getProperty("id");
	    	msg = "<h1>Store an ontology network</h1><form action=\"storeonet\">";
		    msg += "Network:  <select name=\"id\">";
		    for ( OntologyNetwork on : manager.ontologyNetworks() ) {		    	
		    	String id = ((BasicOntologyNetwork)on).getExtension( Namespace.ALIGNMENT.uri, Annotations.ID ); //TODO eliminate BasicOntologyNetwork
		    	String pid = ((BasicOntologyNetwork)on).getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ); //TODO eliminate BasicOntologyNetwork
		    	if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
		    	if ( sel != null && sel.equals( id ) ){
		    		msg += "<option selected=\"1\" value=\""+id+"\">"+pid+"</option>";
		    		} else { msg += "<option value=\""+id+"\">"+pid+"</option>";}
		    	}
		    msg += "</select><br />";
		    msg += "<br /><input type=\"submit\" value=\"Store\"/></form>";
	    
    } else if ( perf.equals("prmtrimonet") ){
    	
    	String sel = params.getProperty("id");
	    msg ="<h1>Trim networks</h1><form action=\"trimonet\">";
	    msg += "Network:  <select name=\"id\">";
	    for ( OntologyNetwork on : manager.ontologyNetworks() ) {		    	
	    	String id = ((BasicOntologyNetwork)on).getExtension( Namespace.ALIGNMENT.uri, Annotations.ID ); //TODO eliminate BasicOntologyNetwork
	    	String pid = ((BasicOntologyNetwork)on).getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ); //TODO eliminate BasicOntologyNetwork
	    	if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
	    	if ( sel != null && sel.equals( id ) ){
	    		msg += "<option selected=\"1\" value=\""+id+"\">"+pid+"</option>";
	    		} else { msg += "<option value=\""+id+"\">"+pid+"</option>";}
	    	}
	    msg += "</select><br />";
	    msg += "Type: <select name=\"type\"><option value=\"hard\">hard</option><option value=\"perc\">perc</option><option value=\"best\">best</option><option value=\"span\">span</option><option value=\"prop\">prop</option></select><br />Threshold: <input type=\"text\" name=\"threshold\" size=\"4\"/> <small>A value between 0. and 1. with 2 digits</small><br /><input type=\"submit\" name=\"action\" value=\"Trim\"/><br /></form>";

    } else if ( perf.equals("trimonet") ){
    	Message answer = manager.trimonet( params );
	    if ( answer instanceof ErrorMsg ) {
	    	System.out.println("answer error ");
		msg = testErrorMessages( answer, params, eSource );
	    } else {
	    	msg ="<h1>Trimed network</h1><form action=\"trimonet\">";
	    	msg += "<a href=\"" + answer.getContent() +"\">" + answer.getContent() + "</a>";

	    	System.out.println("answer: "+ answer.getContent());
	    }
	    
	} else if ( perf.equals("prmmatchonet") ){

	    	msg = "<h1>Match an ontology network</h1><form action=\"matchonet\">";
	    	msg += "<h2><font color=\"red\">(not implemented yet)</font></h2>";
		    msg += "Network:  <select name=\"id\">";
			String sel = params.getProperty("id");
		    for ( OntologyNetwork on : manager.ontologyNetworks() ) {		    	
		    	String id = ((BasicOntologyNetwork)on).getExtension( Namespace.ALIGNMENT.uri, Annotations.ID ); //TODO eliminate BasicOntologyNetwork
		    	String pid = ((BasicOntologyNetwork)on).getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ); //TODO eliminate BasicOntologyNetwork
		    	if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
		    	if ( sel != null && sel.equals( id ) ){
		    		msg += "<option selected=\"1\" value=\""+id+"\">"+pid+"</option>";
		    		} else { msg += "<option value=\""+id+"\">"+pid+"</option>";}
		    	}
		    msg += "</select><br />";

//		    msg += "<!--input type=\"submit\" name=\"action\" value=\"Find\"/>";
//		    msg += "<br /-->Methods: <select name=\"method\">";

		    msg += "<br />Methods: <select name=\"method\">";
		    for( String idMethod : manager.listmethods() ) {
				msg += "<option value=\""+idMethod+"\">"+idMethod+"</option>"; 
			    }
		    msg += "</select><br /><br />";

		    msg += "Pretty name: <input type=\"text\" name=\"pretty\" size=\"80\"/><br />";
		    msg += "  <input type=\"checkbox\" name=\"force\" /> Force <input type=\"checkbox\" name=\"async\" /> Asynchronous<br />";
		    msg += "Additional parameters:<br /><input type=\"text\" name=\"paramn1\" size=\"15\"/> = <input type=\"text\" name=\"paramv1\" size=\"65\"/><br /><input type=\"text\" name=\"paramn2\" size=\"15\"/> = <input type=\"text\" name=\"paramv2\" size=\"65\"/><br /><input type=\"text\" name=\"paramn3\" size=\"15\"/> = <input type=\"text\" name=\"paramv3\" size=\"65\"/><br /><input type=\"text\" name=\"paramn4\" size=\"15\"/> = <input type=\"text\" name=\"paramv4\" size=\"65\"/>";

		    msg += "<br /><input type=\"checkbox\" name=\"reflexive\" /> Reflexive ";
		    msg += "<input type=\"checkbox\" name=\"symmetric\" /> Symmetric ";
		    msg += "<input type=\"checkbox\" name=\"new\" /> New ";
		    msg += "<br /><br /><input type=\"submit\" name=\"action\" value=\"Match\"/> ";
		    msg += "</form>";
		    	    
	} else if ( perf.equals("matchonet") ) {
		
		// DO MATCHING version 1
/*		
	    List<Message> answer = manager.alignonet( params );
		msg = "<h1>Network Alignments results</h1>";
		Iterator<Message> it = answer.iterator();
		msg += "<ul>";
		while (it.hasNext()) {
			final Message alignment = it.next();
 			msg += "<li><a href=\"" + alignment.getContent() +"\">" + alignment.getContent() + "</a></li>";
		}
		msg += "</ul>";
		
*/		
		
		// DO MATCHING
    	String idON = params.getProperty("id");
		logger.debug("Matching network {}", idON);
		Message answer = manager.alignonet2( params );
		if ( answer instanceof ErrorMsg ) {
			msg = testErrorMessages( answer, params, eSource );
		    } else {
		    	//return answer.getContent();
				msg = "<h1>Network alignments results</h1>";
			    Set<Alignment> networkAlignments = manager.networkAlignmentUri(idON);
			    msg += "<p> Network ID: <a href=\""+idON+"\">"+idON+"</a></p>";
			    msg += "<p><tr><th><b>Total alignments: </b>" + networkAlignments.size() + "</th></tr></p>";
			    msg += "<ul>";
			    for ( Alignment al : networkAlignments ) {
					String id = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID );
					String pid = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
					if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
					msg += "<li><a href=\""+id+"\">"+pid+"</a></li>";
				    }
			    msg += "</ul>";
		    }	    
	    
	} else if ( perf.equals("prmretreiveonet") ){
		
		String sel = params.getProperty("id");
	    msg = "<h1>Retrieve ontology network</h1><form action=\"retrieveonet\">";
	    msg += "Network:  <select name=\"id\">";
	    for ( OntologyNetwork on : manager.ontologyNetworks() ) {		    	
	    	String id = ((BasicOntologyNetwork)on).getExtension( Namespace.ALIGNMENT.uri, Annotations.ID ); //TODO eliminate BasicOntologyNetwork
	    	String pid = ((BasicOntologyNetwork)on).getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ); //TODO eliminate BasicOntologyNetwork
	    	if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
	    	if ( sel != null && sel.equals( id ) ){
	    		msg += "<option selected=\"1\" value=\""+id+"\">"+pid+"</option>";
	    		} else { msg += "<option value=\""+id+"\">"+pid+"</option>";}
	    	}
	    msg += "</select><br />";
	    msg += "<br /><input type=\"submit\" value=\"Retrieve\"/></form>";
	    
	} else if ( perf.equals("retrieveonet") ) {

	    Message answer = manager.renderonet( params );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer, params, eSource );
	    } else {
		// Depending on the type we should change the MIME type
	    	//return answer.getContent().replaceAll("&", "&amp;").replaceAll("<", "&lt;");
	    	return answer.getContent();
	    }
		
		
		
		
	    
	} else if ( perf.equals("prmcloseonet") ){
		
		msg = "<h1>Close an ontology network</h1><form action=\"matchonet\">";
		msg += "<h2><font color=\"red\">(not implemented yet)</font></h2>";
	    msg += "Network:  <select name=\"id\">";
		String sel = params.getProperty("id");
	    for ( OntologyNetwork on : manager.ontologyNetworks() ) {		    	
	    	String id = ((BasicOntologyNetwork)on).getExtension( Namespace.ALIGNMENT.uri, Annotations.ID ); //TODO eliminate BasicOntologyNetwork
	    	String pid = ((BasicOntologyNetwork)on).getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ); //TODO eliminate BasicOntologyNetwork
	    	if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
	    	if ( sel != null && sel.equals( id ) ){
	    		msg += "<option selected=\"1\" value=\""+id+"\">"+pid+"</option>";
	    		} else { msg += "<option value=\""+id+"\">"+pid+"</option>";}
	    	}

	    msg += "</select><br />";
	    msg += "<input type=\"submit\" name=\"action\" value=\"Invert\" /> ";
	    msg += "<input type=\"submit\" name=\"action\" value=\"Compose\"/> ";
	    msg += "<input type=\"checkbox\" name=\"new\" /> New ";
	   
	} else if ( perf.equals("prmnormalizeonet") ){

	} else if ( perf.equals("") ) {
		msg = "<h1>Ontology Network commands</h1>";
		msg += "<form action=\"../ontonet/listnetwork\"><button title=\"List networks stored in the server\" type=\"submit\">Available networks</button></form>";
	//	msg += "<form action=\"prmlistonet\"><button title=\"List networks stored in the server\" type=\"submit\">Available networks</button></form>";
		msg += "<form action=\"prmloadonet\"><button title=\"Load a network from a valid source\" type=\"submit\">Load a network</button></form>";
		msg += "<form action=\"prmmatchonet\"><button title=\"Match an ontology network\" type=\"submit\">Match network </button></form>";
		msg += "<form action=\"prmtrimonet\"><button title=\"Trim a network\" type=\"submit\">Trim network</button></form>";
		msg += "<form action=\"prmnormalizeonet\"><button title=\"Normalize an ontology network\" type=\"submit\">Normalize network</button></form>";
		msg += "<form action=\"prmcloseonet\"><button title=\"Close an ontology network\" type=\"submit\">Close network</button></form>";
		msg += "<form action=\"prmretreiveonet\"><button title=\"Render an ontology network in a particular format\" type=\"submit\">Render network</button></form>";
		msg += "<form action=\"prmstoreonet\"><button title=\"Store a network in the server\" type=\"submit\">Store network</button></form>";
		msg += "<form action=\"../html/\"><button style=\"background-color: lightpink;\" title=\"Back to user menu\" type=\"submit\">User interface</button></form>";
	} else {
	    msg = "Cannot understand: "+perf;
	}
	return "<html><head>"+HEADER+"</head><body>"+msg+"<hr /><center><small><a href=\".\">Ontology Networks Management</a></small></center></body></html>";
    }

    /**
     * User friendly HTTP interface
     * uses the protocol but offers user-targeted interaction
     */
    public String htmlAnswer( String uri, String perf, Properties header, Properties params ) {
	logger.trace("HTML[{}]", perf );
	// REST get
	String msg = "";
	String eSource = "al";
	if ( perf.equals("listalignments") ) {
	    URI uri1 = null;	
	    String u1 = params.getProperty("uri1");
	    try {
		if ( u1 != null && !u1.equals("all") ) uri1 = new URI( u1 );
	    } catch ( URISyntaxException usex ) {
		logger.debug( "IGNORED Invalid URI parameter", usex );
	    };
	    URI uri2 = null;
	    String u2 = params.getProperty("uri2");
	    try {
		if ( u2 != null && !u2.equals("all") ) uri2 = new URI( u2 );
	    } catch ( URISyntaxException usex ) {
		logger.debug( "IGNORED Invalid URI parameter", usex );
	    };
	    // Add two onto checklist
	    Collection<URI> ontologies = manager.ontologies();
	    msg = "<h1>Available alignments</h1><form action=\"listalignments\">";
	    msg += "Onto1:  <select name=\"uri1\"><option value=\"all\"";
	    if ( uri1 == null ) msg += " selected=\"1\"";
	    msg += ">all</option>";
	    for ( URI ont : ontologies ) {
		msg += "<option";
		if ( ont.equals( uri1 ) ) msg += " selected =\"1\"";
		msg += " value=\""+ont+"\">"+ont+"</option>"; //simplify
	    }
	    msg += "</select>";
	    msg += "Onto2:  <select name=\"uri2\"><option value=\"all\"";
	    if ( uri2 == null ) msg += " selected=\"1\"";
	    msg += ">all</option>";
	    for ( URI ont : ontologies ) { 
		msg += "<option";
		if ( ont.equals( uri2 ) ) msg += " selected =\"1\"";
		msg += " value=\""+ont+"\">"+ont+"</option>"; //simplify
	    }
	    msg += "</select>";
	    msg += "&nbsp;<input type=\"submit\" value=\"Restrict\"/></form><ul compact=\"1\">";
	    // would be better as a JavaScript which updates
	    Collection<Alignment> alignments = null;
	    if ( uri1 == null && uri2 == null ) {
		alignments = manager.alignments();
	    } else {
		alignments = manager.alignments( uri1, uri2 );
	    }

	    for ( Alignment al : alignments ) {
		String id = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID );
		String pid = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
		if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
		//msg += "<li><a href=\"../html/retrieve?method=fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor&id="+id+"\">"+pid+"</a></li>";
		msg += "<li><a href=\""+id+"\">"+pid+"</a></li>";
	    }
	    msg += "</ul>";
	    
	} else if ( perf.equals("manalignments") ){ // Manage alignments
	    msg = "<h1>Available alignments</h1><ul compact=\"1\">";
	    for ( Alignment al : manager.alignments() ) {
		String id = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID );
		String pid = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
		if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
		//msg += "<li><a href=\"../html/retrieve?method=fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor&id="+id+"\">"+pid+"</a> "+al.nbCells()+" <a href=\"../html/errrazze?id="+id+"\">DEL</a></li>";
		msg += "<li><a href=\""+id+"\">"+pid+"</a> "+al.nbCells()+" <a href=\"../html/errrazze?id="+id+"\">DEL</a></li>";
	    }
	    msg += "</ul>";
	} else if ( perf.equals("errrazze") ){ // Suppress an alignment
	    String id = params.getProperty("id");
	    if ( id != null && !id.equals("") ) { // Erase it
		Message answer = manager.erase( params );
		if ( answer instanceof ErrorMsg ) {
		    msg = testErrorMessages( answer, params, eSource );
		} else {
		    msg = "<h1>Alignment deleted</h1>";
		    msg += displayAnswer( answer, params );
		}
	    }
	} else 	if ( perf.equals("prmstore") ) {
	    msg = "<h1>Store an alignment</h1><form action=\"store\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    // JE: only those non stored please (retrieve metadata + stored)
	    for ( Alignment al : manager.alignments() ) {
		String id = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID);
		params.setProperty("id", id);
		if ( !manager.storedAlignment( params ) ){
		    String pid = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
		    if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
		    msg += "<option value=\""+id+"\">"+pid+"</option>";
		}
	    }
	    msg += "</select><br />";
	    msg += "<input type=\"submit\" value=\"Store\"/></form>";
	} else if ( perf.equals("store") ) {
	    // here should be done the switch between store and load/store
	    String id = params.getProperty("id");
	    String url = params.getProperty("url");
	    if ( url != null && !url.equals("") ) { // Load the URL
		Message answer = manager.load( params );
		if ( answer instanceof ErrorMsg ) {
		    msg = testErrorMessages( answer, params, eSource );
		} else {
		    id = answer.getContent();
		}
	    }
	    if ( id != null ){ // Store it
		Message answer = manager.store( params );
		if ( answer instanceof ErrorMsg ) {
		    msg = testErrorMessages( answer, params, eSource );
		} else {
		    msg = "<h1>Alignment stored</h1>";
		    msg += displayAnswer( answer, params );
		}
	    }
	} else if ( perf.equals("prmtrim") ) {
	    String sel = params.getProperty("id");
	    msg ="<h1>Trim alignments</h1><form action=\"trim\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    for( Alignment al: manager.alignments() ){
		String id = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID);
		String pid = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
		if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
		if ( sel != null && sel.equals( id ) ){
		    msg += "<option selected=\"1\" value=\""+id+"\">"+pid+"</option>";
		} else {
		    msg += "<option value=\""+id+"\">"+pid+"</option>";
		}
	    }
	    msg += "</select><br />";
	    msg += "Type: <select name=\"type\"><option value=\"hard\">hard</option><option value=\"perc\">perc</option><option value=\"best\">best</option><option value=\"span\">span</option><option value=\"prop\">prop</option></select><br />Threshold: <input type=\"text\" name=\"threshold\" size=\"4\"/> <small>A value between 0. and 1. with 2 digits</small><br /><input type=\"submit\" name=\"action\" value=\"Trim\"/><br /></form>";
	} else if ( perf.equals("trim") ) {
	    String id = params.getProperty("id");
	    String threshold = params.getProperty("threshold");
	    if ( id != null && !id.equals("") && threshold != null && !threshold.equals("") ){ // Trim it
		Message answer = manager.trim( params );
		if ( answer instanceof ErrorMsg ) {
		    msg = testErrorMessages( answer, params, eSource );
		} else {
		    msg = "<h1>Alignment trimed</h1>";
		    msg += displayAnswer( answer, params );
		}
	    }
	} else if ( perf.equals("prminv") ) {
	    msg ="<h1>Invert alignment</h1><form action=\"inv\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    for( Alignment al: manager.alignments() ){
		String id = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID);
		String pid = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
		if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
		msg += "<option value=\""+id+"\">"+pid+"</option>";
	    }
	    msg += "</select><br />";
	    msg += "<input type=\"submit\" name=\"action\" value=\"Invert\"/><br /></form>";
	} else if ( perf.equals("inv") ) {
	    String id = params.getProperty("id");
	    if ( id != null && !id.equals("") ){ // Invert it
		Message answer = manager.inverse( params );
		if ( answer instanceof ErrorMsg ) {
		    msg = testErrorMessages( answer, params, eSource );
		} else {
		    msg = "<h1>Alignment inverted</h1>";
		    msg += displayAnswer( answer, params );
		}
	    }
	} else if ( perf.equals("prmmatch") ) {
	    String RESTOnto1 = "";
	    String RESTOnto2 = "";
	    String readonlyOnto = "";
	    //Ontologies from Cupboard may be already provided here.
	    if ( params.getProperty("restful") != null && 
		 (params.getProperty("renderer")).equals("HTML") ) {
		RESTOnto1 = params.getProperty("onto1");
		RESTOnto2 = params.getProperty("onto2");
		//if(RESTOnto1 != null && !RESTOnto1.equals("") && RESTOnto2 != null && !RESTOnto2.equals("")) 
		readonlyOnto = "readonly=\"readonly\"";
	    }
	    msg ="<h1>Match ontologies</h1><form action=\"match\">Ontology 1: <input type=\"text\" name=\"onto1\" size=\"80\" value="+RESTOnto1+" " +readonlyOnto+"> (uri)<br />Ontology 2: <input type=\"text\" name=\"onto2\" size=\"80\" value="+RESTOnto2+" "+readonlyOnto+ "> (uri)<br /><small>These are the URL of places where to find these ontologies. They must be reachable by the server (i.e., file:// URI are acceptable if they are on the server)</small><br /><!--input type=\"submit\" name=\"action\" value=\"Find\"/><br /-->Methods: <select name=\"method\">";
	    for( String id : manager.listmethods() ) {
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br />Initial alignment id:  <select name=\"id\"><option value=\"\" selected=\"1\"></option>";
	    for( Alignment al: manager.alignments() ){
		String id = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID);
		String pid = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
		if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
		msg += "<option value=\""+id+"\">"+pid+"</option>";
	    }
	    msg += "</select><br />";
	    msg += "Pretty name: <input type=\"text\" name=\"pretty\" size=\"80\"/><br />";
	    msg += "<input type=\"submit\" name=\"action\" value=\"Match\"/>";
	    msg += "  <input type=\"checkbox\" name=\"force\" /> Force <input type=\"checkbox\" name=\"async\" /> Asynchronous<br />";
	    msg += "Additional parameters:<br /><input type=\"text\" name=\"paramn1\" size=\"15\"/> = <input type=\"text\" name=\"paramv1\" size=\"65\"/><br /><input type=\"text\" name=\"paramn2\" size=\"15\"/> = <input type=\"text\" name=\"paramv2\" size=\"65\"/><br /><input type=\"text\" name=\"paramn3\" size=\"15\"/> = <input type=\"text\" name=\"paramv3\" size=\"65\"/><br /><input type=\"text\" name=\"paramn4\" size=\"15\"/> = <input type=\"text\" name=\"paramv4\" size=\"65\"/></form>";
	} else if ( perf.equals("match") ) {
	    Message answer = manager.align( params );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer, params, eSource );
	    } else {
		msg = "<h1>Alignment results</h1>";
		msg += displayAnswer( answer, params );
	    }
	} else if ( perf.equals("prmfind") ) {
	    msg ="<h1>Find alignments between ontologies</h1><form action=\"find\">Ontology 1: <input type=\"text\" name=\"onto1\" size=\"80\"/> (uri)<br />Ontology 2: <input type=\"text\" name=\"onto2\" size=\"80\"/> (uri)<br /><small>These are the URI identifying the ontologies. Not those of places where to upload them.</small><br /><input type=\"submit\" name=\"action\" value=\"Find\"/></form>";
	} else if ( perf.equals("find") ) {
	    Message answer = manager.existingAlignments( params );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer, params, eSource );
	    } else {
		msg = "<h1>Found alignments</h1>";
		msg += displayAnswer( answer, params );
	    }
	} else if ( perf.equals("corresp") ) {
	    Message answer = manager.findCorrespondences( params );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer, params, eSource );
	    } else {
		msg = "<h1>Found correspondences</h1>";
		msg += displayAnswer( answer, params );
	    }
	} else if ( perf.equals("prmretrieve") ) {
	    String sel = params.getProperty("id");
	    msg = "<h1>Retrieve alignment</h1><form action=\"retrieve\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    for( Alignment al: manager.alignments() ){
		String id = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID);
		String pid = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
		if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
		if ( sel != null && sel.equals( id ) ){
		    msg += "<option selected=\"1\" value=\""+id+"\">"+pid+"</option>";
		} else {
		    msg += "<option value=\""+id+"\">"+pid+"</option>";
		}
	    }
	    msg += "</select><br />";
	    msg += "Rendering: <select name=\"method\">";
	    for( String id : manager.listrenderers() ) {
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br /><input type=\"submit\" value=\"Retrieve\"/></form>";
	} else if ( perf.equals("retrieve") ) {

	    Message answer = manager.render( params );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer, params, eSource );
	    } else {
		// Depending on the type we should change the MIME type
		// This should be returned in answer.getParameters()
		return answer.getContent();

	    }
	// Metadata not done yet
	} else if ( perf.equals("prmmetadata") ) {
	    msg = "<h1>Retrieve alignment metadata</h1><form action=\"metadata\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    for( Alignment al: manager.alignments() ){
		String id = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID);
		String pid = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
		if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
		msg += "<option value=\""+id+"\">"+pid+"</option>";
	    }
	    msg += "</select><br /><input type=\"submit\" value=\"Get metadata\"/></form>";
	} else if ( perf.equals("metadata") ) {
	    if( params.getProperty("renderer") == null || (params.getProperty("renderer")).equals("HTML") )
	    	params.setProperty("method", "fr.inrialpes.exmo.align.impl.renderer.HTMLMetadataRendererVisitor");
	    else
		params.setProperty("method", "fr.inrialpes.exmo.align.impl.renderer.XMLMetadataRendererVisitor");
	    Message answer = manager.render( params );
	    //logger.trace( "Content: {}", answer.getContent() );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer, params, eSource );
	    } else {
		// Depending on the type we should change the MIME type
		return answer.getContent();
	    }
	    // render
	    // Alignment in HTML can be rendre or metadata+tuples
	} else if ( perf.equals("prmload") ) {
	    // Should certainly be good to offer store as well
	    msg = "<h1>Load an alignment</h1><form action=\"load\">Alignment URL: <input type=\"text\" name=\"url\" size=\"80\"/> (uri)<br /><small>This is the URL of the place where to find this alignment. It must be reachable by the server (i.e., file:// URI is acceptable if it is on the server).</small><br />Pretty name: <input type=\"text\" name=\"pretty\" size=\"80\"/><br /><input type=\"submit\" value=\"Load\"/></form>";
	    //msg += "Alignment file: <form ENCTYPE=\"text/xml; charset=utf-8\" action=\"loadfile\" method=\"POST\">";
	    msg += "Alignment file: <form enctype=\"multipart/form-data\" action=\"load\" method=\"POST\">";
	    msg += "<input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\""+MAX_FILE_SIZE+"\"/>";
	    msg += "<input name=\"content\" type=\"file\" size=\"35\">";
	    msg += "<br /><small>NOTE: Max file size is "+(MAX_FILE_SIZE/1024)+"KB; this is experimental but works</small><br />";
	    msg += "Pretty name: <input type=\"text\" name=\"pretty\" size=\"80\"/><br />";
	    msg += "<input type=\"submit\" Value=\"Upload\">";
	    msg +=  " </form>";
	} else if ( perf.equals("load") ) {
	    // load
	    Message answer = manager.load( params );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer, params, eSource );
	    } else {
		msg = "<h1>Alignment loaded</h1>";
		msg += displayAnswer( answer, params );
	    }
	} else if ( perf.equals("prmtranslate") ) {
	    msg = "<h1>Translate query</h1><form action=\"translate\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    for( Alignment al: manager.alignments() ){
		String id = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID);
		String pid = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
		if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
		msg += "<option value=\""+id+"\">"+pid+"</option>";
	    }
	    msg += "</select><br />";
	    msg += "PREFIX rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt; .<br /><br />SPARQL query:<br /> <textarea name=\"query\" rows=\"20\" cols=\"80\">PREFIX foaf: <http://xmlns.com/foaf/0.1/>\nSELECT *\nFROM <>\nWHERE {\n\n}</textarea> (SPARQL)<br /><small>A SPARQL query (PREFIX prefix: &lt;uri&gt; SELECT variables FROM &lt;url&gt; WHERE { triples })</small><br /><input type=\"submit\" value=\"Translate\"/></form>";
	} else if ( perf.equals("translate") ) {
	    Message answer = manager.translate( params );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer, params, eSource );
	    } else {
		msg = "<h1>Message translation</h1>";
		msg += "<h2>Initial message</h2><pre>"+(params.getProperty("query")).replaceAll("&", "&amp;").replaceAll("<", "&lt;")+"</pre>";
		msg += "<h2>Translated message</h2><pre>";
		msg += answer.HTMLString().replaceAll("&", "&amp;").replaceAll("<", "&lt;");
		msg += "</pre>";
	    }
	} else if ( perf.equals("prmeval") ) {
	    msg ="<h1>Evaluate alignment</h1><form action=\"eval\">";
	    msg += "Alignment to evaluate: ";
	    msg += "<select name=\"id\">";
	    for( Alignment al: manager.alignments() ){
		String id = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID);
		String pid = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
		if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
		msg += "<option value=\""+id+"\">"+pid+"</option>";
	    }
	    msg += "</select><br />";
	    msg +="Reference alignment: ";
	    msg += "<select name=\"ref\">";
	    for( Alignment al: manager.alignments() ){
		String id = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID);
		String pid = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
		if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
		msg += "<option value=\""+id+"\">"+pid+"</option>";
	    }
	    msg += "</select><br />";
	    msg += "Evaluator: ";
	    msg += "<select name=\"method\">";
	    for( String id : manager.listevaluators() ) {
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br /><input type=\"submit\" name=\"action\" value=\"Evaluate\"/>\n";
	    msg += "</form>\n";
	} else if ( perf.equals("eval") ) {
	    Message answer = manager.eval( params );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer, params, eSource );
	    } else {
		msg = "<h1>Evaluation results</h1>";
		msg += displayAnswer( answer, params );
	    }
	} else if ( perf.equals("saveeval") ) {
	} else if ( perf.equals("prmgrpeval") ) {
	} else if ( perf.equals("grpeval") ) {
	} else if ( perf.equals("savegrpeval") ) {
	} else if ( perf.equals("prmresults") ) {
	} else if ( perf.equals("getresults") ) {
	} else if ( perf.equals("prmdiff") ) {
	    msg ="<h1>Compare alignments</h1><form action=\"diff\">";
	    msg += "First alignment: ";
	    msg += "<select name=\"id1\">";
	    for( Alignment al: manager.alignments() ){
		String id = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID);
		String pid = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
		if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
		msg += "<option value=\""+id+"\">"+pid+"</option>";
	    }
	    msg += "</select><br />";
	    msg +="Second alignment: ";
	    msg += "<select name=\"id2\">";
	    for( Alignment al: manager.alignments() ){
		String id = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID);
		String pid = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
		if ( pid == null ) pid = id; else pid = id+" ("+pid+")";
		msg += "<option value=\""+id+"\">"+pid+"</option>";
	    }
	    msg += "</select><br />";
	    msg += "<br /><input type=\"submit\" name=\"action\" value=\"Compare\"/>\n";
	    msg += "</form>\n";
	} else if ( perf.equals("diff") ) {
	    Message answer = manager.diff( params );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer, params, eSource );
	    } else {
		msg = "<h1>Comparison results</h1>";
		msg += displayAnswer( answer, params );
	    }
	} else if ( perf.equals("") ) {
	    msg = "<h1>Alignment server commands</h1>";
	    msg += "<form action=\"../html/listalignments\"><button title=\"List of all the alignments stored in the server\" type=\"submit\">Available alignments</button></form>";
	    msg += "<form action=\"prmload\"><button title=\"Upload an existing alignment in this server\" type=\"submit\">Load alignments</button></form>";
	    msg += "<form action=\"prmfind\"><button title=\"Find existing alignements between two ontologies\" type=\"submit\">Find alignment</button></form>";
	    msg += "<form action=\"prmmatch\"><button title=\"Apply matchers to ontologies for obtaining an alignment\" type=\"submit\">Match ontologies</button></form>";
	    msg += "<form action=\"prmtrim\"><button title=\"Trim an alignment above some threshold\" type=\"submit\">Trim alignment</button></form>";
	    msg += "<form action=\"prminv\"><button title=\"Swap the two ontologies of an alignment\" type=\"submit\">Invert alignment</button></form>";
	    msg += "<form action=\"prmstore\"><button title=\"Persistently store an alignent in this server\" type=\"submit\" >Store alignment</button></form>";
	    msg += "<form action=\"prmretrieve\"><button title=\"Render an alignment in a particular format\" type=\"submit\">Render alignment</button></form>";
	    msg += "<form action=\"prmtranslate\"><button title=\"Query translation through an alignment\" type=\"submit\">Translate query</button></form>";
	    msg += "<form action=\"prmeval\"><button title=\"Evaluation of an alignment\" type=\"submit\">Evaluate alignment</button></form>";
	    msg += "<form action=\"prmdiff\"><button title=\"Compare two alignments\" type=\"submit\">Compare alignment</button></form>";
	    msg += "<form action=\"../admin/\"><button style=\"background-color: lightpink;\" title=\"Server management functions\" type=\"submit\">Server management</button></form>";
	    msg += "<form action=\"../ontonet/\"><button style=\"background-color: lightgreen;\" title=\"Ontology Networks commands\" type=\"submit\">Ontology Networks</button></form>";
	} else {
	    msg = "Cannot understand command "+perf;
	}
	return "<html><head>"+HEADER+"</head><body>"+msg+"<hr /><center><small><a href=\".\">Alignment server</a></small></center></body></html>";
    }

    // ===============================================
    // Util

    private String testErrorMessages( Message answer, Properties param, String errorSource ) {
	/*
	if ( returnType == HTTPResponse.MIME_RDFXML ) {
	    return answer.RESTString();
	} else if ( returnType == HTTPResponse.MIME_JSON ) {
	    return answer.JSONString();render
	    
	    } else {*/
//	    return "<h1>Alignment error</h1>"+answer.HTMLString();
	    /*}*/
	    
	    switch (errorSource) {
	    case "al": return "<h1>Alignment error</h1>"+answer.HTMLString();
	    case "on": return "<h1>Ontology Network error</h1>"+answer.HTMLString();
	    default:   return "<h1>Not know error source</h1>"+answer.HTMLString();
	    }
    }

    private String displayAnswer( Message answer, Properties param ) {
	return displayAnswer( answer, param, null );
    }

    private String displayAnswer( Message answer, Properties param, String returnType ) {
	String result = null;
	if ( returnType == HTTPResponse.MIME_RDFXML ) {
	    if( param.getProperty("return").equals("HTML") ) { // RESTFUL but in HTML ??
	    	result = answer.HTMLRESTString();
	    	if ( answer instanceof AlignmentId && ( answer.getParameters() == null || answer.getParameters().getProperty("async") == null ) ) {
		    result += "<table><tr>";
		    result += "<td><form action=\"getID\"><input type=\"hidden\" name=\"id\" value=\""+answer.getContent()+"\"/><input type=\"submit\" name=\"action\" value=\"GetID\"  disabled=\"disabled\"/></form></td>";
		    result += "<td><form action=\"metadata\"><input type=\"hidden\" name=\"id\" value=\""+answer.getContent()+"\"/><input type=\"submit\" name=\"action\" value=\"Metadata\"/></form></td>";
		    result += "</tr></table>";
	    	} else if( answer instanceof AlignmentIds && ( answer.getParameters() == null || answer.getParameters().getProperty("async") == null )) {
	    	result = answer.HTMLRESTString();
		}
	    } else {
		result = answer.RESTString();
	    }
	} else if ( returnType == HTTPResponse.MIME_JSON ) {
	    result = answer.JSONString();
	} else {
	    result = answer.HTMLString();
	    // Improved return
	    if ( answer instanceof AlignmentId && ( answer.getParameters() == null || answer.getParameters().getProperty("async") == null ) ){
		result += "<table><tr>";
		// STORE
		result += "<td><form action=\"store\"><input type=\"hidden\" name=\"id\" value=\""+answer.getContent()+"\"/><input type=\"submit\" name=\"action\" value=\"Store\"/></form></td>";
		// TRIM (2)
		result += "<td><form action=\"prmtrim\"><input type=\"hidden\" name=\"id\" value=\""+answer.getContent()+"\"/><input type=\"submit\" name=\"action\" value=\"Trim\"/></form></td>";
		// RETRIEVE (1)
		result += "<td><form action=\"prmretrieve\"><input type=\"hidden\" name=\"id\" value=\""+answer.getContent()+"\"/><input type=\"submit\" name=\"action\" value=\"Show\"/></form></td>";
		// Note at that point it is not possible to get the methods
		// COMPARE (2)
		// INV
		result += "<td><form action=\"inv\"><input type=\"hidden\" name=\"id\" value=\""+answer.getContent()+"\"/><input type=\"submit\" name=\"action\" value=\"Invert\"/></form></td>";
		result += "</tr></table>";
	    } else if ( answer instanceof EvaluationId && ( answer.getParameters() == null || answer.getParameters().getProperty("async") == null ) ){
		result += "<table><tr>";
		// STORE (the value should be the id here, not the content)
		result += "<td><form action=\"saveeval\"><input type=\"hidden\" name=\"id\" value=\""+answer.getContent()+"\"/><input type=\"submit\" name=\"action\" value=\"Store\"/></form></td>";
		result += "</tr></table>";
	    }
	}
	return result;
    }
    
    private String displayAnswerON( Message answer, Properties param ) {
    	return displayAnswerON( answer, param, null );
        }

        private String displayAnswerON( Message answer, Properties param, String returnType ) {
    	String result = null;
    	if ( returnType == HTTPResponse.MIME_RDFXML ) {
    	    if( param.getProperty("return").equals("HTML") ) { // RESTFUL but in HTML ??
    	    	result = answer.HTMLRESTString();
    	    	if ( answer instanceof OntologyNetworkId && ( answer.getParameters() == null || answer.getParameters().getProperty("async") == null ) ) {
    		    result += "<table><tr>";
    		    result += "<td><form action=\"getID\"><input type=\"hidden\" name=\"id\" value=\""+answer.getContent()+"\"/><input type=\"submit\" name=\"action\" value=\"GetID\"  disabled=\"disabled\"/></form></td>";
    		    result += "<td><form action=\"metadata\"><input type=\"hidden\" name=\"id\" value=\""+answer.getContent()+"\"/><input type=\"submit\" name=\"action\" value=\"Metadata\"/></form></td>";
    		    result += "</tr></table>";
    	    	//} else if( answer instanceof OntologyNetworkIds && ( answer.getParameters() == null || answer.getParameters().getProperty("async") == null )) { //TODO is it needed??create public class OntologyNetworkIds extends Success {
    		    result = answer.HTMLRESTString();
    		}
    	    } else {
    		result = answer.RESTString();
    	    }
    	} else if ( returnType == HTTPResponse.MIME_JSON ) {
    	    result = answer.JSONString();
    	} else {
    	    result = answer.HTMLString();
    	    // Improved return
    	    if ( answer instanceof OntologyNetworkId && ( answer.getParameters() == null || answer.getParameters().getProperty("async") == null ) ){
    		result += "<table><tr>";
    		// STORE ONTOLOGY NETWORK
    		result += "<td><form action=\"storeonet\"><input type=\"hidden\" name=\"id\" value=\""+answer.getContent()+"\"/><input type=\"submit\" name=\"action\" value=\"Store\"/></form></td>";
    		// RETREIVE ONTOLOGY NETWORK
    		result += "<td><form action=\"prmretreiveonet\"><input type=\"hidden\" name=\"id\" value=\""+answer.getContent()+"\"/><input type=\"submit\" name=\"action\" value=\"Show\"/></form></td>";  
    		result += "</tr></table>";
    	    }
    	}
    	return result;
        }

}

