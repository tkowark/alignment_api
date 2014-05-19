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

import fr.inrialpes.exmo.align.service.msg.Message;
import fr.inrialpes.exmo.align.service.msg.ErrorMsg;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;

import java.util.Locale;
import java.util.TimeZone;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Properties;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import java.net.URI;
import java.net.URISyntaxException;
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
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.Request;
import org.mortbay.servlet.MultiPartFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTPTransport: implements the HTTP connection of the server
 * Dispatch messages to the various services
 */

public class HTTPTransport {
    final static Logger logger = LoggerFactory.getLogger( HTTPTransport.class );

    private int tcpPort;
    private String tcpHost;
    private Server server;
    private AServProtocolManager manager;
    private Vector<AlignmentServiceProfile> services;

    private String myId;
    private String serverId;
    private int localId = 0;

    private String returnType = HTTPResponse.MIME_HTML;

    // ==================================================
    // Socket & server code
    // ==================================================

    /**
     * Starts a HTTP server to given port.<p>
     * Throws an exception if the socket is already in use
     */
    public void init( Properties params, AServProtocolManager manager, Vector<AlignmentServiceProfile> serv ) throws AServException {
	this.manager = manager;
	services = serv;
	tcpPort = Integer.parseInt( params.getProperty( "http" ) );
	tcpHost = params.getProperty( "host" ) ;

	/*
	try {
	    final ServerSocket ss = new ServerSocket( tcpPort );
	    Thread t = new Thread( new Runnable() {
		    public void run() {
			try { while( true ) new HTTPSession( ss.accept());
			} catch ( IOException ioe ) { logger.debug( "IGNORED Exception", ioe ); }
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

	// The handler deals with the request
	// most of its work is to deal with large content sent in specific ways 
	Handler handler = new AbstractHandler(){
		public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch ) throws IOException, ServletException {
		    String method = request.getMethod();
		    //uri = URLDecoder.decode( request.getURI(), "iso-8859-1" );
		    // Should be decoded?
		    String uri = request.getPathInfo();
		    Properties params = new Properties();
		    try { decodeParams( request.getQueryString(), params ); }
		    catch ( Exception e) {};
		    // I do not decode them here because it is useless
		    // See below how it is done.
		    Properties header = new Properties();
		    Enumeration headerNames = request.getHeaderNames();
		    while( headerNames.hasMoreElements() ) {
			String headerName = (String)headerNames.nextElement();
			header.setProperty( headerName, request.getHeader(headerName) );
		    }

		    // Get the content if any
		    // This is supposed to be only an uploaded file
		    // We use jetty MultiPartFilter to decode this file.
		    // Note that this could be made more uniform 
		    // with the text/xml part stored in a file as well.
		    String mimetype = request.getContentType();
		    // Multi part: the content provided by an upload HTML form
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
			// Apparently a bug from Jetty prevents from retrieving this
			if ( request.getParameter("pretty") != null )
			    params.setProperty( "pretty", request.getParameter("pretty").toString() );
			if ( request.getAttribute("content") != null )
			    params.setProperty( "filename", request.getAttribute("content").toString() );
			filter.destroy();
		    } else if ( mimetype != null && mimetype.startsWith("text/xml") ) {
			// Most likely Web service request (REST through POST)
			int length = request.getContentLength();
			if ( length > 0 ) {
			    char [] mess = new char[length+1];
			    try {
				new BufferedReader(new InputStreamReader(request.getInputStream())).read( mess, 0, length);
			    } catch ( Exception e ) {
				logger.debug( "IGNORED Exception", e );
			    }
			    params.setProperty( "content", new String( mess ) );
			}
		    // File attached to SOAP messages
		    } else if ( mimetype != null && mimetype.startsWith("application/octet-stream") ) {
         		File alignFile = new File(File.separator + "tmp" + File.separator + newId() +"XXX.rdf");
         		// check if file already exists - and overwrite if necessary.
         		if (alignFile.exists()) alignFile.delete();
               	 	FileOutputStream fos = new FileOutputStream(alignFile);
            		InputStream is = request.getInputStream();
			
           	        try {
			    byte[] buffer = new byte[4096];
			    int bytes=0; 
			    while (true) {
				bytes = is.read(buffer);
				if (bytes < 0) break;
				fos.write(buffer, 0, bytes);
			    }
            		} catch (Exception e) {
			} finally {
			    fos.flush();
			    fos.close();
			}
               		is.close();
			params.setProperty( "content", "" );
			params.setProperty( "filename" ,  alignFile.getAbsolutePath()  );
         	    } 

		    // Get the answer (HTTP)
		    HTTPResponse r = serve( uri, method, header, params );

		    // Return it
		    response.setContentType( r.getContentType() );
		    response.setStatus( HttpServletResponse.SC_OK );
		    response.getWriter().println( r.getData() );
		    ((Request)request).setHandled( true );
		}
	    };
	server.setHandler(handler);

	// Common part
	try { server.start(); }
	catch (Exception e) {
	    throw new AServException( "Cannot launch HTTP Server" , e );
	}
	//server.join();

	// ********************************************************************
	//if ( params.getProperty( "wsdl" ) != null ){
	//    wsmanager = new WSAServProfile();
	//    if ( wsmanager != null ) wsmanager.init( params, manager );
	//}
	//if ( params.getProperty( "http" ) != null ){
	//    htmanager = new HTMLAServProfile();
	//    if ( htmanager != null ) htmanager.init( params, manager );
	//}
	myId = "LocalHTMLInterface";
	serverId = manager.serverURL();
	logger.info( "Launched on {}/html/", serverId );
	localId = 0;
    }

    public void close(){
	if ( server != null ) {
	    try { server.stop(); }
            catch (Exception e) { logger.debug( "IGNORED Exception on close", e ); }
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
     * @param uri	Percent-decoded URI without parameters, for example "/index.cgi"
     * @param method	"GET", "POST" etc.
     * @param parms	Parsed, percent decoded parameters from URI and, in case of POST, data.
     * @param header	Header entries, percent decoded
     * @return HTTP response, see class Response for details
     */
    public HTTPResponse serve( String uri, String method, Properties header, Properties parms ) {
	logger.debug( "{} '{}'", method, uri );

	// Convert parms to parameters
	Properties params = new Properties();
	for ( String key : parms.stringPropertyNames() ) {
	    //logger.trace( "  PRM: '{}' = '{}'", key, parms.getProperty( key ) );
	    if ( key.startsWith( "paramn" ) ){
		params.setProperty( parms.getProperty( key ),
				     parms.getProperty( "paramv"+key.substring( 6 ) ) );
	    } else if ( !key.startsWith( "paramv" ) ) {
		params.setProperty( key, parms.getProperty( key ) );
	    }
	}
	
	int start = 0;
	while ( start < uri.length() && uri.charAt(start) == '/' ) start++;
	int end = uri.indexOf( '/', start+1 );
	String oper = "";
	if ( end != -1 ) {
	    oper = uri.substring( start, end );
	    start = end+1;
	} else {
	    oper = uri.substring( start );
	    start = uri.length();
	}
	logger.trace( "Oper: {}", oper );

	// Content negotiation first
	String accept = header.getProperty( "Accept" );
	returnType = HTTPResponse.MIME_HTML;
	if ( accept == null ) accept = header.getProperty( "accept" );
	logger.trace( "Accept header: {}", accept );
	if ( accept != null && !accept.equals("") ) {
	    int indexRXML = accept.indexOf( HTTPResponse.MIME_RDFXML );
	    if ( indexRXML == -1 ) indexRXML = accept.indexOf( HTTPResponse.MIME_XML );
	    int indexJSON = accept.indexOf( HTTPResponse.MIME_JSON );
	    if ( indexRXML != -1 ) {
		if ( indexJSON > indexRXML || indexJSON == -1 ) {
		    returnType = HTTPResponse.MIME_RDFXML;
		} else {
		    returnType = HTTPResponse.MIME_JSON;
		}
	    } else if ( indexJSON != -1 ) {
		returnType = HTTPResponse.MIME_JSON;
	    }
	}
	logger.trace( "Return MIME Type: {}", returnType );
	params.setProperty( "returnType", returnType );

	if ( oper.equals( "alid" ) ){ // Asks for an alignment by URI
	    return returnAlignment( uri, returnType );
	} else if ( oper.equals( "onid" ) ){ // Asks for a network by URI
		return returnNetwork( uri, returnType );		
	} else if ( oper.equals( "" ) ) {
	    // SHOULD BE ASSIGNED TO CONTENT NEGOCIATION AS WELL... (DEFAULT IN SERVERS)
	    //return serveFile( uri, header, new File("."), true );
	    return new HTTPResponse( HTTPResponse.HTTP_OK, HTTPResponse.MIME_HTML, "<html><head>"+HTMLAServProfile.HEADER+"</head><body>"+HTMLAServProfile.about()+"</body></html>" );
	} else {
	    // Selects the relevant service for the request
	    for ( AlignmentServiceProfile serv : services ) {
		if ( serv.accept( oper ) ) {
		    return new HTTPResponse( HTTPResponse.HTTP_OK, returnType, serv.process( uri, oper, uri.substring(start), header, params ) );
		}
	    }
	    return noManager( oper );
	}
    }

    protected HTTPResponse noManager( String type ) {
	if ( returnType == HTTPResponse.MIME_JSON ) {
	    return new HTTPResponse( HTTPResponse.HTTP_OK, HTTPResponse.MIME_JSON, "{ \"type\" : \"AServErrorMsg\",\n  \"content\" : \"No "+type+" service launched\"\n}" );
	} else if ( returnType == HTTPResponse.MIME_RDFXML ) {
	    return new HTTPResponse( HTTPResponse.HTTP_OK, HTTPResponse.MIME_RDFXML, "<AServErrorMsg>No "+type+" service launched</AServErrorMsg>" );
	} else {
	    return new HTTPResponse( HTTPResponse.HTTP_OK, HTTPResponse.MIME_HTML, "<html><head>"+HTMLAServProfile.HEADER+"</head><body>"+"<ErrMsg>No "+type+" service launched</ErrMsg>"+"<hr /><center><small><a href=\".\">Alignment server</a></small></center></body></html>" );
	}
    }

    /**
     * Returns the alignment in negociated format
     */
    public HTTPResponse returnAlignment( String uri, String mimeType ) {
	Properties params = new Properties();
	params.setProperty( "id", manager.serverURL()+uri );
	if ( returnType == HTTPResponse.MIME_JSON ) { // YES string compared by ==.
	    params.setProperty( "method", "fr.inrialpes.exmo.align.impl.renderer.JSONRendererVisitor" );
	} else if ( returnType == HTTPResponse.MIME_RDFXML ) {
	    params.setProperty( "method", "fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor" );
	} else {
	    params.setProperty( "method", "fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor" );
	}
	logger.trace( "Bloody URI : {}", manager.serverURL()+uri);
	Message answer = manager.render( params );
	if ( answer instanceof ErrorMsg ) {
	    return new HTTPResponse( HTTPResponse.HTTP_NOTFOUND, HTTPResponse.MIME_PLAINTEXT, "Alignment server: unknown alignment : "+answer.getContent() );
	} else {
	    return new HTTPResponse( HTTPResponse.HTTP_OK, mimeType, answer.getContent() );
	}
    }

    
    /**
     * Returns the network in HTML format
     */
    
    public HTTPResponse returnNetwork( String uri, String mimeType ) {
    	Properties params = new Properties();
    	params.setProperty( "id", manager.serverURL()+uri );
    	if ( returnType == HTTPResponse.MIME_JSON ) { // YES string compared by ==.
    	    params.setProperty( "method", "fr.inrialpes.exmo.align.impl.renderer.JSONRendererVisitor" );
    	} else if ( returnType == HTTPResponse.MIME_RDFXML ) {
    	    params.setProperty( "method", "fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor" );
    	} else {
    	    params.setProperty( "method", "fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor" );
    	}
    	logger.trace( "Bloody Network URI : {}", manager.serverURL()+uri);
    	Message answer = manager.renderHTMLNetwork(params);
    	if ( answer instanceof ErrorMsg ) {
    	    return new HTTPResponse( HTTPResponse.HTTP_NOTFOUND, HTTPResponse.MIME_PLAINTEXT, "Alignment server: unknown network : "+answer.getContent() );
    	} else {
    	    return new HTTPResponse( HTTPResponse.HTTP_OK, mimeType, answer.getContent() );
    	}
        }
    // ===============================================
    // Util

    private int newId() { return localId++; }

    private void decodeParams( String params, Properties p ) throws InterruptedException {
	if ( params == null ) return;
	
	for ( String next : params.split("&") ) {
	    int sep = next.indexOf( '=' );
	    if ( sep >= 0 ){
		try {
		    p.put( URLDecoder.decode( next.substring( 0, sep ), "iso-8859-1" ).trim(),
			   // JE: URLDecoder allows for : and / but not #
			   URLDecoder.decode( next.substring( sep+1 ), "iso-8859-1" ));
		} catch (Exception e) {}; //never thrown
	    }
	}
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

