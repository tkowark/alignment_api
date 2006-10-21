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

    private String DbName = null;
    private String DbPassword = null;

    private AServProtocolManager manager;

    public static void main(String[] args) {
	AlignmentService aserv = new AlignmentService();
	try { aserv.run( args ); }
	catch ( Exception ex ) { ex.printStackTrace(); };
    }
    
    public void run(String[] args) throws Exception {
	String filename = null;
	String outfile = null;
	String paramfile = null;
	int debug = 0;

	// Default parameters

	Parameters params = new BasicParameters();
	// Put this in the argument of --html=8089
	params.setParameter( "httpport", "8089" );
	// --jadeport=
	params.setParameter( "jadeport", "8888" );
	// --dbms= --name= --password=
	params.setParameter( "dbmsserver", "" );
	params.setParameter( "dbmsname", "root" );
	params.setParameter( "dbmspass", "1234" );

	// Read parameters

	LongOpt[] longopts = new LongOpt[10];

	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[2] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[3] = new LongOpt("impl", LongOpt.REQUIRED_ARGUMENT, null, 'l');
	longopts[4] = new LongOpt("params", LongOpt.REQUIRED_ARGUMENT, null, 'p');
	// Is there a way for that in LongOpt ???
	longopts[5] = new LongOpt("D", LongOpt.REQUIRED_ARGUMENT, null, 'D');

	Getopt g = new Getopt("", args, "ho:p:d::l:D:", longopts);
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
	    case 'p' :
		/* Read parameters from filename */
		paramfile = g.getOptarg();
		BasicParameters.read( params, paramfile);
		break;
	    case 'l' :
		/* Use the given class for rendering */
		filename = g.getOptarg();
		break;
	    case 'd' :
		/* Debug level  */
		arg = g.getOptarg();
		if ( arg != null ) debug = Integer.parseInt(arg.trim());
		else debug = 4;
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

	// Connect database
	DBService connection = new DBServiceImpl();
	connection.init();
	connection.connect( DbName, DbPassword );

	// Create a AServProtocolManager
	manager = new AServProtocolManager();
	manager.init( connection, params );

	// Launch HTTP Server (this will have to be changed)
	// To a more generic way by launching all the requested
	// profile or all the available profiles
	HTMLAServProfile htmlS;
	// May be put some Runable here...
	try {
	    htmlS = new HTMLAServProfile();
	    htmlS.init( params, manager );
	} catch ( AServException e ) {
	    System.err.println( "Couldn't start server:\n" + e );
	    System.exit( -1 );
	}
	if ( debug > 0 ) System.err.println("AServ launched on http://localhost:"+params.getParameter("httpport"));

	// For all services:
	// Get a list of services
	// Create them
	// init( params ) -- parameters must be passed

	// This will have to be changed to a more generic way by
	// launching all the requested profile or all the available profiles
	// TODO Auto-generated method stub
	JadeFIPAAServProfile JADEServeur = new JadeFIPAAServProfile();
	try{ JADEServeur.init( params, manager ); }
	catch ( AServException e ) {
	    System.err.println( "Couldn't start server:\n" + e );
	    System.exit( -1 );
	    // not good
	}
	//catch (Exception e) {e.printStackTrace();}

	if ( debug > 0 ) System.err.println("AServ launched on http://localhost:"+params.getParameter("jadeport"));

	try { System.in.read(); } catch( Throwable t ) {};

	// I must do something for stoping them
	// For all list of services
	// close()

	// Shut down database connection
	connection.close();
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
