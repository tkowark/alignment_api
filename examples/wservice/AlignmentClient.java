/*
 * $Id: AlignmentClient.java 522 2007-07-18 09:08:08Z euzenat $
 *
 * Copyright (C) INRIA Rhône-Alpes, 2007-2008
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

import fr.inrialpes.exmo.align.impl.BasicParameters;

import fr.inrialpes.exmo.align.util.NullStream;

import org.semanticweb.owl.align.Parameters;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.PrintStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

public class AlignmentClient {

    public static final String //Port Strings
	HTML = "8089",
	WSDL = "7777";

    public static final String //IP Strings
//	HOST = "localhost";
	HOST = "aserv.inrialpes.fr";

    private int debug = 0;
    private String filename = null;
    private String outfile = null;
    private String paramfile = null;
    private Hashtable services = null;

    private URL SOAPUrl = null;
    private String SOAPAction = null;

    public static void main(String[] args) {
	try { new AlignmentClient().run( args ); }
	catch ( Exception ex ) { ex.printStackTrace(); };
    }
    
    public void run(String[] args) throws Exception {
	services = new Hashtable();
	SOAPUrl = new URL( "http://" + HOST + ":" + HTML + "/aserv" );
	// Read parameters
	Parameters params = readParameters( args );
	if ( outfile != null ) {
	    // This redirects error outout to log file given by -o
	    System.setErr( new PrintStream( outfile ) );
	}
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
	displayAnswer( answer );
    }

    public String createMessage( Parameters params ) throws Exception {
        String messageBegin = "<SOAP-ENV:Envelope xmlns='http://exmo.inrialpes.fr/align/service'\n                   xml:base='http://exmo.inrialpes.fr/align/service'\n                   xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'\n" +
			                  "                   xmlns:xsi=\'http://www.w3.org/1999/XMLSchema-instance'\n" + 
			                  "                   xmlns:xsd=\'http://www.w3.org/1999/XMLSchema\'>\n" +
			                  "  <SOAP-ENV:Body>\n";
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
	    messageBody = "    <uri1>"+uri1+"</uri1>\n    <uri2>"+uri2+"</uri2>\n";
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
	    messageBody = "    <url1>"+uri1+"</url1>\n    <url2>"+uri2+"</url2>\n";
	    if ( method != null )
		messageBody += "    <method>"+method+"</method>\n";
	    //fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment
	    if ( arg3 != null )
		messageBody += "    <force>"+arg3+"</force>\n";
	} else if ( cmd.equals("align" ) ) {
	    SOAPAction = "align";
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
	    //arg3 = (String)params.getParameter( "arg4" );
	    messageBody = "    <url1>"+uri1+"</url1>\n    <url2>"+uri2+"</url2>\n";
	    if ( method != null )
		messageBody += "    <method>"+method+"</method>\n";
	    //fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment
	    //if ( arg3 != null )
	    //	messageBody += "<force>"+arg3+"</force>";
	}

	  else if ( cmd.equals("trim" ) ) {
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
	    messageBody = "    <alid>"+id+"</alid>\n    <threshold>"+thres+"</threshold>\n";
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
		messageBody = "    <content>"+content+"</content>\n";
	    } else {
		SOAPAction = "loadfileRequest";
		messageBody = "    <url>"+url+"</url>\n";
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
	    messageBody = "    <alid>"+uri+"</alid>\n    <method>"+method+"</method>\n";
	} else if ( cmd.equals("metadata" ) ) {
	    SOAPAction = "metadata";
	    String uri = (String)params.getParameter( "arg1" );
	    String key = (String)params.getParameter( "arg2" );
	    if ( key == null ){
		usage();
		System.exit(-1);
	    }
	    messageBody = "    <alid>"+uri+"</alid>\n    <key>"+key+"</key>\n";
	} else {
	    usage();
	    System.exit(-1);
	}
	// Create input message and URL
	String messageEnd = "  </SOAP-ENV:Body>\n"+"</SOAP-ENV:Envelope>\n";
	String message = messageBegin + messageBody + messageEnd;
	return message;
    }
        
    public String sendMessage( String message, Parameters param ) throws Exception {
	// Create the connection
        URLConnection connection = SOAPUrl.openConnection();
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
    public Parameters readParameters( String[] args ) throws java.net.MalformedURLException {
	Parameters params = new BasicParameters();

	params.setParameter( "host", HOST );

	// Read parameters

	LongOpt[] longopts = new LongOpt[8];
	// General parameters
	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[2] = new LongOpt("D", LongOpt.REQUIRED_ARGUMENT, null, 'D');
	// Service parameters
	longopts[3] = new LongOpt("server", LongOpt.REQUIRED_ARGUMENT, null, 'S');

	Getopt g = new Getopt("", args, "hD:d::S:", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch (c) {
	    case 'h' :
		usage();
		System.exit(0);
	    case 'd' :
		/* Debug level  */
		arg = g.getOptarg();
		if ( arg != null ) debug = Integer.parseInt(arg.trim());
		else debug = 4;
		break;
	    case 'S' :
		/* HTTP Server + port */
		arg = g.getOptarg();
		SOAPUrl = new URL( arg );
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

    public void displayAnswer( String answer ) {
	// Printout to be improved...
	System.out.println( answer );
    }

    public void usage() {
	System.err.println("usage: AlignmentClient [options] command [args]");
	System.err.println("options are:");
	System.err.println("\t--server=URL -S URL\tthe server to which to connect");
	System.err.println("\t--debug[=n] -d[n]\t\tReport debug info at level n");
	System.err.println("\t-Dparam=value\t\t\tSet parameter");
	System.err.println("\t--help -h\t\t\tPrint this message");
	System.err.println();
	System.err.println("commands are:");
	System.err.println("\twsdl");
	System.err.println("\tfind URI URI");
	System.err.println("\tmatch URI URI");
	System.err.println("\talign URI URI (this is for WSAlignment)");
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

