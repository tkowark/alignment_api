/*
 * $Id$
 *
 * Copyright (C) INRIA Rh?e-Alpes, 2007.
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
import java.io.IOException;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;

import java.util.StringTokenizer;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.URLEncoder;
import java.net.URLDecoder;

import java.lang.Integer;

/**
 * HTMLAServProfile: an HTML provile for the Alignment server
 * It embeds an HTTP server.
 */

public class WSAServProfile implements AlignmentServiceProfile {

    private int tcpPort;
    private String tcpHost;
    private int debug = 0;
    private AServProtocolManager manager;

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
	MIME_DEFAULT_BINARY = "application/octet-stream";

    public static final int MAX_FILE_SIZE = 10000;

    // ==================================================
    // Socket & server code
    // ==================================================

    public void init( Parameters params, AServProtocolManager manager ) throws AServException {
	this.manager = manager;
	// This may register the WSDL file to some directory
	myId = "LocalHTMLInterface";
	serverId = "dummy";
	localId = 0;	
	}

    public void close(){
	// This may unregister the WSDL file to some directory
    }
    
    // ==================================================
    // API parts
    // ==================================================

    /**
     * HTTP protocol implementation
     * each call of the protocol is a direct URL
     * and the answer is through the resulting page (RDF? SOAP? HTTP?)
     * Not implemented yet
     * but reserved if appears useful
     */
    public String protocolAnswer( String uri, String perf, Properties header, Parameters param ) {
	String method;
	String message;
//	System.err.println("SOAP MESSAGE [ "+perf+" ]\n"+params.getParameter("content"));
	method = header.getProperty("SOAPAction");
	message = (String) param.getParameter("content");

	String msg = "<SOAP-ENV:Envelope   xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'   xmlns:xsi='http://www.w3.org/1999/XMLSchema-instance'   xmlns:xsd='http://www.w3.org/1999/XMLSchema'>  <SOAP-ENV:Body>";
	if ( perf.equals("WSDL") ) {
	} else if ( method.equals("listalignment") ) {
		for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension("id");
		msg += "<uri>"+id+"</uri>";
	    }
		// -> List of URI
	} else if ( method.equals("listmethods") ) { // -> List of String
	    for( Iterator it = manager.listmethods().iterator(); it.hasNext(); ) {
		msg += "<method>"+it.next()+"</method>";
	    }
	} else if ( method.equals("listrenderers") ) { // -> List of String
	    for( Iterator it = manager.listrenderers().iterator(); it.hasNext(); ) {
		msg += "<renderer>"+it.next()+"</renderer>";
	    }
	} else if ( method.equals("listservices") ) { // -> List of String
	    for( Iterator it = manager.listservices().iterator(); it.hasNext(); ) {
		msg += "<service>"+it.next()+"</service>";
	    }
	} else if ( method.equals("store") ) { // URI -> URI
		int start;
		int end;
		String request_id;
		String request_uri;
		Parameters params;

		params = new BasicParameters();

		start = message.indexOf("<id>");
		end = message.indexOf("</id>");
		request_id = message.substring(start+4, end);
		start = message.indexOf("<uri>");
		end = message.indexOf("</uri>");
		request_uri = message.substring(start+5, end);

		params.setParameter("id", request_id);
		params.setParameter("uri", request_uri);

	    if ( request_uri != null && !request_uri.equals("") ) { // Load the URL
		Message answer = manager.load( new Message(newId(),(Message)null,myId,serverId,"", params) );
		if ( answer instanceof ErrorMsg ) {
		    msg = testErrorMessagesSOAP( answer );
		} else {
		    request_id = answer.getContent();
		}
	    }
	    if ( request_id != null ){ // Store it
		Message answer = manager.store( new Message(newId(),(Message)null,myId,serverId, request_id, params) );
		
		if ( answer instanceof ErrorMsg ) {
		    msg += testErrorMessagesSOAP( answer );
		} else {
			msg += displayAnswerSOAP( answer );
		}
	    }
	} else if ( method.equals("cut") ) { // URI * string * float -> URI
		int start;
		int end;
		String request_id;
		String request_uri;
		String request_threshold;
		Parameters params;

		params = new BasicParameters();

		start = message.indexOf("<id>");
		end = message.indexOf("</id>");
		request_id = message.substring(start+4, end);
		start = message.indexOf("<uri>");
		end = message.indexOf("</uri>");
		request_uri = message.substring(start+5, end);
		start = message.indexOf("<threshold>");
		end = message.indexOf("</threshold>");
		request_threshold = message.substring(start+10, end);
		
		params.setParameter("id", request_id);
		params.setParameter("uri", request_uri);
		params.setParameter("threshold", request_threshold);

	    if ( request_id != null && !request_id.equals("") && request_threshold != null && !request_threshold.equals("") ){ // Trim it
		Message answer = manager.cut( new Message(newId(),(Message)null,myId,serverId, request_id, params) );
		if ( answer instanceof ErrorMsg ) {
		    msg = testErrorMessagesSOAP( answer );
		} else {
		    msg += displayAnswerSOAP( answer );
		}
	    }
	} else if ( method.equals("align") ) { // URL * URL * URI * String * boolean * (params) -> URI
		int start;
		int end;

		String request_url1;
		String request_url2;
		String request_method;
		String request_force;
		Parameters params;

		params = new BasicParameters();

		start = message.indexOf("<url1>");
		end = message.indexOf("</url1>");
		request_url1 = message.substring(start+6, end);

		start = message.indexOf("<url2>");
		end = message.indexOf("</url2>");
		request_url2 = message.substring(start+6, end);

		start = message.indexOf("<method>");
		end = message.indexOf("</method>");
		request_method = message.substring(start+5, end);

		start = message.indexOf("<force>");
		end = message.indexOf("</force>");
		request_force = message.substring(start+7, end);
		
		params.setParameter("onto1", request_url1);
		params.setParameter("onto2", request_url2);
		params.setParameter("method", request_method);
		params.setParameter("force", request_force);

		Message answer = manager.align( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessagesSOAP( answer );
	    } else {
		msg += displayAnswerSOAP( answer );
	    }
	} else if ( method.equals("find") ) { // URI * URI -> List of URI
		int start;
		int end;

		String request_url1;
		String request_url2;
		Parameters params;

		params = new BasicParameters();

		start = message.indexOf("<url1>");
		end = message.indexOf("</url1>");
		request_url1 = message.substring(start+6, end);

		start = message.indexOf("<url2>");
		end = message.indexOf("</url2>");
		request_url2 = message.substring(start+6, end);

		params.setParameter("onto1", request_url1);
		params.setParameter("onto2", request_url2);

	    
		Message answer = manager.existingAlignments( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessagesSOAP( answer );
	    } else {
		msg += displayAnswerSOAP( answer );
	    }
	} else if ( method.equals("retrieve") ) { // URI -> XML
		int start;
		int end;
		String request_id;
		Parameters params;

		params = new BasicParameters();

		start = message.indexOf("<id>");
		end = message.indexOf("</id>");
		request_id = message.substring(start+4, end);
	
		params.setParameter("id", request_id);
	
		Message answer = manager.render( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg += testErrorMessagesSOAP( answer );
	    } else {
		// Depending on the type we should change the MIME type
		// This should be returned in answer.getParameters()
		msg += "<result>" + answer.getContent() + "</result>";
		}
	} else if ( method.equals("metadata") ) { // URI -> XML
		int start;
		int end;
		String request_id;
		Parameters params;

		params = new BasicParameters();

		start = message.indexOf("<id>");
		end = message.indexOf("</id>");
		request_id = message.substring(start+4, end);
	
		params.setParameter("id", request_id);
	
		Message answer = manager.render( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg += testErrorMessagesSOAP( answer );
	    } else {
		msg += "<result>" + answer.getContent() + "</result>";
		}
	} else if ( method.equals("load") ) { // URL -> URI
		int start;
		int end;
		String request_url;
		Parameters params;

		params = new BasicParameters();

		start = message.indexOf("<url>");
		end = message.indexOf("</url>");
		request_url = message.substring(start+5, end);

		params.setParameter("url", request_url);

		Message answer = manager.load( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessagesSOAP( answer );
	    } else {
		msg += displayAnswerSOAP( answer );
	    }
	} else if ( method.equals("loadfile") ) { // XML -> URI
	    Message answer = manager.load( new Message(newId(),(Message)null,myId,serverId,"", param) );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessagesSOAP( answer );
	    } else {
		msg += displayAnswerSOAP( answer );
	    }
	} else if ( method.equals("translate") ) { // XML * URI -> XML
	} else {
	    msg += "<UnRecognizedAction />";
	}
	msg += "  </SOAP-ENV:Body></SOAP-ENV:Envelope>";
	return msg;
    }
	private String displayAnswerSOAP ( Message answer ) {
	return answer.SOAPString();
	}
	private String testErrorMessagesSOAP( Message answer ) {
	return answer.SOAPString();
    }
    private int newId() { return localId++; }
}
