/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2006-2007.
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

import fr.inrialpes.exmo.align.impl.BasicParameters;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Parameters;

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

import java.util.StringTokenizer;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.URLEncoder;
import java.net.URLDecoder;

import java.lang.Integer;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.Request;

import org.mortbay.util.MultiMap;
import org.mortbay.util.StringUtil;
import org.mortbay.util.TypeUtil;

import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.servlet.MultiPartFilter;
import org.mortbay.jetty.servlet.Context.SContext;

/**
 * HTMLAServProfile: an HTML provile for the Alignment server
 * It embeds an HTTP server.
 */

public class HTMLAServProfile implements AlignmentServiceProfile {

    private int tcpPort;
    private String tcpHost;
    private int debug = 0;
    private Server server;
    private AServProtocolManager manager;
    private WSAServProfile wsmanager;

    private String myId;
    private String serverId;
    private int localId = 0;

    /**
     * Some HTTP response status codes
     */
    public static final String
	HTTP_OK = "200 OK",
	HTTP_REDIRECT = "301 Moved Permanently",
	HTTP_FORBIDDEN = "403 Forbidden",
	HTTP_NOTFOUND = "404 Not Found",
	HTTP_BADREQUEST = "400 Bad Request",
	HTTP_INTERNALERROR = "500 Internal Server Error",
	HTTP_NOTIMPLEMENTED = "501 Not Implemented";

    /**
     * Common mime types for dynamic content
     */
    public static final String
	MIME_PLAINTEXT = "text/plain",
	MIME_HTML = "text/html",
	MIME_XML = "text/xml",
	MIME_DEFAULT_BINARY = "application/octet-stream";

    public static final int MAX_FILE_SIZE = 10000;

    // ==================================================
    // Socket & server code
    // ==================================================

    /**
     * Starts a HTTP server to given port.<p>
     * Throws an IOException if the socket is already in use
     */
    public void init( Parameters params, AServProtocolManager manager ) throws AServException {
	this.manager = manager;
	tcpPort = Integer.parseInt( (String)params.getParameter( "http" ) );
	tcpHost = (String)params.getParameter( "host" ) ;

	/*
	try {
	    final ServerSocket ss = new ServerSocket( tcpPort );
	    Thread t = new Thread( new Runnable() {
		    public void run() {
			try { while( true ) new HTTPSession( ss.accept());
			} catch ( IOException ioe ) { ioe.printStackTrace(); }
		    }
		});
	    t.setDaemon( true );
	    t.start();
	} catch (Exception e) {
	    throw new AServException ( "Cannot launch HTTP Server" , e );
	}
	*/

	// ********************************************************************
	// JE: Jetty implementation
	server = new Server(tcpPort);

	Handler handler = new AbstractHandler(){
		public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) 
		    throws IOException, ServletException
		{
		    String method = request.getMethod();
		    //uri = URLDecoder.decode( request.getURI(), "iso-8859-1" );
		    // Should be decoded?
		    String uri = request.getPathInfo();
		    Properties params = new Properties();
		    try { decodeParms( request.getQueryString(), params ); }
		    catch ( Exception e) {};
		    // I do not decode them here because it is useless
		    // See below how it is done.
		    Properties header = new Properties();
		    Enumeration headerNames = request.getHeaderNames();
		    while(headerNames.hasMoreElements()) {
			String headerName = (String)headerNames.nextElement();
			header.setProperty( headerName, request.getHeader(headerName) );
		    }

		    // Get the content if any
		    // Multi part?
		    // This is supposed to be only an uploaded file
		    // We use jetty MultiPartFilter to decode this file,
		    // and only this
		    String mimetype = request.getContentType();
		    if ( mimetype != null && mimetype.startsWith("multipart/form-data") ) {
			MultiPartFilter filter = new MultiPartFilter();
			// This is in fact useless
			ParameterServletResponseWrapper dummyResponse =
			    new ParameterServletResponseWrapper( response );
			// In theory, the filter must be inited with a FilterConfig
			// filter.init( new FilterConfig);
			// This filter config must have a javax.servlet.context.tempdir attribute
			// and a ServletConxtext with parameter "deleteFiles"
			// Apparently the Jetty implementation uses System defaults
			// if no FilterConfig
			// e.g., it uses /tmp and keeps the files
			filter.doFilter( request, dummyResponse, new Chain() );
			// Extract parameters from response
			if ( request.getAttribute("content") != null )
			    params.setProperty( "filename", request.getAttribute("content").toString() );
			filter.destroy();
		    } else if ( mimetype != null && mimetype.startsWith("text/xml") ) {
			// Most likely Web service request
			int length = request.getContentLength();
			char [] mess = new char[length+1];
			try { 
			    new BufferedReader(new InputStreamReader(request.getInputStream())).read( mess, 0, length);
			} catch (Exception e) {
			    e.printStackTrace(); // To clean up
			}
			params.setProperty( "content", new String( mess ) );
		    }

		    // Get the answer (HTTP)
		    Response r = serve( uri, method, header, params );

		    // Return it
		    response.setContentType(r.getContentType());
		    //response.setStatus(r.getStatus());
		    response.setStatus(HttpServletResponse.SC_OK);
		    response.getWriter().println(r.getData());
		    // r.getStatus(); r.getContentType; r.getData();
		    ((Request)request).setHandled(true);
		}
	    };
	server.setHandler(handler);

	// Common part
	try { server.start(); }
	catch (Exception e) {
	    throw new AServException ( "Cannot launch HTTP Server" , e );
	}
	//server.join();

	// ********************************************************************
	if ( params.getParameter( "wsdl" ) != null ){
	    wsmanager = new WSAServProfile();
	    if ( wsmanager != null ) wsmanager.init( params, manager );
	}
	myId = "LocalHTMLInterface";
	serverId = "dummy";
	localId = 0;

    }

    /**
     * Je//: should certainly do more than that!
     */
    public void close(){
	if ( wsmanager != null ) wsmanager.close();
	if ( server != null ) {
	    try { server.stop(); }
            catch (Exception e) { e.printStackTrace(); }
	}
    }
    
    // ==================================================
    // API parts
    // ==================================================

    /**
     * Override this to customize the server.<p>
     *
     * (By default, this delegates to serveFile() and allows directory listing.)
     *
     * @parm uri	Percent-decoded URI without parameters, for example "/index.cgi"
     * @parm method	"GET", "POST" etc.
     * @parm parms	Parsed, percent decoded parameters from URI and, in case of POST, data.
     * @parm header	Header entries, percent decoded
     * @return HTTP response, see class Response for details
     */
    public Response serve( String uri, String method, Properties header, Properties parms ) {
	if ( debug >= 1 ) System.err.println( method + " '" + uri + "' " );
	Enumeration en = header.propertyNames();
	while ( en.hasMoreElements()) {
	    String value = (String)en.nextElement();
	    //System.err.println( "  HDR: '" + value + "' = '" +
	    //			header.getProperty( value ) + "'" );
	}
	/*
	e = parms.propertyNames();
	while ( e.hasMoreElements()) {
	    String value = (String)e.nextElement();
	    //System.err.println( "  PRM: '" + value + "' = '" +parms.getProperty( value ) + "'" );
	}
	*/

	// Convert parms to parameters
	Parameters params = new BasicParameters();
	Enumeration e = parms.propertyNames();
	while ( e.hasMoreElements()) {
	    String value = (String)e.nextElement();
	    if ( debug > 1 ) System.err.println( "  PRM: '" + value + "' = '" +parms.getProperty( value ) + "'" );
	    if ( value.startsWith( "paramn" ) ){
		params.setParameter( parms.getProperty( value ),
				     parms.getProperty( "paramv"+value.substring( 6 ) ) );
	    } else if ( !value.startsWith( "paramv" ) ) {
		params.setParameter( value, parms.getProperty( value ) );
	    }
	}
	
	int start = 0;
	if ( uri.charAt(0) == '/' ) start = 1;
	int end = uri.indexOf( '/', start+1 );
	String oper = "";
	if ( end != -1 ) {
	    oper = uri.substring( start, end );
	    start = end+1;
	} else {
	    // Old implementation
	    oper = uri.substring( start );
	    start = uri.length();
	    // No '/' after the tag cause problems, send redirect
	    //uri += "/";
	    //Response r = new Response( HTTP_REDIRECT, MIME_HTML,
	    // 			       "<html><body>Redirected: <a href=\""+uri+"\">" +uri+"</a></body></html>");
	//r.addHeader( "Location", uri );
	//return r;
	}

	if ( oper.equals( "aserv" ) ){
	    if ( wsmanager != null ) {
		return new Response( HTTP_OK, MIME_HTML, wsmanager.protocolAnswer( uri, uri.substring(start), header, params ) );
	    } else {
		// This is not correct: I shoud return an error
		return new Response( HTTP_OK, MIME_HTML, "<html><head></head><body>"+about()+"</body></html>" );
	    }
	} else if ( oper.equals( "admin" ) ){
	    return adminAnswer( uri, uri.substring(start), header, params );
	} else if ( oper.equals( "html" ) ){
	    return htmlAnswer( uri, uri.substring(start), header, params );
	} else if ( oper.equals( "wsdl" ) ){
	    return wsdlAnswer(uri, uri.substring(start), header, params);
	} else {
	    //return serveFile( uri, header, new File("."), true );
	    return new Response( HTTP_OK, MIME_HTML, "<html><head></head><body>"+about()+"</body></html>" );
	}
    }

    protected String about() {
	return "<h1>Alignment Server</h1><center>$Id$<br />"
	    + "<center><a href=\"/html/\">Access</a></center>"
	    + "(C) INRIA Rh&ocirc;ne-Alpes, 2006-2007<br />"
	    + "<a href=\"http://alignapi.gforge.inria.fr\">http://alignapi.gforge.inria.fr</a>"
	    + "</center>";
    }

    /**
     * HTTP administration interface
     * Allows some limited administration of the server through HTTP
     */
    public Response adminAnswer( String uri, String perf, Properties header, Parameters params ) {
	if ( debug > 0 ) System.err.println("ADMIN["+perf+"]");
	String msg = "";
        if ( perf.equals("listalignments") ){
	    msg = "<h1>Available alignments</h1><ul compact=\"1\">";
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension("id");
		msg += "<li><a href=\"../html/retrieve?method=fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor&id="+id+"\">"+id+"</a></li>";
	    }
	    msg += "</ul>";
	} else if ( perf.equals("listmethods") ){
	    msg = "<h1>Embedded classes</h1>\n<h2>Methods</h2><ul compact=\"1\">";
	    for( Iterator it = manager.listmethods().iterator(); it.hasNext(); ) {
		msg += "<li>"+it.next()+"</li>";
	    }
	    msg += "</ul>";
	    msg += "<h2>Renderers</h2><ul compact=\"1\">";
	    for( Iterator it = manager.listrenderers().iterator(); it.hasNext(); ) {
		msg += "<li>"+it.next()+"</li>";
	    }
	    msg += "</ul>";
	    msg += "<h2>Services</h2><ul compact=\"1\">";
	    for( Iterator it = manager.listservices().iterator(); it.hasNext(); ) {
		msg += "<li>"+it.next()+"</li>";
	    }
	    msg += "</ul>";
	} else if ( perf.equals("prmsqlquery") ){
	    msg = "<h1>SQL query</h1><form action=\"sqlquery\">Query:<br /><textarea name=\"query\" rows=\"20\" cols=\"80\">SELECT \nFROM \nWHERE </textarea> (sql)<br /><small>An SQL SELECT query</small><br /><input type=\"submit\" value=\"Query\"/></form>";
	} else if ( perf.equals("sqlquery") ){
	    String answer = manager.query( (String)params.getParameter("query") );
	    msg = "<pre>"+answer+"</pre>";
	} else if ( perf.equals("about") ){
	    msg = about();
	} else if ( perf.equals("shutdown") ){
	    manager.close();
	    msg = "Server shut down";
	} else if ( perf.equals("prmreset") ){
	    msg = perf;
	} else if ( perf.equals("prmflush") ){
	    msg = perf;
	} else if ( perf.equals("addservice") ){
	    msg = perf;
	} else if ( perf.equals("addmethod") ){
	    msg = perf;
	} else if ( perf.equals("addrenderer") ){
	    msg = perf;
	} else if ( perf.equals("") ) {
	    msg = "<h1>Alignment server administration</h1><ul compact=\"1\">";
	    msg += "<li><form action=\"listalignments\"><input type=\"submit\" value=\"Available alignments\"/></form></li>";
	    msg += "<li><form action=\"listmethods\"><input type=\"submit\" value=\"Embedded classes\"/></form></li>";
	    msg += "<li><form action=\"prmsqlquery\"><input type=\"submit\" value=\"SQL Query\"/></form></li>";
	    msg += "<li><form action=\"prmflush\"><input type=\"submit\" value=\"Flush caches\"/></form></li>";
	    msg += "<li><form action=\"prmreset\"><input type=\"submit\" value=\"Reset server\"/></form></li>";
	    msg += "<li><form action=\"shutdown\"><input type=\"submit\" value=\"Shutdown\"/></form></li>";
	    msg += "<li><form action=\"..\"><input type=\"submit\" value=\"About\"/></form></li>";
	    msg += "<li><form action=\"../html/\"><input type=\"submit\" value=\"User interface\"/></form></li>";
	    msg += "</ul>";
	} else {
	    msg = "Cannot understand: "+perf;
	}
	return new Response( HTTP_OK, MIME_HTML, "<html><head></head><body>"+msg+"<hr /><center><small><a href=\".\">Alignment server administration</a></small></center></body></html>" );
    }

    /**
     * User friendly HTTP interface
     * uses the protocol but offers user-targeted interaction
     */
    public Response htmlAnswer( String uri, String perf, Properties header, Parameters params ) {
	//System.err.println("HTML["+perf+"]");
	String msg = "";
	if ( perf.equals("prmstore") ) {
	    msg = "<h1>Store an alignment</h1><form action=\"store\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    // JE: only those non stored please (retrieve metadata + stored)
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension("id");
		params.setParameter("id", id);
		if ( !manager.storedAlignment( new Message(newId(),(Message)null,myId,serverId,"", params ) ) ){
		msg += "<option value=\""+id+"\">"+id+"</option>";
		}
	    }
	    msg += "</select><br />";
	    msg += "<input type=\"submit\" value=\"Store\"/></form>";
	} else if ( perf.equals("store") ) {
	    // here should be done the switch between store and load/store
	    String id = (String)params.getParameter("id");
	    String url = (String)params.getParameter("url");
	    if ( url != null && !url.equals("") ) { // Load the URL
		Message answer = manager.load( new Message(newId(),(Message)null,myId,serverId,"", params) );
		if ( answer instanceof ErrorMsg ) {
		    msg = testErrorMessages( answer );
		} else {
		    id = answer.getContent();
		}
	    }
	    if ( id != null ){ // Store it
		Message answer = manager.store( new Message(newId(),(Message)null,myId,serverId,id, params) );
		if ( answer instanceof ErrorMsg ) {
		    msg = testErrorMessages( answer );
		} else {
		    msg = "<h1>Alignment stored</h1>";
		    msg += displayAnswer( answer );
		}
	    }
	} else if ( perf.equals("prmcut") ) {
	    msg ="<h1>Trim alignments</h1><form action=\"cut\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension("id");
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br />";
	    msg += "Methods: <select name=\"method\"><option value=\"hard\">hard</option><option value=\"perc\">perc</option><option value=\"best\">best</option><option value=\"span\">span</option><option value=\"prop\">prop</option></select><br />Threshold: <input type=\"text\" name=\"threshold\" size=\"4\"/> <small>A value between 0. and 1. with 2 digits</small><br /><input type=\"submit\" name=\"action\" value=\"Trim\"/><br /></form>";
	} else if ( perf.equals("cut") ) {
	    String id = (String)params.getParameter("id");
	    String threshold = (String)params.getParameter("threshold");
	    if ( id != null && !id.equals("") && threshold != null && !threshold.equals("") ){ // Trim it
		Message answer = manager.cut( new Message(newId(),(Message)null,myId,serverId,id, params) );
		if ( answer instanceof ErrorMsg ) {
		    msg = testErrorMessages( answer );
		} else {
		    msg = "<h1>Alignment trimed</h1>";
		    msg += displayAnswer( answer );
		}
	    }
	} else if ( perf.equals("prmalign") ) {
	    msg ="<h1>Match ontologies</h1><form action=\"align\">Ontology 1: <input type=\"text\" name=\"onto1\" size=\"80\"/> (uri)<br />Ontology 2: <input type=\"text\" name=\"onto2\" size=\"80\"/> (uri)<br /><small>These are the URL of places where to find these ontologies. They must be reachable by the server (i.e., file:// URI are acceptable if they are on the server)</small><br /><!--input type=\"submit\" name=\"action\" value=\"Find\"/><br /-->Methods: <select name=\"method\">";
	    for( Iterator it = manager.listmethods().iterator(); it.hasNext(); ) {
		String id = (String)it.next();
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br />Initial alignment id:  <select name=\"id\"><option value=\"\" selected=\"1\"></option>";
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension("id");
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br />";
	    msg += "<input type=\"submit\" name=\"action\" value=\"Match\"/>  <input type=\"checkbox\" name=\"force\" /> Force<br />";
	    msg += "Additional parameters:<br /><input type=\"text\" name=\"paramn1\" size=\"15\"/> = <input type=\"text\" name=\"paramv1\" size=\"65\"/><br /><input type=\"text\" name=\"paramn2\" size=\"15\"/> = <input type=\"text\" name=\"paramv2\" size=\"65\"/><br /><input type=\"text\" name=\"paramn3\" size=\"15\"/> = <input type=\"text\" name=\"paramv3\" size=\"65\"/><br /><input type=\"text\" name=\"paramn4\" size=\"15\"/> = <input type=\"text\" name=\"paramv4\" size=\"65\"/></form>";
	} else if ( perf.equals("align") ) {
	    Message answer = manager.align( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer );
	    } else {
		msg = "<h1>Alignment results</h1>";
		msg += displayAnswer( answer );
	    }
	} else if ( perf.equals("prmfind") ) {
	    msg ="<h1>Find alignments between ontologies</h1><form action=\"find\">Ontology 1: <input type=\"text\" name=\"onto1\" size=\"80\"/> (uri)<br />Ontology 2: <input type=\"text\" name=\"onto2\" size=\"80\"/> (uri)<br /><small>These are the URI identifying the ontologies. Not those of places where to upload them.</small><br /><input type=\"submit\" name=\"action\" value=\"Find\"/></form>";
	} else if ( perf.equals("find") ) {
	    Message answer = manager.existingAlignments( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer );
	    } else {
		msg = "<h1>Found alignments</h1>";
		msg += displayAnswer( answer );
	    }
	} else if ( perf.equals("prmretrieve") ) {
	    msg = "<h1>Retrieve alignment</h1><form action=\"retrieve\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension("id");
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br />";
	    msg += "Rendering: <select name=\"method\">";
	    for( Iterator it = manager.listrenderers().iterator(); it.hasNext(); ) {
		String id = (String)it.next();
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br /><input type=\"submit\" value=\"Retrieve\"/></form>";
	} else if ( perf.equals("retrieve") ) {
	    Message answer = manager.render( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer );
	    } else {
		// Depending on the type we should change the MIME type
		// This should be returned in answer.getParameters()
		return new Response( HTTP_OK, MIME_HTML, answer.getContent() );
	    }
	    // Metadata not done yet
	} else if ( perf.equals("prmmetadata") ) {
	    msg = "<h1>Retrieve alignment metadata</h1><form action=\"metadata\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension("id");
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br /><input type=\"submit\" value=\"Get metadata\"/></form>";
	} else if ( perf.equals("metadata") ) {
	    Message answer = manager.render( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    //System.err.println("Content: "+answer.getContent());
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer );
	    } else {
		// Depending on the type we should change the MIME type
		return new Response( HTTP_OK, MIME_HTML, answer.getContent() );
	    }
	    // render
	    // Alignment in HTML can be rendre or metadata+tuples
	} else if ( perf.equals("prmload") ) {
	    // Should certainly be good to offer store as well
	    msg = "<h1>Load an alignment</h1><form action=\"load\">Alignment URL: <input type=\"text\" name=\"url\" size=\"80\"/> (uri)<br /><small>This is the URL of the place where to find this alignment. It must be reachable by the server (i.e., file:// URI is acceptable if it is on the server).</small><br /><input type=\"submit\" value=\"Load\"/></form>";
	    //msg += "Alignment file: <form ENCTYPE=\"text/xml; charset=utf-8\" action=\"loadfile\" method=\"POST\">";
	    msg += "Alignment file: <form enctype=\"multipart/form-data\" action=\"load\" method=\"POST\">";
	    msg += " <input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\""+MAX_FILE_SIZE+"\"/>";
	    msg += "<input name=\"content\" type=\"file\" size=\"35\">";
	    msg += "<br /><small>NOTE: Max file size is "+(MAX_FILE_SIZE/1024)+"KB; this is experimental but works</small><br />";
	    msg += " <input type=\"submit\" Value=\"Upload\">";
	    msg +=  " </form>";
	} else if ( perf.equals("load") ) {
	    // load
	    Message answer = manager.load( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer );
	    } else {
		msg = "<h1>Alignment loaded</h1>";
		msg += displayAnswer( answer );
	    }
	} else if ( perf.equals("prmtranslate") ) {
	    msg = "<h1>Translate query</h1><form action=\"translate\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension("id");
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br />";
	    msg += "SPARQL query:<br /> <textarea name=\"query\" rows=\"20\" cols=\"80\">PREFIX foaf: <http://xmlns.com/foaf/0.1/>\nSELECT *\nFROM <>\nWHERE {\n\n}</textarea> (SPARQL)<br /><small>A SPARQL query (PREFIX prefix: &lt;uri&gt; SELECT variables FROM &lt;url&gt; WHERE { triples })</small><br /><input type=\"submit\" value=\"Translate\"/></form>";
	} else if ( perf.equals("translate") ) {
	    Message answer = manager.translate( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer );
	    } else {
		msg = "<h1>Message translation</h1>";
		msg += "<h2>Initial message</h2><pre>"+((String)params.getParameter("query")).replaceAll("&", "&amp;").replaceAll("<", "&lt;")+"</pre>";
		msg += "<h2>Translated message</h2><pre>";
		msg += answer.HTMLString().replaceAll("&", "&amp;").replaceAll("<", "&lt;");
		msg += "</pre>";
	    }
	} else if ( perf.equals("prmmetadata") ) {
	    msg = "<h1>Retrieve alignment metadata</h1><form action=\"metadata\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension("id");
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br /><input type=\"submit\" value=\"Get metadata\"/></form>";
	} else if ( perf.equals("metadata") ) {
	    Message answer = manager.render( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    //System.err.println("Content: "+answer.getContent());
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer );
	    } else {
		// Depending on the type we should change the MIME type
		return new Response( HTTP_OK, MIME_HTML, answer.getContent() );
	    }
	    // render
	    // Alignment in HTML can be rendre or metadata+tuples
	} else if ( perf.equals("") ) {
	    msg = "<h1>Alignment Server commands</h1><ul compact=\"1\">";
	    msg += "<li><form action=\"prmfind\"><input type=\"submit\" value=\"Find an alignment for ontologies\"/></form></li>";
	    msg += "<li><form action=\"prmalign\"><input type=\"submit\" value=\"Match ontologies\"/></form></li>";
	    msg += "<li><form action=\"prmcut\"><input type=\"submit\" value=\"Trim an alignment above some threshold\"/></form></li>";
	    msg += "<li><form action=\"prmload\"><input type=\"submit\" value=\"Load alignments\"/></form></li>";
	    msg += "<li><form action=\"prmstore\"><input type=\"submit\" value=\"Store an alignment in the server\"/></form></li>";
	    msg += "<li><form action=\"prmretrieve\"><input type=\"submit\" value=\"Retrieve an alignment from id\"/></form></li>";
	    msg += "<li><form action=\"../admin/\"><input type=\"submit\" value=\"Server management\"/></form></li>";
	    msg += "</ul>";
	} else {
	    msg = "Cannot understand command "+perf;
	}
	return new Response( HTTP_OK, MIME_HTML, "<html><head></head><body>"+msg+"<hr /><center><small><a href=\".\">Alignment server</a></small></center></body></html>" );
    }

    // ===============================================
    // Util

	public Response wsdlAnswer(String uri, String perf, Properties header, Parameters params  ) {
	    String msg = "";
	    try {
		FileReader fr = null;
		String temp;
		// JE: I would not... but absolutely not do this
		fr = new FileReader ("WSAlignSVC.wsdl");
		BufferedReader inFile = new BufferedReader( fr );
		while ((temp = inFile.readLine()) != null) {
		    //msg = msg + line + "\n";
		    msg =msg + temp;
		}
		if (fr != null)  fr.close();
	    } catch (IOException e) { e.printStackTrace(); }
	    return new Response( HTTP_OK, MIME_XML, msg );
	}	 


    private String testErrorMessages( Message answer ) {
	return "<h1>Alignment error</h1>"+answer.HTMLString();
    }

    private String displayAnswer ( Message answer ) {
	return answer.HTMLString();
    }

    private int newId() { return localId++; }

	private void decodeParms( String parms, Properties p ) throws InterruptedException {
	    if ( parms == null ) return;

	    StringTokenizer st = new StringTokenizer( parms, "&" );
	    while ( st.hasMoreTokens())	{
		String next = st.nextToken();
		int sep = next.indexOf( '=' );
		if ( sep >= 0 )

		try {
		    p.put( URLDecoder.decode( next.substring( 0, sep ), "iso-8859-1" ).trim(),
			   URLDecoder.decode( next.substring( sep+1 ), "iso-8859-1" ));
		} catch (Exception e) {}; //never thrown
	    }
	}
    // ==================================================
    // HTTP Machinery

    /**
     * Handles one session, i.e. parses the HTTP request
     * and returns the response.
     */
    private class HTTPSession implements Runnable {
	public HTTPSession( Socket s ) {
	    mySocket = s;
	    Thread t = new Thread( this );
	    t.setDaemon( true );
	    t.start();
	}

	public void run() {
	    try	{
		InputStream is = mySocket.getInputStream();
		if ( is == null) return;
		BufferedReader in = new BufferedReader( new InputStreamReader( is ));
		
		// Read the request line
		StringTokenizer st = new StringTokenizer( in.readLine());
		if ( !st.hasMoreTokens())
		    sendError( HTTP_BADREQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html" );
		
		String method = st.nextToken();

		if ( !st.hasMoreTokens())
		    sendError( HTTP_BADREQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html" );

		String uri = null;
		try {
		    uri = URLDecoder.decode( st.nextToken(), "iso-8859-1" );
		} catch (Exception e) {}; //never thrown
		//String uri = decodePercent( st.nextToken());

		// Decode parameters from the URI
		Properties parms = new Properties();
		int qmi = uri.indexOf( '?' );
		if ( qmi >= 0 )	{
		    decodeParms( uri.substring( qmi+1 ), parms );
		    try {
			uri = URLDecoder.decode( uri.substring( 0, qmi ), "iso-8859-1" );
		    } catch (Exception e) {}; //never thrown
		    //uri = decodePercent( uri.substring( 0, qmi ));
		}

		// If there's another token, it's protocol version,
		// followed by HTTP headers. Ignore version but parse headers.
		Properties header = new Properties();
		if ( st.hasMoreTokens()) {
		    String line = in.readLine();
		    while ( line.trim().length() > 0 ) {
			int p = line.indexOf( ':' );
			header.put( line.substring(0,p).trim(), line.substring(p+1).trim());
			line = in.readLine();
		    }
		}

		// If the method is POST, there may be parameters
		// in data section, too, read it:
		if ( method.equalsIgnoreCase( "POST" ))	{
		    long size = 0x7FFFFFFFFFFFFFFFl;
		    String contentLength = header.getProperty("Content-Length");
		    if (contentLength != null) {
			try { size = Integer.parseInt(contentLength); }
			catch (NumberFormatException ex) {}
		    }
		    String postLine = "";
		    char buf[] = new char[512];
		    int read = in.read(buf);
		    while ( read >= 0 && size > 0 && !postLine.endsWith("\r\n") ) {
			size -= read;
			postLine += String.valueOf(buf);
			if ( size > 0 ) read = in.read(buf);
		    }
		    postLine = postLine.trim();
		    // JE: it should not decode...
		    //decodeParms( postLine, parms );
		    // JE: Display the parameters to know what we have
		    //System.err.println("POST detected at "+uri);
		    //System.err.println(method+" [ "+header+" ] ");
		    Enumeration e = header.propertyNames();
		    while ( e.hasMoreElements()) {
			String value = (String)e.nextElement();
			//System.err.println( "  HDR: '" + value + "' = '" +
			//		    header.getProperty( value ) + "'" );
		    }
		    e = parms.propertyNames();
		    while ( e.hasMoreElements()) {
			String value = (String)e.nextElement();
			//System.err.println( "  PRM: '" + value + "' = '" +parms.getProperty( value ) + "'" );
		    }
		    //System.err.println("The content is\n"+postLine);
		    parms.put( "content", postLine );
		}

		// Ok, now do the serve()
		Response r = serve( uri, method, header, parms );
		if ( r == null )
		    sendError( HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: Serve() returned a null response." );
		else
		    sendResponse( r.status, r.mimeType, r.header, r.data );
		in.close();
	    } catch ( IOException ioe )	{
		try {
		    sendError( HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
		} catch ( Throwable t ) {}
	    } catch ( InterruptedException ie )	{
		// Thrown by sendError, ignore and exit the thread.
	    }
	}

	/**
	 * Decodes the percent encoding scheme. <br/>
	 * For example: "an+example%20string" -> "an example string"
	private String decodePercent( String str ) throws InterruptedException {
	    try	{
		StringBuffer sb = new StringBuffer();
		for( int i=0; i<str.length(); i++ ) {
		    char c = str.charAt( i );
		    switch ( c ) {
		    case '+':
			sb.append( ' ' );
			break;
		    case '%':
			sb.append((char)Integer.parseInt( str.substring(i+1,i+3), 16 ));
			i += 2;
			break;
		    default:
			sb.append( c );
			break;
		    }
		}
		return new String( sb.toString().getBytes());
	    } catch( Exception e ) {
		sendError( HTTP_BADREQUEST, "BAD REQUEST: Bad percent-encoding." );
		return null;
	    }
	}
	 */

	/**
	 * Decodes parameters in percent-encoded URI-format
	 * ( e.g. "name=Jack%20Daniels&pass=Single%20Malt" ) and
	 * adds them to given Properties.
	 */
	private void decodeParms( String parms, Properties p ) throws InterruptedException {
	    if ( parms == null ) return;

	    StringTokenizer st = new StringTokenizer( parms, "&" );
	    while ( st.hasMoreTokens())	{
		String next = st.nextToken();
		int sep = next.indexOf( '=' );
		if ( sep >= 0 )

		try {
		    p.put( URLDecoder.decode( next.substring( 0, sep ), "iso-8859-1" ).trim(),
			   URLDecoder.decode( next.substring( sep+1 ), "iso-8859-1" ));
		} catch (Exception e) {}; //never thrown
	    }
	}

	/**
	 * Returns an error message as a HTTP response and
	 * throws InterruptedException to stop furhter request processing.
	 */
	private void sendError( String status, String msg ) throws InterruptedException {
	    sendResponse( status, MIME_PLAINTEXT, null, new ByteArrayInputStream( msg.getBytes()));
	    throw new InterruptedException();
	}

	/**
	 * Sends given response to the socket.
	 */
	private void sendResponse( String status, String mime, Properties header, InputStream data ) {
	    try {
		if ( status == null )
		    throw new Error( "sendResponse(): Status can't be null." );

		OutputStream out = mySocket.getOutputStream();
		PrintWriter pw = new PrintWriter( out );
		pw.print("HTTP/1.0 " + status + " \r\n");
		
		if ( mime != null )
		    pw.print("Content-Type: " + mime + "\r\n");
		
		if ( header == null || header.getProperty( "Date" ) == null )
		    pw.print( "Date: " + gmtFrmt.format( new Date()) + "\r\n");

		if ( header != null ) {
		    Enumeration e = header.keys();
		    while ( e.hasMoreElements()) {
			String key = (String)e.nextElement();
			String value = header.getProperty( key );
			pw.print( key + ": " + value + "\r\n");
		    }
		}

		pw.print("\r\n");
		pw.flush();

		if ( data != null ) {
		    byte[] buff = new byte[2048];
		    while (true) {
			int read = data.read( buff, 0, 2048 );
			if (read <= 0)
			    break;
			out.write( buff, 0, read );
		    }
		}
		out.flush();
		out.close();
		if ( data != null ) data.close();
	    } catch( IOException ioe ) {
		// Couldn't write? No can do.
		try { mySocket.close(); } catch( Throwable t ) {}
	    }
	}

	private Socket mySocket;
    };

    /**
     * URL-encodes everything between "/"-characters.
     * Encodes spaces as '%20' instead of '+'.
     */
    private String encodeUri( String uri ) {
	String newUri = "";
	StringTokenizer st = new StringTokenizer( uri, "/ ", true );
	try {
	    while ( st.hasMoreTokens()) {
		String tok = st.nextToken();
		if ( tok.equals( "/" ))
		    newUri += "/";
		else if ( tok.equals( " " ))
		    newUri += "%20";
		else
		    newUri += URLEncoder.encode( tok, "iso-8859-1" );
	    }
	} catch (Exception e) {}; // never reported exception
	return newUri;
    }

    /*
    protected String file loadFile( req ){
	DiskFileItemFactory factory = new DiskFileItemFactory();
	//factory.setSizeThreshold(XXX);
	//factory.setRepository("/tmp"); // System dependent
	ServletFileUpload upload = new ServletFileUpload( factory );
	//upload.setSizeMax(XXX);
	List items = upload.parseRequest( req );
    }

    String mimetype=httpRequest.getMimeType();
    HashMap multipartNVP=null;
    if (mimetype!=null)
    {   if(mimetype.equals("multipart/form-data"))
    {   MultiPartRequest mpr=new MultiPartRequest(httpRequest);
    String[] names=mpr.getPartNames();
    multipartNVP=new HashMap(names.length);
    for(int i=0; i<names.length;i++)
    {   String filename=mpr.getFilename(names[i]);
                     if(filename!=null)
                     {   // uploaded file
                        URIdentifier partURI = new URIdentifier(URI_UPLOAD+Integer.toString(i));
                        requestURI.addArg(names[i],partURI.toString());
                        requestArguments.put( partURI, ParameterUploadAspect.create(new AlwaysExpiredMeta("type/unknown",0), mpr,names[i], filename) );
                        multipartNVP.put(names[i], new ByteArrayInputStream(filename.getBytes()));
                     }
                     else //Must be form data so add to NVP
                     {   multipartNVP.put(names[i], mpr.getInputStream(names[i]));
                     }
                  }
               }
            }
    */
    // From Jetty
     private String value(String nameEqualsValue) {
	 String value=nameEqualsValue.substring(nameEqualsValue.indexOf('=')+1).trim();
	 int i=value.indexOf(';');
         if(i>0) value=value.substring(0,i);
         if(value.startsWith("\"")) {
             value=value.substring(1,value.indexOf('"',1));
         } else {
             i=value.indexOf(' ');
             if(i>0) value=value.substring(0,i);
	 }
         return value;
     }

    // ==================================================
    // File browsing stuff
    // JE: MOST OF THIS CODE WILL BE USELESS

    File myFileDir;

    /**
     * Serves file from homeDir and its' subdirectories (only).
     * Uses only URI, ignores all headers and HTTP parameters.
     */
    public Response serveFile( String uri, Properties header, File homeDir,
							   boolean allowDirectoryListing ) {
	//System.err.println("SANDBOX");
	// Make sure we won't die of an exception later
	if ( !homeDir.isDirectory())
	    return new Response( HTTP_INTERNALERROR, MIME_PLAINTEXT,
								 "INTERNAL ERRROR: serveFile(): given homeDir is not a directory." );

	// Remove URL arguments
	uri = uri.trim().replace( File.separatorChar, '/' );
	if ( uri.indexOf( '?' ) >= 0 )
	    uri = uri.substring(0, uri.indexOf( '?' ));

	// Prohibit getting out of current directory
	if ( uri.startsWith( ".." ) || uri.endsWith( ".." ) || uri.indexOf( "../" ) >= 0 )
	    return new Response( HTTP_FORBIDDEN, MIME_PLAINTEXT,
								 "FORBIDDEN: Won't serve ../ for security reasons." );

	File f = new File( homeDir, uri );
	if ( !f.exists())
	    return new Response( HTTP_NOTFOUND, MIME_PLAINTEXT,
								 "Error 404, file not found." );

	// List the directory, if necessary
	if ( f.isDirectory()) {
	    // Browsers get confused without '/' after the
	    // directory, send a redirect.
	    if ( !uri.endsWith( "/" )) {
		uri += "/";
		Response r = new Response( HTTP_REDIRECT, MIME_HTML,
					   "<html><body>Redirected: <a href=\""+uri+"\">" +uri+"</a></body></html>");
		r.addHeader( "Location", uri );
		return r;
	    }
	    
	    // First try index.html and index.htm
	    if ( new File( f, "index.html" ).exists())
		f = new File( homeDir, uri + "/index.html" );
	    else if ( new File( f, "index.htm" ).exists())
		f = new File( homeDir, uri + "/index.htm" );

	    // No index file, list the directory
	    else if ( allowDirectoryListing ) {
		String[] files = f.list();
		String msg = "<html><body><h1>Directory " + uri + "</h1><br/>";

		if ( uri.length() > 1 ) {
		    String u = uri.substring( 0, uri.length()-1 );
		    int slash = u.lastIndexOf( '/' );
		    if ( slash >= 0 && slash  < u.length())
			msg += "<b><a href=\"" + uri.substring(0, slash+1) + "\">..</a></b><br/>";
		}

		for ( int i=0; i<files.length; ++i ) {
		    File curFile = new File( f, files[i] );
		    boolean dir = curFile.isDirectory();
		    if ( dir ) {
			msg += "<b>";
			files[i] += "/";
		    }

		    msg += "<a href=\"" + encodeUri( uri + files[i] ) + "\">" +
			files[i] + "</a>";

		    // Show file size
		    if ( curFile.isFile()) {
			long len = curFile.length();
			msg += " &nbsp;<font size=2>(";
			if ( len < 1024 )
			    msg += curFile.length() + " bytes";
			else if ( len < 1024 * 1024 )
			    msg += curFile.length()/1024 + "." + (curFile.length()%1024/10%100) + " KB";
			else
			    msg += curFile.length()/(1024*1024) + "." + curFile.length()%(1024*1024)/10%100 + " MB";
			msg += ")</font>";
		    }
		    msg += "<br/>";
		    if ( dir ) msg += "</b>";
		}
		return new Response( HTTP_OK, MIME_HTML, msg );
	    } else {
		return new Response( HTTP_FORBIDDEN, MIME_PLAINTEXT,
				     "FORBIDDEN: No directory listing." );
	    }
	}
	
	try {
	    // Get MIME type from file name extension, if possible
	    String mime = null;
	    int dot = f.getCanonicalPath().lastIndexOf( '.' );
	    if ( dot >= 0 )
		mime = (String)theMimeTypes.get( f.getCanonicalPath().substring( dot + 1 ).toLowerCase());
	    if ( mime == null )
		mime = MIME_DEFAULT_BINARY;

	    // Support (simple) skipping:
	    long startFrom = 0;
	    String range = header.getProperty( "Range" );
	    if ( range != null ) {
		if ( range.startsWith( "bytes=" )) {
		    range = range.substring( "bytes=".length());
		    int minus = range.indexOf( '-' );
		    if ( minus > 0 ) range = range.substring( 0, minus );
		    try	{
			startFrom = Long.parseLong( range );
		    } catch ( NumberFormatException nfe ) {}
		}
	    }

	    FileInputStream fis = new FileInputStream( f );
	    fis.skip( startFrom );
	    Response r = new Response( HTTP_OK, mime, fis );
	    r.addHeader( "Content-length", "" + (f.length() - startFrom));
	    r.addHeader( "Content-range", "" + startFrom + "-" +
			 (f.length()-1) + "/" + f.length());
	    return r;
	} catch( IOException ioe ) {
	    return new Response( HTTP_FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: Reading file failed." );
	}
    }

    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    private static Hashtable theMimeTypes = new Hashtable();
    static {
	StringTokenizer st = new StringTokenizer(
			"htm		text/html "+
			"html		text/html "+
			"txt		text/plain "+
			"asc		text/plain "+
			"gif		image/gif "+
			"jpg		image/jpeg "+
			"jpeg		image/jpeg "+
			"png		image/png "+
			"mp3		audio/mpeg "+
			"m3u		audio/mpeg-url " +
			"pdf		application/pdf "+
			"doc		application/msword "+
			"ogg		application/x-ogg "+
			"zip		application/octet-stream "+
			"exe		application/octet-stream "+
			"class		application/octet-stream " );
	while ( st.hasMoreTokens())
	    theMimeTypes.put( st.nextToken(), st.nextToken());
    }

    // ==================================================
    // License

    /**
     * GMT date formatter
     */
    private static java.text.SimpleDateFormat gmtFrmt;
    static {
	gmtFrmt = new java.text.SimpleDateFormat( "E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
	gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * The distribution licence
     */
    private static final String LICENCE =
	"Copyright (C) 2001,2005 by Jarno Elonen <elonen@iki.fi>\n"+
	"\n"+
	"Redistribution and use in source and binary forms, with or without\n"+
	"modification, are permitted provided that the following conditions\n"+
	"are met:\n"+
	"\n"+
	"Redistributions of source code must retain the above copyright notice,\n"+
	"this list of conditions and the following disclaimer. Redistributions in\n"+
	"binary form must reproduce the above copyright notice, this list of\n"+
	"conditions and the following disclaimer in the documentation and/or other\n"+
	"materials provided with the distribution. The name of the author may not\n"+
	"be used to endorse or promote products derived from this software without\n"+
	"specific prior written permission. \n"+
	" \n"+
	"THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\n"+
	"IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\n"+
	"OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\n"+
	"IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\n"+
	"INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\n"+
	"NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\n"+
	"DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\n"+
	"THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n"+
	"(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\n"+
	"OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
    /**
     * HTTP response.
     * Return one of these from serve().
     */
    public class Response {
	/**
	 * Default constructor: response = HTTP_OK, data = mime = 'null'
	 */
	public Response() {
	    this.status = HTTP_OK;
	}

	/**
	 * Basic constructor.
	 */
	public Response( String status, String mimeType, InputStream data ) {
	    this.status = status;
	    this.mimeType = mimeType;
	    this.data = data;
	}

	/**
	 * Convenience method that makes an InputStream out of
	 * given text.
	 */
	public Response( String status, String mimeType, String txt ) {
	    this.status = status;
	    this.mimeType = mimeType;
	    this.data = new ByteArrayInputStream( txt.getBytes());
	    // JE: Added
	    this.msg = txt;
	}

	/**
	 * Adds given line to the header.
	 */
	public void addHeader( String name, String value ) {
	    header.put( name, value );
	}


	/**
	 * HTTP status code after processing, e.g. "200 OK", HTTP_OK
	 */
	public String status;

	/**
	 * MIME type of content, e.g. "text/html"
	 */
	public String mimeType;

	/**
	 * Data of the response, may be null.
	 */
	public InputStream data;

	/**
	 * Headers for the HTTP response. Use addHeader()
	 * to add lines.
	 */
	public Properties header = new Properties();
	// JE: Added for testing Jetty
	public String msg;
	public String getStatus() { return status; };
	public String getContentType() { return mimeType; }
	public String getData() { return msg; }

    }

    /**
     * Two private cclasses for retrieving parameters
     */
    private class ParameterServletResponseWrapper extends ServletResponseWrapper  {
	private Map parameters;

	public ParameterServletResponseWrapper( ServletResponse r ){
	    super(r);
	};

	public Map getParameterMap(){ return parameters; }
 
	public void setParameterMap( Map m ){ parameters = m; }
 
     }

    private class Chain implements FilterChain {
 
	public void doFilter( ServletRequest request, ServletResponse response)
	    throws IOException, ServletException {
	    if ( response instanceof ParameterServletResponseWrapper &&
		 request instanceof ServletRequestWrapper ) {
		((ParameterServletResponseWrapper)response).setParameterMap( ((ServletRequestWrapper)request).getParameterMap() );
	    }
         }
 
     }
}

