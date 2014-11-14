/*
 * $Id$
 *
 * Copyright (C) 2013-2014 INRIA
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

/*
 * Common command line parameter parsing based on Apache commons cli wrt 
 */

package fr.inrialpes.exmo.align.cli;

import java.util.Properties;
import java.util.Map.Entry;
import java.io.FileInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

/**
 * For instanciating this class:
 * - extends CommonCLI
 * - call to super() in constructor
 * - add options in constructor
 * - CommandLine line = parseCommandLine( args ); in main
 * - retrieve new options
 * - retrieve remaining args
 * - redefine usage() with specific first line
 * - use parameters
 */

public abstract class CommonCLI {
    final static Logger logger = LoggerFactory.getLogger( CommonCLI.class );

    protected Options options = null;

    protected String outputfilename = null;

    protected Properties parameters = null;

    public CommonCLI() {
	parameters = new Properties();
	options = new Options();
	options.addOption( "h", "help", false, "Print this page" );
	options.addOption( OptionBuilder.withLongOpt( "output" ).hasArg().withDescription( "Send output to FILE" ).withArgName("FILE").create( 'o' ) );
	options.addOption( OptionBuilder.withLongOpt( "debug" ).hasOptionalArg().withDescription( "debug argument is deprecated, use logging instead\nSee http://alignapi.gforge.inria.fr/logging.html" ).withArgName("LEVEL").create( 'd' ) );
	options.addOption( OptionBuilder.withLongOpt( "params" ).hasArg().withDescription( "Read parameters from FILE" ).withArgName("FILE").create( 'P' ) );
	options.addOption( OptionBuilder.withArgName( "NAME=VALUE" ).hasArgs(2).withValueSeparator().withDescription( "Use value for given property" ).create( 'D' ) );
    }

    // This is an example of using the interface
    private void run( String[] args ) {
	parseSpecificCommandLine( args );
	// Usually do process here
    }

    // This is an example of processing the arguments
    // In principle, use super.
    public void parseSpecificCommandLine( String[] args ) {
	try { 
	    CommandLine line = parseCommandLine( args );
	    if ( line == null ) return;
	    // Here deal with command specific arguments
	    for ( Object o : line.getArgList() ) {
		logger.info( " Arg: {}", o );
	    }
	    for ( Entry<Object,Object> m : parameters.entrySet() ) {
		logger.info( " Param: {} = {}", m.getKey(), m.getValue() );
	    }
	} catch( ParseException exp ) {
	    logger.error( exp.getMessage() );
	    usage();
	}
    }

    // In spirit, this is final
    public CommandLine parseCommandLine( String[] args ) throws ParseException {
	CommandLineParser parser = new PosixParser();
	CommandLine line = parser.parse( options, args );
	parameters = line.getOptionProperties( "D" );
	if ( line.hasOption( 'd' ) ) {
	    logger.warn( "debug command-line switch DEPRECATED, use logging" );
	}
	if ( line.hasOption( 'o' ) ) {
	    outputfilename = line.getOptionValue( 'o' );
	}
	if ( line.hasOption( 'P' ) ) {
	    try {
		String paramfile = line.getOptionValue( 'P' );
		parameters.loadFromXML( new FileInputStream( paramfile ) );
	    } catch ( Exception ex ) {
		logger.warn( "Cannot parse parameter file", ex );
	    }
	}
	if ( line.hasOption( 'h' ) ) {
	    usage();
	    line = null;
	}
	return line;
    }

    public void exit( int returnCode ) {
	System.exit( returnCode );
    }

    // This is an example of using the interface
    public abstract void usage();

    /*
     * The subclasses may define usage() by calling this:
     * usage( "java "+this.getClass().getName()+" [options] alignfile\nParse the given <alignfile> and prints it\nOptions:" );
     * In spirit, this is final
     */
    public void usage( String firstlines ) {
	usage( firstlines, "" );
    }
    public void usage( String firstlines, String footer ) {
	Package pkg = this.getClass().getPackage();
	String rfooter = footer;
	if ( pkg != null )
	    rfooter += "\n"+pkg.getImplementationTitle()+" "+pkg.getImplementationVersion();
	new HelpFormatter().printHelp( 80, firstlines, "\nOptions:", options, rfooter );
    }
}
