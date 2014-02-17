/*
 * $Id$
 *
 * Copyright (C) INRIA, 2014
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

package fr.inrialpes.exmo.align.cli;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import org.semanticweb.owl.align.AlignmentException;

/**
 * Transform a query according to an alignment
 * TransformQuery alignmentURI -q query [-e]
 * would be better with:
 * TransformQuery [-a alignmentURI] [-e] [-q query] < query
 * definitely...todo
 */

public class TransformQuery extends CommonCLI {
    final static Logger logger = LoggerFactory.getLogger( ParserPrinter.class );

    public TransformQuery() {
	super();
	options.addOption( "e", "echo", false, "Echo the input query" );
	//options.addOption( OptionBuilder.withLongOpt( "process" ).hasArg().withDescription( "Process the query against a particular CLASS" ).withArgName("CLASS").create( 'p' ) );
	options.addOption( OptionBuilder.withLongOpt( "query" ).hasArg().withDescription( "get the query from the corresponding FILE" ).withArgName("FILE").create( 'q' ) );
    }

    public static void main(String[] args) {
	try { new TransformQuery().run( args ); }
	catch (Exception ex) { ex.printStackTrace(); };
    }

    public void run(String[] args) throws Exception {
	BasicAlignment al = null;
	String alignmentURL = null;
	String query = "";
	String result = null;
	//String processorClass = null;
	String queryFile = null;
	PrintWriter writer = null;
	boolean echo = false;

	try { 
	    CommandLine line = parseCommandLine( args );
	    if ( line == null ) return; // --help

	    // Here deal with command specific arguments	    
	    //if ( line.hasOption( 'p' ) ) processorClass = line.getOptionValue( 'p' );
	    if ( line.hasOption( 'q' ) ) queryFile = line.getOptionValue( 'q' );
	    if ( line.hasOption( 'e' ) ) echo = true;
	    String[] argList = line.getArgs();
	    if ( argList.length > 0 ) {
		alignmentURL = argList[0];
	    } else {
		logger.error("Require the alignement URL");
		usage();
		System.exit(-1);
	    }
	} catch( ParseException exp ) {
	    logger.error( exp.getMessage() );
	    usage();
	    System.exit(-1);
	}

	try {

	    try {
		InputStream in = new FileInputStream( queryFile );
		BufferedReader reader =
		    new BufferedReader(new InputStreamReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
		    query += line + "\n";
		}
	    } catch (IOException x) {
		System.err.println(x);
		System.exit( -1 );
		//} finally {
	    }

	    // load the alignment
	    AlignmentParser aparser = new AlignmentParser();
	    al = (BasicAlignment)aparser.parse( alignmentURL );

	    if ( echo ) System.out.println( query );

	    // Create query processor
	    result = al.rewriteQuery( query, parameters );
	    
	    // Set output file
	    OutputStream stream;
	    if ( outputfilename == null ) {
		stream = System.out;
	    } else {
		stream = new FileOutputStream( outputfilename );
	    }
	    writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);

	    // Print
	    try {
		writer.println( result );
	    } finally {
		writer.flush();
		writer.close();
	    }	    
	    
	} catch (Exception ex) {
	    ex.printStackTrace();
	    logger.error( ex.getMessage() );
	    System.exit(-1);
	}
    }

    public void usage() {
	usage( "java "+this.getClass().getName()+" [options] alignmentURI query\nTransforms the given query and transforms it according to the alignment" );
    }
}
