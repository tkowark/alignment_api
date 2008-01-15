/*
 * $Id: AlignmentClient.java 522 2007-07-18 09:08:08Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2007.
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

/** 
 * Example of connection to the Alignment Server through  HTTP/SOAP 
 * Inspired from SOAPClient4XG by Bob DuCharme
 * $Id: Client.java 521 2007-07-18 09:02:04Z euzenat $
 *
*/
package fr.inrialpes.exmo.align.plugin.neontk;

import fr.inrialpes.exmo.align.impl.BasicParameters;

//import fr.inrialpes.exmo.align.util.NullStream;

import org.semanticweb.owl.align.Parameters;
import org.w3c.dom.Document;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.Vector;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xerces.dom.*;
import org.apache.xml.serialize.*;


public class AlignmentClient {


    public  String HTML = null;
    public  String WSDL = "7777";

    public  String HOST = null;

    private int debug = 0;
    private String filename = null;
    private String outfile = null;
    private String paramfile = null;
    private Hashtable services = null;

    public URL SOAPUrl = null;
    public String SOAPAction = null;
    
    public boolean connected = false;

    public AlignmentClient(String htmlPort, String host)  {
    	
    	HTML = htmlPort;
    	HOST = host;
    }
     
    
    public void run(String[] args) throws Exception {
	services = new Hashtable();
	// Read parameters
	Parameters params = readParameters( args );
	if ( outfile != null ) {
	    // This redirects error outout to log file given by -o
	    System.setErr( new PrintStream( outfile ) );
	}
	// JE//: Should not be correct.
	SOAPUrl = new URL( "http://" + HOST + ":" + HTML + "/aserv" );
	if ( debug > 0 ) {
	    System.err.println("***** Parameter parsed");
	    for ( int i=0; i < args.length; i++ ){
		System.err.print( args[i]+" / " );
	    }
	    System.err.println();
	}
	// Create the SOAP message
	String message = createMessage( params );
	if ( debug > 1 ){
	    System.err.print("***** Send to "+SOAPUrl+" :: "+SOAPAction);
	    System.err.println("==>");
	    System.err.println(message);
	    System.err.println();
	}
	// Send message
	String answer = sendMessage( message, params );
	if ( debug > 1 ){
	    System.err.println("***** Received ==>");
	    System.err.println(answer);
	    System.err.println();
	}
	// Displays it
	//displayAnswer( answer );
    }

    public String createMessage( Parameters params ) throws Exception {
        String messageBegin = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\'http://schemas.xmlsoap.org/soap/envelope/\' " +
			                  "xmlns:xsi=\'http://www.w3.org/1999/XMLSchema-instance\' " + 
			                  "xmlns:xsd=\'http://www.w3.org/1999/XMLSchema\'>" +
			                  "<SOAP-ENV:Body>";
	String messageBody = "";
	String cmd = (String)params.getParameter( "command" );
	if ( cmd.equals("list" ) ) {
	    String arg = (String)params.getParameter( "arg1" );
	    if ( arg.equals("methods" ) ){  
		SOAPAction = "listmethodsRequest";
	    } else if ( arg.equals("renderers" ) ){
		SOAPAction = "listrenderersRequest";
	    } else if ( arg.equals("services" ) ){
		SOAPAction = "listservicesRequest";
	    } else if ( arg.equals("alignments" ) ){
		SOAPAction = "listalignmentsRequest";
	    } else {
		usage();
		System.exit(-1);
	    }
	} else if ( cmd.equals("wsdl" ) ) {
	    SOAPAction = "wsdlRequest";
	} else if ( cmd.equals("find" ) ) {
	    SOAPAction = "findRequest";
	    String uri1 = (String)params.getParameter( "arg1" );
	    String uri2 = (String)params.getParameter( "arg2" );
	    if ( uri2 == null ){
		usage();
		System.exit(-1);
	    }
	    messageBody = "<uri1>"+uri1+"</uri1><uri2>"+uri2+"</uri2>";
	} else if ( cmd.equals("match" ) ) {
	    SOAPAction = "matchRequest";
	    String uri1 = (String)params.getParameter( "arg1" );
	    String uri2 = (String)params.getParameter( "arg2" );
	    if ( uri2 == null ){
		usage();
		System.exit(-1);
	    }
	    String method = null;
	    String arg3 = (String)params.getParameter( "arg3" );
	    if ( arg3 != null ) {
		method = uri1; uri1 = uri2; uri2 = arg3;
	    }
	    arg3 = (String)params.getParameter( "arg4" );
	    messageBody = "<url1>"+uri1+"</url1><url2>"+uri2+"</url2>";
	    if ( method != null )
		messageBody += "<method>"+method+"</method>";
	    //fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment
	    if ( arg3 != null )
		messageBody += "<force>"+arg3+"</force>";
	} else if ( cmd.equals("trim" ) ) {
	    SOAPAction = "cutRequest";
	    String id = (String)params.getParameter( "arg1" );
	    String thres = (String)params.getParameter( "arg2" );
	    if ( thres == null ){
		usage();
		System.exit(-1);
	    }
	    String method = null;
	    String arg3 = (String)params.getParameter( "arg3" );
	    if ( arg3 != null ) {
		method = thres; thres = arg3;
	    }
	    messageBody = "<alid>"+id+"</alid><threshold>"+thres+"</threshold>";
	    if ( method != null )
		messageBody += "<method>"+method+"</method>";
	} else if ( cmd.equals("invert" ) ) {
	    SOAPAction = "invertRequest";
	    String uri = (String)params.getParameter( "arg1" );
	    if ( uri == null ){
		usage();
		System.exit(-1);
	    }
	    messageBody = "<alid>"+uri+"</alid>";
	} else if ( cmd.equals("store" ) ) {
	    SOAPAction = "storeRequest";
	    String uri = (String)params.getParameter( "arg1" );
	    if ( uri == null ){
		usage();
		System.exit(-1);
	    }
	    messageBody = "<alid>"+uri+"</alid>";
	} else if ( cmd.equals("load" ) ) {
	    String url = (String)params.getParameter( "arg1" );
	    if ( url == null ){
		SOAPAction = "loadRequest";
		//usage();
		//System.exit(-1);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line;
		String content = "";
		while ((line = in.readLine()) != null) {
		    content += line + "\n";
		}
		if (in != null) in.close();
		System.err.println(content);
		messageBody = "<content>"+content+"</content>";
	    } else {
		SOAPAction = "loadfileRequest";
		messageBody = "<url>"+url+"</url>";
	    }
	    /* This may read the input stream!
			// Most likely Web service request
			int length = request.getContentLength();
			char [] mess = new char[length+1];
			try { 
			    new BufferedReader(new InputStreamReader(request.getInputStream())).read( mess, 0, length);
			} catch (Exception e) {
			    e.printStackTrace(); // To clean up
			}
			params.setProperty( "content", new String( mess ) );
	    */
	} else if ( cmd.equals("retrieve" ) ) {
	    SOAPAction = "retrieveRequest";
	    String uri = (String)params.getParameter( "arg1" );
	    String method = (String)params.getParameter( "arg2" );
	    if ( method == null ){
		usage();
		System.exit(-1);
	    }
	    messageBody = "<alid>"+uri+"</alid><method>"+method+"</method>";
	} else if ( cmd.equals("metadata" ) ) {
	    SOAPAction = "metadata";
	    String uri = (String)params.getParameter( "arg1" );
	    String key = (String)params.getParameter( "arg2" );
	    if ( key == null ){
		usage();
		System.exit(-1);
	    }
	    messageBody = "<alid>"+uri+"</alid><key>"+key+"</key>";
	} else {
	    usage();
	    System.exit(-1);
	}
		// Create input message and URL
	String messageEnd = "</SOAP-ENV:Body>"+"</SOAP-ENV:Envelope>";
	String message = messageBegin + messageBody + messageEnd;
	return message;
    }
        
    public String sendMessage( String message, Parameters param ) throws Exception {
	// Create the connection
    	 
        URLConnection connection = SOAPUrl.openConnection();
        if(connection==null) {
        	connected = false;
        }
        else {
        	connected = true;
        	System.out.println("connected to Server !");
        }
        HttpURLConnection httpConn = (HttpURLConnection) connection;

        byte[] b = message.getBytes();

	// Create HTTP Request
        httpConn.setRequestProperty( "Content-Length",
                                     String.valueOf( b.length ) );
        httpConn.setRequestProperty("Content-Type","text/xml; charset=utf-8");
        httpConn.setRequestProperty("SOAPAction",SOAPAction);
        httpConn.setRequestMethod( "POST" );
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);

        // Send the request through the connection
        OutputStream out = httpConn.getOutputStream();
        out.write( b );    
        out.close();

        // Read the response and write it to standard output
        InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
        BufferedReader in = new BufferedReader(isr);
	String answer = "";
	String line;
	while ((line = in.readLine()) != null) {
	    answer += line + "\n";
	}
	if (in != null) in.close();

	return answer;
    }
    
    
    
    public Parameters readParameters( String[] args ) {
    	
    try { 
        	SOAPUrl = new URL( "http://" + HOST + ":" + HTML + "/aserv" );}
    catch ( Exception ex ) { ex.printStackTrace(); };
         
        	
	Parameters params = new BasicParameters();
	services = new Hashtable();
	params.setParameter( "host", HOST );

	
	// Read parameters

	LongOpt[] longopts = new LongOpt[16];
	// General parameters
	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[2] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[3] = new LongOpt("impl", LongOpt.REQUIRED_ARGUMENT, null, 'l');
	longopts[4] = new LongOpt("D", LongOpt.REQUIRED_ARGUMENT, null, 'D');
	// Service parameters
	longopts[5] = new LongOpt("html", LongOpt.OPTIONAL_ARGUMENT, null, 'H');
	longopts[6] = new LongOpt("jade", LongOpt.OPTIONAL_ARGUMENT, null, 'A');
	longopts[7] = new LongOpt("wsdl", LongOpt.OPTIONAL_ARGUMENT, null, 'W');
	longopts[8] = new LongOpt("jxta", LongOpt.OPTIONAL_ARGUMENT, null, 'P');
	// DBMS Server parameters
	longopts[9] = new LongOpt("dbmshost", LongOpt.REQUIRED_ARGUMENT, null, 'm');
	longopts[10] = new LongOpt("dbmsport", LongOpt.REQUIRED_ARGUMENT, null, 's');
	longopts[11] = new LongOpt("dbmsuser", LongOpt.REQUIRED_ARGUMENT, null, 'u');
	longopts[12] = new LongOpt("dbmspass", LongOpt.REQUIRED_ARGUMENT, null, 'p');
	longopts[13] = new LongOpt("dbmsbase", LongOpt.REQUIRED_ARGUMENT, null, 'b');
	longopts[14] = new LongOpt("host", LongOpt.REQUIRED_ARGUMENT, null, 'S');
	longopts[15] = new LongOpt("serv", LongOpt.REQUIRED_ARGUMENT, null, 'i');
	// Is there a way for that in LongOpt ???

	Getopt g = new Getopt("", args, "ho:S:l:d::D:H::A::W::P::m:s:u:p:b:i:", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch (c) {
	    case 'h' :
		usage();
		System.exit(0);
	    case 'o' :
		/* Use filename instead of stdout */
		outfile = g.getOptarg();
		break;
	    case 'd' :
		/* Debug level  */
		arg = g.getOptarg();
		if ( arg != null ) debug = Integer.parseInt(arg.trim());
		else debug = 4;
		break;
	    case 'H' :
		/* HTTP Server + port */
		arg = g.getOptarg();
		if ( arg != null ) {
		    params.setParameter( "http", arg );
		} else {
		    params.setParameter( "http", HTML );
		}
		// This shows that it does not work
		services.put( "fr.inrialpes.exmo.align.service.HTMLAServProfile", params.getParameter( "http" ) );
		break;
	    case 'W' :
		/* Web service + port */
		arg = g.getOptarg();
		if ( arg != null ) {
		    params.setParameter( "wsdl", arg );
		} else {
		    params.setParameter( "wsdl", WSDL );
		}		    
		break;
	    case 'S' :
		/* Server */
		params.setParameter( "host", g.getOptarg() );
		break;
	    case 'D' :
		/* Parameter definition */
		arg = g.getOptarg();
		int index = arg.indexOf('=');
		if ( index != -1 ) {
		    params.setParameter( arg.substring( 0, index), 
					 arg.substring(index+1));
		} else {
		    System.err.println("Bad parameter syntax: "+g);
		    usage();
		    System.exit(0);
		    
		}
		break;
	    }
	}
	
	if (debug > 0) {
	    params.setParameter("debug", new Integer(debug));
	} else if ( params.getParameter("debug") != null ) {
	    debug = Integer.parseInt((String)params.getParameter("debug"));
	}

	// Store the remaining arguments in param
	int i = g.getOptind();
	if ( args.length < i + 1 ){
	    usage();
	   
	    System.exit(-1);
	} else {
	    params.setParameter("command", args[i++]);
	    for ( int k = 1; i < args.length; i++,k++ ){
		params.setParameter("arg"+k, args[i]);
	    }
	}
	return params;
    }
    
    private Document parse(String uri) throws Exception {
        org.w3c.dom.Document doc = null;
        try {
    	javax.xml.parsers.DocumentBuilderFactory dbf =
    	    javax.xml.parsers.DocumentBuilderFactory.newInstance();
    	dbf.setValidating(false);
    	javax.xml.parsers.DocumentBuilder builder = dbf.newDocumentBuilder();
    	doc = builder.parse(uri);
        } catch (Exception e) {
    	//compilerError(Compiler.XML, e.getMessage());
        }
        return doc;
    }

    private Document parse(File f) throws Exception {
      try {
        String uri = f.toURL().toString();
        return parse(uri);
      } catch (MalformedURLException ex) {
        //compilerError(Compiler.FILE, ex.getMessage());
      }
      return null;
    }

    
    public Document parseString(String s) throws Exception {
	    org.w3c.dom.Document doc = null;
	    try {
	    	
	        javax.xml.parsers.DocumentBuilderFactory dbf =
	            javax.xml.parsers.DocumentBuilderFactory.newInstance();
	        dbf.setValidating(false);
	        javax.xml.parsers.DocumentBuilder builder = dbf.newDocumentBuilder();
	        System.out.println( "parsing 0... ");
	        doc = builder.parse(s);
	        System.out.println( "parsing 1... ");
	    } catch (Exception e) {
	    	 System.out.println( "problem="+ e.getMessage());
	        //compilerError(Compiler.XML, e.getMessage());
	    }
	    System.out.println( "return from parsing ");
	    return doc;
	} 

     
     
     
    // Really missing:
    // OUTPUT(o): what for, there is no output (maybe LOGS)
    // LOAD(l): good idea, load from file, but what kind? sql?
    // PARAMS(p is taken, P is taken): yes good as well to read parameters from file
    public void usage() {
	System.err.println("usage: AlignmentClient [options] command [args]");
	System.err.println("options are:");
	System.err.println("\t--html[=port] -H[port]\t\t\tLaunch HTTP service");
	System.err.println("\t--wsdl[=port] -W[port]\t\t\tLaunch Web service");
	System.err.println("\t--output=filename -o filename\tRedirect output to filename");
	System.err.println("\t--debug[=n] -d[n]\t\tReport debug info at level n");
	System.err.println("\t-Dparam=value\t\t\tSet parameter");
	System.err.println("\t--help -h\t\t\tPrint this message");
	System.err.println();
	System.err.println("commandss are:");
	System.err.println("\twsdl");
	System.err.println("\tfind URI URI");
	System.err.println("\tmatch URI URI");
	System.err.println("\ttrim AURI [method] threshold");
	System.err.println("\tinvert AURI");
	System.err.println("\tload URI | File");
	System.err.println("\tstore AURI");
	System.err.println("\tretrieve AURI");
	//	System.err.println("\tmetadata AURI key");
	System.err.println("\tlist alignments");
	System.err.println("\tlist method");
	System.err.println("\tlist renderers");
	System.err.println("\tlist services");
	System.err.println("\n$Id$\n");
    }    
}

