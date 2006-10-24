/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2006
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

import fr.inrialpes.exmo.queryprocessor.QueryProcessor;
import fr.inrialpes.exmo.queryprocessor.Result;
import fr.inrialpes.exmo.queryprocessor.Type;

import fr.inrialpes.exmo.align.service.jade.JadeFIPAAServProfile;

import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.impl.BasicParameters;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Parameters;

import java.io.IOException;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

/**
 * AlignmentService
 * 
 * The basic alignment service able to run a store and answer queries...
 * 
    <pre>
    java -jar alignsvc.jar [options]
    </pre>

    where the options are:
    <pre>
        --load=filename -l filename     Load previous image
	--params=filename -p filename   Read the parameters in file
        --debug[=n] -d [n]              Report debug info at level n,
        --help -h                       Print this message
    </pre>

<pre>
$Id$
</pre>

 * @author Jérôme Euzenat
 */
public class AlignmentService {

    public static final String
	DBHOST = "localhost",
	DBPORT = "3306",
	DBUSER = "adminAServ",
	DBPASS = "aaa345",
	DBBASE = "AServDB";

    public static final String
	HTML = "8089",
	JADE = "8888",
	WSDL = "7777",
	JXTA = "6666";

    private int debug = 0;
    private String filename = null;
    private String outfile = null;
    private String paramfile = null;

    private AServProtocolManager manager;

    public static void main(String[] args) {
	AlignmentService aserv = new AlignmentService();
	try { aserv.run( args ); }
	catch ( Exception ex ) { ex.printStackTrace(); };
    }
    
    public void run(String[] args) throws Exception {
	// Read parameters
	Parameters params = readParameters( args );
	if ( debug > 0 ) System.err.println("Parameter parsed");

	// Connect database
	DBService connection = new DBServiceImpl();
	connection.init();
	connection.connect((String)params.getParameter( "dbmshost"), 
			   (String)params.getParameter( "dbmsport"), 
			   (String)params.getParameter( "dbmsuser"), 
			   (String)params.getParameter( "dbmspass"), 
			   (String)params.getParameter( "dbmsbase") );
	if ( debug > 0 ) System.err.println("Database connected");

	// Create a AServProtocolManager
	manager = new AServProtocolManager();
	manager.init( connection, params );
	if ( debug > 0 ) System.err.println("Manager created");

	// This will have to be changed to a more generic way by
	// launching all the requested profile or all the available profiles
	// For all services:
	// Get a list of services
	// Create them
	// init( params ) -- parameters must be passed

	// Launch HTTP Server
	if ( params.getParameter( "http" ) != null ){
	    HTMLAServProfile htmlS;
	    // May be put some Runable here...
	    try {
		htmlS = new HTMLAServProfile();
		htmlS.init( params, manager );
		if ( debug > 0 ) System.err.println("HTTP AServ launched on http://localhost:"+params.getParameter("http"));
	    } catch ( AServException e ) {
		System.err.println( "Couldn't start HTTP server:\n");
		e.printStackTrace();
	    }
	}

	if ( params.getParameter( "jade" ) != null ){
	    JadeFIPAAServProfile JADEServeur = new JadeFIPAAServProfile();
	    try{ 
		JADEServeur.init( params, manager );
		if ( debug > 0 ) System.err.println("JADE AServ launched on http://localhost:"+params.getParameter("jadeport")); }
	    catch ( AServException e ) {
		System.err.println( "Couldn't start JADE server:\n");
		e.printStackTrace();
	    }
	}

	// Wait loop
	try { System.in.read(); } catch( Throwable t ) {};

	// I must do something for stoping them
	// For all list of services
	// close()

	// Shut down database connection
	connection.close();
	if ( debug > 0 ) System.err.println("Database connection closed");
    }

    public Parameters readParameters( String[] args ) {
	Parameters params = new BasicParameters();

	// Default database parameters
	params.setParameter( "dbmshost", DBHOST );
	params.setParameter( "dbmsport", DBPORT );
	params.setParameter( "dbmsuser", DBUSER );
	params.setParameter( "dbmspass", DBPASS );
	params.setParameter( "dbmsbase", DBBASE);

	// Read parameters

	LongOpt[] longopts = new LongOpt[14];
	// General parameters
	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[2] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[3] = new LongOpt("impl", LongOpt.REQUIRED_ARGUMENT, null, 'l');
	//longopts[4] = new LongOpt("params", LongOpt.REQUIRED_ARGUMENT, null, 'p');
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
	// Is there a way for that in LongOpt ???
	longopts[5] = new LongOpt("D", LongOpt.REQUIRED_ARGUMENT, null, 'D');

	Getopt g = new Getopt("", args, "ho:d::l:D:H::A::W::P::m:s:u:p:b:", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch (c) {
	    case 'h' :
		usage();
	    case 'o' :
		/* Use filename instead of stdout */
		outfile = g.getOptarg();
		break;
		//case 'p' :
		/* Read parameters from filename */
		//paramfile = g.getOptarg();
		//BasicParameters.read( params, paramfile);
		//break;
	    case 'l' :
		/* Use the given file as a database image to load */
		filename = g.getOptarg();
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
		break;
	    case 'A' :
		/* JADE Server + port */
		arg = g.getOptarg();
		if ( arg != null ) {
		    params.setParameter( "jade", arg );
		} else {
		    params.setParameter( "jade", JADE );
		}		    
		break;
	    case 'W' :
		/* WSDL Server + port */
		arg = g.getOptarg();
		if ( arg != null ) {
		    params.setParameter( "wsdl", arg );
		} else {
		    params.setParameter( "wsdl", WSDL );
		}		    
		break;
	    case 'P' :
		/* JXTA Server + port */
		arg = g.getOptarg();
		if ( arg != null ) {
		    params.setParameter( "jxta", arg );
		} else {
		    params.setParameter( "jxta", JXTA );
		}		    
	    case 'm' :
		params.setParameter( "dbmshost", g.getOptarg() );
		break;
	    case 's' :
		params.setParameter( "dbmsport", g.getOptarg() );
		break;
	    case 'u' :
		params.setParameter( "dbmsuser", g.getOptarg() );
		break;
	    case 'p' :
		params.setParameter( "dbmspass", g.getOptarg() );
		break;
	    case 'b' :
		params.setParameter( "dbmsbase", g.getOptarg() );
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

	return params;
    }


    public void usage() {
	System.err.println("usage: AlignmentService [options]");
	System.err.println("$Id$\n");
	System.err.println("options are:");
	System.err.println("\t--load=filename -l filename\t\tInitialize the Service with the content of this file.");
	System.err.println("\t--output=filename -o filename\tOutput the alignment in filename");
	System.err.println("\t--params=filename -p filename\tReads parameters from filename");
	System.err.println("\t--debug[=n] -d [n]\t\tReport debug info at level n");
	System.err.println("\t-Dparam=value\t\t\tSet parameter");
	System.err.println("\t--help -h\t\t\tPrint this message");
    }
    
}
