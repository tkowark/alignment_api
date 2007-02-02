/*
 * $Id$
 *
 * Copyright (C) 2003 The University of Manchester
 * Copyright (C) 2003 The University of Karlsruhe
 * Copyright (C) 2003-2007, INRIA Rhône-Alpes
 * Copyright (C) 2004, Université de Montréal
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

//*/3.0
//import fr.inrialpes.exmo.align.impl.URIAlignment;
//import fr.inrialpes.exmo.align.impl.OWLAPIAlignment;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.OntologyCache;

/*//3.0
import org.semanticweb.owl.util.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.io.owl_rdf.OWLRDFErrorHandler;
import org.semanticweb.owl.io.ParserException;
*/

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Hashtable;
import java.lang.Double;
import java.lang.Integer;
import java.lang.Long;

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
	--params=filename -p filename   Read the parameters in file
        --debug[=n] -d [n]              Report debug info at level n,
        --output=filename -o filename Output the alignment in filename
        --impl=className -i classname           Use the given alignment implementation.
        --renderer=className -r className       Specifies the alignment renderer
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

    //*/3.0
    //Hashtable loadedOntologies = null;
    OntologyCache loadedOntologies = null;

    //*/3.0
    //OWLRDFErrorHandler handler = null;

    public static void main(String[] args) {
	try { new Procalign().run( args ); }
	catch ( Exception ex ) { ex.printStackTrace(); };
    }

    public Alignment run(String[] args) throws Exception {
	//*/3.0
	//OWLOntology onto1 = null;
	//OWLOntology onto2 = null;
	URI onto1 = null;
	URI onto2 = null;
	AlignmentProcess result = null;
	String cutMethod = "hard";
	String initName = null;
	Alignment init = null;
	String alignmentClassName = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
	String filename = null;
	String paramfile = null;
	String rendererClass = "fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor";
	//PrintStream writer = null;
	PrintWriter writer = null;
	AlignmentVisitor renderer = null;
	int debug = 0;
	double threshold = 0;
	Parameters params = new BasicParameters();

	LongOpt[] longopts = new LongOpt[10];

	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[2] = new LongOpt("alignment", LongOpt.REQUIRED_ARGUMENT, null, 'a');
	longopts[3] = new LongOpt("renderer", LongOpt.REQUIRED_ARGUMENT, null, 'r');
	longopts[4] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[5] = new LongOpt("impl", LongOpt.REQUIRED_ARGUMENT, null, 'i');
	longopts[6] = new LongOpt("threshold", LongOpt.REQUIRED_ARGUMENT, null, 't');
	longopts[7] = new LongOpt("cutmethod", LongOpt.REQUIRED_ARGUMENT, null, 'T');
	longopts[8] = new LongOpt("params", LongOpt.REQUIRED_ARGUMENT, null, 'p');
	// Is there a way for that in LongOpt ???
	longopts[9] = new LongOpt("D", LongOpt.REQUIRED_ARGUMENT, null, 'D');

	Getopt g = new Getopt("", args, "ho:a:p:d::r:t:T:i:D:", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch (c) {
	    case 'h' :
		usage();
		return null;
	    case 'o' :
		/* Use filename instead of stdout */
		filename = g.getOptarg();
		break;
	    case 'p' :
		/* Read parameters from filename */
		paramfile = g.getOptarg();
		//try {
		//    FileInputStream fis = new FileInputStream( paramfile );
		//    System.setIn( fis );
		//} catch (Exception e) { e.printStackTrace(); }
		BasicParameters.read( params, paramfile);
		break;
	    case 'r' :
		/* Use the given class for rendering */
		rendererClass = g.getOptarg();
		break;
	    case 'i' :
		/* Use the given class for the alignment */
		alignmentClassName = g.getOptarg();
		break;
	    case 'a' :
		/* Use the given file as a partial alignment */
		initName = g.getOptarg();
		break;
	    case 't' :
		/* Threshold */
		threshold = Double.parseDouble(g.getOptarg());
		break;
	    case 'T' :
		/* Cut method */
		cutMethod = g.getOptarg();
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
	
	int i = g.getOptind();

	if (debug > 0) {
	    params.setParameter("debug", new Integer(debug));
	} else if ( params.getParameter("debug") != null ) {
	    debug = Integer.parseInt((String)params.getParameter("debug"));
	}

	//loadedOntologies = new Hashtable();
	loadedOntologies = new OntologyCache();
	if (debug > 0) {
	    params.setParameter("debug", new Integer(debug));
	} else if ( params.getParameter("debug") != null ) {
	    debug = Integer.parseInt((String)params.getParameter("debug"));
	}
	
	try {
	    
	    BasicConfigurator.configure();

	    URI uri1 = null;
	    URI uri2 = null;

	    if (args.length > i + 1) {
		uri1 = new URI(args[i++]);
		uri2 = new URI(args[i]);
	    } else if (initName == null) {
		System.err.println("Two URIs required");
		usage();
		System.exit(0);
	    }

	    /*
	    //3.0
	    handler = new OWLRDFErrorHandler() {
		    public void owlFullConstruct(int code, String message)
			throws SAXException {
		    }
		public void owlFullConstruct(int code, String message, Object o)
		    throws SAXException {
		}
		    public void error(String message) throws SAXException {
			throw new SAXException(message.toString());
		    }
		    public void warning(String message) throws SAXException {
			System.err.println("WARNING: " + message);
		    }
		};
	    */

	    if (debug > 0) System.err.println(" Handler set");

	    // JE: Procalign could be rendered independent from the 
	    // OWL-API by implementing a static class.NewIntance( u1, u2 );
	    // method
	    try {
		//*/3.0
		//if (uri1 != null) onto1 = loadOntology(uri1);
		//if (uri2 != null) onto2 = loadOntology(uri2);
		//if (debug > 0) System.err.println(" Ontology parsed");
		if (initName != null) {
		    // (otherwise)
		    //AlignmentParser aparser = new OWLAPIAlignmentParser(debug);
		    AlignmentParser aparser = new AlignmentParser(debug);
		    //*/3.0 loadedOntologies useless
		    Alignment al = aparser.parse( initName );
		    //*/3.O useless
		    init = al;
		    //init = OWLAPIAlignment.toOWLAPIAlignment( (URIAlignment)al, loadedOntologies );
		    //*/3.0
		    //onto1 = (OWLOntology) init.getOntology1();
		    //onto2 = (OWLOntology) init.getOntology2();
		    if (debug > 0) System.err.println(" Init parsed");
		}

		// Create alignment object
		//Object[] mparams = {(Object)onto1, (Object)onto2 };
		Object[] mparams = {};
		Class alignmentClass = Class.forName(alignmentClassName);
		//*/3.0
		//Class oClass = Class.forName("org.semanticweb.owl.model.OWLOntology");
		//Class oClass = Class.forName("java.lang.Object");
		//Class[] cparams = { oClass, oClass };
		Class[] cparams = {};
		java.lang.reflect.Constructor alignmentConstructor = alignmentClass.getConstructor(cparams);
		result = (AlignmentProcess)alignmentConstructor.newInstance(mparams);
		//*/3.0
		//result.init( onto1, onto2 ); //3.0
		//result.setFile1(uri1);
		//result.setFile2(uri2);
		result.init( uri1, uri2, loadedOntologies );
	    } catch (Exception ex) {
		System.err.println("Cannot create alignment "+alignmentClassName+"\n"
				   +ex.getMessage());
		usage();
		throw ex;
	    }

	    if (debug > 0) System.err.println(" Alignment structure created");
	    // Compute alignment
	    long time = System.currentTimeMillis();
	    result.align(  init, params ); // add opts
	    long newTime = System.currentTimeMillis();
	    result.setExtension( "time", Long.toString(newTime - time) );

	    // Thresholding
	    if (threshold != 0) result.cut( cutMethod, threshold );

	    if (debug > 0) System.err.println(" Alignment performed");
	    
	    // Set output file
	    OutputStream stream;
	    if (filename == null) {
		stream = System.out;
	    } else {
		stream = new FileOutputStream(filename);
	    }
	    writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);

	    // Result printing (to be reimplemented with a default value)
	    try {
		Object[] mparams = {(Object) writer };
		java.lang.reflect.Constructor[] rendererConstructors =
		    Class.forName(rendererClass).getConstructors();
		renderer =
		    (AlignmentVisitor) rendererConstructors[0].newInstance(mparams);
	    } catch (Exception ex) {
		System.err.println("Cannot create renderer "+rendererClass+"\n"
				   + ex.getMessage());
		usage();
		throw ex;
	    }

	    // Output
	    result.render(renderer);
	    writer.flush();
	    writer.close();

	} catch (Exception ex) {
	    throw ex;
	}
	return result;
    }

    /*
    public OWLOntology loadOntology(URI uri)
	throws ParserException, OWLException {
	OWLOntology parsedOnt = null;
	OWLRDFParser parser = new OWLRDFParser();
	parser.setOWLRDFErrorHandler(handler);
	parser.setConnection(OWLManager.getOWLConnection());
	parsedOnt = parser.parseOntology(uri);
	loadedOntologies.put(uri.toString(), parsedOnt);
	return parsedOnt;
    }
    */

    public void usage() {
	System.err.println("usage: Procalign [options] URI1 URI2");
	System.err.println("options are:");
	System.err.println("\t--impl=className -i classname\t\tUse the given alignment implementation.");
	System.err.println("\t--renderer=className -r className\tSpecifies the alignment renderer");
	System.err.println("\t--output=filename -o filename\tOutput the alignment in filename");
	System.err.println("\t--params=filename -p filename\tReads parameters from filename");
	System.err.println("\t--alignment=filename -a filename Start from an XML alignment file");
	System.err.println("\t--threshold=double -t double\tFilters the similarities under threshold");
	System.err.println("\t--cutmethod=hard|perc|prop|best|span -T hard|perc|prop|best|span\tmethod for computing the threshold");
	System.err.println("\t--debug[=n] -d [n]\t\tReport debug info at level n");
	System.err.println("\t-Dparam=value\t\t\tSet parameter");
	System.err.println("\t--help -h\t\t\tPrint this message");
	System.err.println("\n$Id$\n");
    }
}
