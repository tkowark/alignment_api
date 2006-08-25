/*
 * $Id: ParserPrinter.java 210 2006-02-17 12:09:31Z euzenat $
 *
 * Copyright (C) 2006 INRIA Rhône-Alpes.
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
$Id: Procalign.java 240 2006-05-19 19:17:08Z euzenat $
</pre>

 * @author Jérôme Euzenat
 */
public class AlignmentService {

    public static void main(String[] args) {
	try { run( args ); }
	catch (Exception ex) { ex.printStackTrace(); };
    }

    public static void run(String[] args) throws Exception {
	String filename = null;
	String outfile = null;
	String paramfile = null;
	int debug = 0;

	// Read parameters

	Parameters params = new BasicParameters();

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
	
	    if ( debug > 0 ) System.err.println("AServ launched");

	if (debug > 0) {
	    params.setParameter("debug", new Integer(debug));
	} else if ( params.getParameter("debug") != null ) {
	    debug = Integer.parseInt((String)params.getParameter("debug"));
	}

	// Connect database

	// For all services, init()

	// Launch HTTP Server (this will have to be changed)
	// To a more generic way by launching all the requested
	// profile or all the available profiles

	// Change port if requested
	// JE: implement as parameter passing to init()
	int port = 8089;
	// Put this in the argument of --html=8980
	//if ( args.length > 0 && lopt != 0 )
	//    port = Integer.parseInt( args[0] );
	HTMLAServProfile htmlS;
	try {
	    htmlS = new HTMLAServProfile();
	    htmlS.init( port );
	    if ( debug > 0 ) System.err.println("AServ launched");
	} catch ( IOException ioe ) {
	    System.err.println( "Couldn't start server:\n" + ioe );
	    System.exit( -1 );
	}
	try { System.in.read(); } catch( Throwable t ) {};

	// I must do something for stoping them
    }

    public static void usage() {
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
