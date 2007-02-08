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

import fr.inrialpes.exmo.align.impl.BasicParameters;

import org.semanticweb.owl.align.Parameters;

import java.util.Hashtable;
import java.util.Enumeration;

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

    public static final String //DBMS Strings
	DBHOST = "localhost",
	DBPORT = "3306",
	DBUSER = "adminAServ",
	DBPASS = "aaa345",
	DBBASE = "AServDB";

    public static final String //Port Strings
	HTML = "8089",
	JADE = "8888",
	WSDL = "7777",
	JXTA = "6666";

    public static final String //IP Strings
	HOST = "localhost";

    private int debug = 0;
    private String filename = null;
    private String outfile = null;
    private String paramfile = null;
    private Hashtable services = null;

    private AServProtocolManager manager;

    public static void main(String[] args) {
	AlignmentService aserv = new AlignmentService();
	try { aserv.run( args ); }
	catch ( Exception ex ) { ex.printStackTrace(); };
    }
    
    public void run(String[] args) throws Exception {
	services = new Hashtable();
	// Read parameters
	Parameters params = readParameters( args );
	if ( debug > 0 ) System.err.println("Parameter parsed");

	// Connect database
	DBService connection = new DBServiceImpl();
	connection.init();
	connection.connect((String)params.getParameter("dbmshost"), 
			   (String)params.getParameter("dbmsport"), 
			   (String)params.getParameter("dbmsuser"), 
			   (String)params.getParameter("dbmspass"), 
			   (String)params.getParameter("dbmsbase") );
	if ( debug > 0 ) System.err.println("Database connected");

	// Create a AServProtocolManager
	manager = new AServProtocolManager();
	manager.init( connection, params );
	if ( debug > 0 ) System.err.println("Manager created");

	// Launch services
	for ( Enumeration e = services.keys() ; e.hasMoreElements() ; ) {
	    String name = (String)e.nextElement();
	    Class serviceClass = Class.forName(name);
	    //Object[] mparams = {(Object)onto1, (Object)onto2 };
	    //Class[] cparams = { oClass, oClass };
	    java.lang.reflect.Constructor constructor = serviceClass.getConstructor( (Class[])null );
	    AlignmentServiceProfile serv = (AlignmentServiceProfile)constructor.newInstance( (Object[])null );
	    try {
		serv.init( params, manager );
		if ( debug > 0 ) System.err.println(name+" launched on http://"+params.getParameter( "host" )+":"+params.getParameter( "http" )+"/html/");
	    } catch ( AServException ex ) {
		System.err.println( "Couldn't start "+name+" server on http://"+params.getParameter( "host" )+":"+params.getParameter( "http" )+"/html/:\n");
		ex.printStackTrace();
	    }
	    services.put( name, serv );
	}

	// Wait loop
	try { System.in.read(); } catch( Throwable t ) {};

	// Close services
	for ( Enumeration e = services.elements() ; e.hasMoreElements() ; ) {
	    AlignmentServiceProfile serv = (AlignmentServiceProfile)e.nextElement();
	    try { serv.close();
	    } catch ( AServException ex ) {
		System.err.println("Cannot close "+serv);
		ex.printStackTrace();
	    }
	}

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
	params.setParameter( "host", HOST );

	// Read parameters

	LongOpt[] longopts = new LongOpt[15];
	// General parameters
	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[2] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[3] = new LongOpt("impl", LongOpt.REQUIRED_ARGUMENT, null, 'l');
	longopts[4] = new LongOpt("D", LongOpt.REQUIRED_ARGUMENT, null, 'D');
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
	longopts[14] = new LongOpt("host", LongOpt.REQUIRED_ARGUMENT, null, 'S');
	longopts[14] = new LongOpt("serv", LongOpt.REQUIRED_ARGUMENT, null, 'i');
	// Is there a way for that in LongOpt ???

	Getopt g = new Getopt("", args, "ho:S:d::l:D:H::A::W::P::m:s:u:p:b:i:", longopts);
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
	    case 'i' :
		/* external service */
		arg = g.getOptarg();
		services.put( arg, arg );
		break;
	    case 'H' :
		/* HTTP Server + port */
		arg = g.getOptarg();
		if ( arg != null ) {
		    params.setParameter( "http", arg );
		} else {
		    params.setParameter( "http", HTML );
		}		    
		services.put( "fr.inrialpes.exmo.align.service.HTMLAServProfile", params.getParameter( "http" ) );
		break;
	    case 'A' :
		/* JADE Server + port */
		arg = g.getOptarg();
		if ( arg != null ) {
		    params.setParameter( "jade", arg );
		} else {
		    params.setParameter( "jade", JADE );
		}		    
		services.put( "fr.inrialpes.exmo.align.service.jade.JadeFIPAAServProfile", params.getParameter( "jade" ) );
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
		break;
	    case 'S' :
		/* Server */
		params.setParameter( "host", g.getOptarg() );
		break;
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

    // Really missing:
    // OUTPUT(o): what for, there is no output (maybe LOGS)
    // LOAD(l): good idea, load from file, but what kind? sql?
    // PARAMS(p is taken, P is taken): yes good as well to read parameters from file
    public void usage() {
	System.err.println("usage: AlignmentService [options]");
	System.err.println("options are:");
	//System.err.println("\t--load=filename -l filename\t\tInitialize the Service with the content of this ");
	//System.err.println("\t--output=filename -o filename\tOutput the alignment in filename");
	System.err.println("\t--html[=port] -H [port]\t\t\tLaunch HTTP service");
	System.err.println("\t--jade[=port] -A [port]\t\t\tLaunch Agent service");
	System.err.println("\t--wsdl[=port] -W [port]\t\t\tLaunch Web service");
	System.err.println("\t--jxta[=port] -P [port]\t\t\tLaunch P2P service");
	System.err.println("\t--serv=class -i class\t\t\tLaunch service corresponding to fully qualified classname");
	//System.err.println("\t--params=filename -p filename\tReads parameters from filename");
	System.err.println("\t--dbmshost=host -m host\t\t\tUse DBMS host");
	System.err.println("\t--dbmsport=port -s port\t\t\tUse DBMS port");
	System.err.println("\t--dbmsuser=name -u name\t\t\tUse DBMS user name");
	System.err.println("\t--dbmspass=pwd -p pwd\t\t\tUse DBMS password");
	System.err.println("\t--dbmsbase=name -b name\t\t\tUse Database name");
	System.err.println("\t--debug[=n] -d [n]\t\tReport debug info at level n");
	System.err.println("\t-Dparam=value\t\t\tSet parameter");
	System.err.println("\t--help -h\t\t\tPrint this message");
	System.err.println("\n$Id$\n");
    }
    
}
