/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2007.
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
    public String protocolAnswer( String uri, String perf, Properties header, Parameters params ) {
	System.err.println("SOAP MESSAGE [ "+perf+" ]\n"+params.getParameter("content"));
	String msg = "";
	if ( perf.equals("WSDL") ) {
	} else if ( perf.equals("listalignment") ) {
	} else if ( perf.equals("listmethods") ) {
	} else if ( perf.equals("listrenderers") ) {
	} else if ( perf.equals("listservices") ) {
	} else if ( perf.equals("store") ) {
	} else if ( perf.equals("cut") ) {
	} else if ( perf.equals("align") ) {
	} else if ( perf.equals("find") ) {
	} else if ( perf.equals("retrieve") ) {
	} else if ( perf.equals("metadata") ) {
	} else if ( perf.equals("load") ) {
	} else if ( perf.equals("loadfile") ) {
	} else if ( perf.equals("translate") ) {
	} else {
	}
	return msg;
    }

}
