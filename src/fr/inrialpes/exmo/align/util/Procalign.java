/*
 * $Id$
 *
 * Copyright (C) 2003 The University of Manchester
 * Copyright (C) 2003 The University of Karlsruhe
 * Copyright (C) 2003-2004, INRIA Rhône-Alpes
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
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

/* This program is an adaptation of the Processor.java class of the
   initial release of the OWL-API
*/
package fr.inrialpes.exmo.align.util;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicParameters;

import org.semanticweb.owl.util.OWLConnection;
import org.semanticweb.owl.util.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.io.owl_rdf.OWLRDFErrorHandler;
import org.semanticweb.owl.io.ParserException;
import org.semanticweb.owl.io.Parser;

import java.io.PrintStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Hashtable;
import java.lang.Double;
import java.lang.Integer;

import org.xml.sax.SAXException;

import org.apache.log4j.BasicConfigurator;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

import fr.inrialpes.exmo.align.parser.AlignmentParser;

/** A basic class for an OWL ontology alignment processing. The processor
    will parse ontologies, align them and renderings the resulting alignment.
    Command synopsis is as follows:
    
    <pre>
    java fr.inrialpes.exmo.align.util.Procalign [options] onto1 onto2 [output]
    </pre>

    or better
    <pre>
    java -jar procalign.jar onto1 onto2
    </pre>

    where the options are:
    <pre>
        --alignment=filename -a filename Start from an XML alignment file
        --debug[=n] -d [n]              Report debug info at level n,
        --output=filename -o filename Output the alignment in filename
	--renderer=renderer -r renderer
        --help -h                       Print this message
    </pre>

    <CODE>onto1</CODE> and <CODE>onto2</CODE> should be URLs. If output is
    requested (<CODE>-o</CODE> flags), then output will be written to
    <CODE>output</CODE> if present, stdout by default.

<pre>
$Id$
</pre>

@author Sean K. Bechhofer
@author Jérôme Euzenat
    */

public class Procalign {

    static Hashtable loadedOntologies = null;
    
    static OWLRDFErrorHandler handler = null;

    public static void main(String[] args) {
	Parameters params = null;
	OWLOntology onto1 = null;
	OWLOntology onto2 = null;
	AlignmentProcess result = null;
	String initName = null;
	Alignment init = null;
	String alignmentClassName = "fr.inrialpes.exmo.align.impl.ClassNameEqAlignment";
	String filename = null;
	String rendererClass = "fr.inrialpes.exmo.align.impl.RDFRendererVisitor";
	PrintStream writer = null;
	AlignmentVisitor renderer = null;
	int debug = 0;
	double threshold = 0;
	
	LongOpt[] longopts = new LongOpt[8];

	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[2] = new LongOpt("alignment", LongOpt.REQUIRED_ARGUMENT, null, 'a');
	longopts[3] = new LongOpt("renderer", LongOpt.REQUIRED_ARGUMENT, null, 'r');
	longopts[4] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[5] = new LongOpt("impl", LongOpt.REQUIRED_ARGUMENT, null, 'i');
	longopts[7] = new LongOpt("threshold", LongOpt.REQUIRED_ARGUMENT, null, 't');
	
	Getopt g = new Getopt("", args, "ho:a:d::r:t:i:", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch(c) {
	    case 'h':
		usage();
		System.exit(0);
	    case 'o':
		/* Write warnings to stdout rather than stderr */
		filename = g.getOptarg();
		break;
	    case 'r':
		/* Use the given class for rendering */
		rendererClass = g.getOptarg();
		break;
	    case 'i':
		/* Use the given class for the alignment */
		alignmentClassName = g.getOptarg();
		break;
	    case 'a':
		/* Use the given file as a partial alignment */
		initName = g.getOptarg();
		break;
	    case 't':
		/* Threshold */
		threshold = Double.parseDouble(g.getOptarg());
		break;
	    case 'd':
		/* Debug level  */
		// Should convert into integer
		arg = g.getOptarg();
		if ( arg != null ) debug = 2;
		else debug = 4; // !!
		break;
	    }
	}
	
	int i = g.getOptind();
	
	loadedOntologies = new Hashtable();
	params = new BasicParameters();
	if ( debug > 0 ) params.setParameter("debug",new Integer(debug));
	// debug = ((Integer)params.getParameter("debug")).intValue();

	try {
	    
	    BasicConfigurator.configure();

	    URI uri1 = null;
	    URI uri2 = null;
	    
	    if (args.length > i+1 ) {
		uri1 = new URI( args[i++] );
		uri2 = new URI( args[i] );
	    } else if ( initName == null ){
		System.out.println("Two URIs required");
		usage();
		System.exit(0);
	    }
	    
	    handler = new OWLRDFErrorHandler(){
		    public void owlFullConstruct( int code,
						  String message ) throws SAXException {
		    }
		    public void error( String message ) throws SAXException {
			throw new SAXException( message.toString() );
		    }
		    public void warning( String message ) throws SAXException {
			System.out.println("WARNING: " + message);
		    }
		};

	    if ( debug > 0 ) System.err.println(" Handler set");

	    try {
		if ( uri1 != null ) onto1 = loadOntology(uri1);
		if ( uri2 != null ) onto2 = loadOntology(uri2);
		if ( debug > 0 ) System.err.println(" Ontology parsed");
		if ( initName != null ){
		    AlignmentParser aparser = new AlignmentParser( debug );
		    init = aparser.parse( initName, loadedOntologies );
		    onto1 = (OWLOntology)init.getOntology1();
		    onto2 = (OWLOntology)init.getOntology2();
		    if ( debug > 0 ) System.err.println(" Init parsed");
		}

		// Create alignment object
		Object [] mparams = {(Object)onto1, (Object)onto2};
		Class alignmentClass =  Class.forName(alignmentClassName);
		java.lang.reflect.Constructor[] alignmentConstructors = alignmentClass.getConstructors();
                result = (AlignmentProcess)alignmentConstructors[0].newInstance(mparams);
		result.setFile1( uri1 );
		result.setFile2( uri2 );
	    } catch (Exception ex) {
		System.err.println("Cannot create alignment " + 
				   alignmentClassName + "\n" + ex.getMessage() );
		usage();
		System.exit(0);
	    }

	    if ( debug > 0 ) System.err.println(" Alignment structure created");
	    // Compute alignment
	    result.align(init,params); // add opts

	    // Thresholding
	    if ( threshold != 0 ) 
		{ ((BasicAlignment)result).cut( threshold ); };

	    if ( debug > 0 ) System.err.println(" Alignment performed");

	    // Set output file
	    if ( filename == null ) {
	    	writer = (PrintStream)System.out;
	    } else {
		writer = new PrintStream(new FileOutputStream( filename ));
	    }

	    // Result printing (to be reimplemented with a default value)
	    try {
		Object [] mparams = {(Object)writer};
		java.lang.reflect.Constructor[] rendererConstructors = Class.forName(rendererClass).getConstructors();
		renderer = (AlignmentVisitor)rendererConstructors[0].newInstance(mparams);
	    } catch (Exception ex) {
		System.err.println("Cannot create renderer " + 
				   rendererClass + "\n" + ex.getMessage() );
		usage();
		System.exit(0);
	    }

	    // Output
	    result.render( writer, renderer );
	    
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public static OWLOntology loadOntology( URI uri ) throws ParserException,OWLException {
	OWLOntology parsedOnt = null;
	OWLRDFParser parser = new OWLRDFParser();
	parser.setOWLRDFErrorHandler( handler );
	parser.setConnection( OWLManager.getOWLConnection() );
	parsedOnt = parser.parseOntology( uri );
	loadedOntologies.put( uri.toString(), parsedOnt );
	return parsedOnt;
    }

    public static void usage() {
	System.out.println("usage: Procalign [options] URI1 URI2");
	System.out.println("options are:");
	System.out.println("\t--impl=className -i classname\t\tUse the given alignment implementation.");
	System.out.println("\t--renderer=className -r className\tSpecifies the alignment renderer");
	System.out.println("\t--output=filename -o filename\tOutput the alignment in filename");
	System.out.println("\t--alignment=filename -a filename Start from an XML alignment file");
	System.out.println("\t--threshold=double -t double\tFilters the similarities under threshold");
	System.out.println("\t--debug[=n] -d [n]\t\tReport debug info at level n");
	System.out.println("\t--help -h\t\t\tPrint this message");

    }
}
