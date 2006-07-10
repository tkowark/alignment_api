/*
 * $Id$
 *
 * Copyright (C) 2003-2004 INRIA Rhône-Alpes.
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

/* 
*/
package fr.inrialpes.exmo.align.util;

//Imported JAVA classes
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.lang.Integer;
import java.lang.Double;
import java.util.Observer;
import java.util.Stack;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Observer;
import java.util.Hashtable;

import org.semanticweb.owl.util.OWLConnection;
import org.semanticweb.owl.util.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.io.owl_rdf.OWLRDFErrorHandler;
import org.semanticweb.owl.io.Parser;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Hashtable;

import org.xml.sax.SAXException;

import org.apache.log4j.BasicConfigurator;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

import fr.inrialpes.exmo.align.parser.AlignmentParser;

/** A really simple utility that loads and alignment and prints it.
    A basic class for an OWL ontology alignment processing. The processor
    will parse ontologies, align them and renderings the resulting alignment.
    Command synopsis is as follows:
    
    <pre>
    java fr.inrialpes.exmo.align.util.ParserPrinter [options] input [output]
    </pre>

    where the options are:
    <pre>
        --inverse -i              Inverse first and second ontology
	--renderer=className -r className  Use the given class for output.
        --debug[=n] -d [n]              Report debug info at level n,
        --output=filename -o filename Output the alignment in filename
        --help -h                       Print this message
    </pre>

    The <CODE>input</CODE> is a filename. If output is
    requested (<CODE>-o</CODE> flags), then output will be written to
    <CODE>output</CODE> if present, stdout by default.

<pre>
$Id$
</pre>

@author Jérôme Euzenat
    */

public class ParserPrinter {

    public static void main(String[] args) {
	try { run( args ); }
	catch (Exception ex) { ex.printStackTrace(); };
    }

    public static void run(String[] args) throws Exception {
	Alignment result = null;
	String initName = null;
	String filename = null;
	PrintWriter writer = null;
	AlignmentVisitor renderer = null;
	LongOpt[] longopts = new LongOpt[7];
	int debug = 0;
	String rendererClass = null;
	boolean inverse = false;	
	double threshold = 0;
	String cutMethod = "hard";

	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[2] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[3] = new LongOpt("renderer", LongOpt.REQUIRED_ARGUMENT, null, 'r');
	longopts[4] = new LongOpt("inverse", LongOpt.NO_ARGUMENT, null, 'i');
	longopts[5] = new LongOpt("threshold", LongOpt.REQUIRED_ARGUMENT, null, 't');
	longopts[6] = new LongOpt("cutmethod", LongOpt.REQUIRED_ARGUMENT, null, 'T');
	
	Getopt g = new Getopt("", args, "hio:t:T:d::r:", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch(c) {
	    case 'h':
		usage();
		return;
	    case 'i':
		inverse = true;
		break;
	    case 'o':
		/* Write warnings to stdout rather than stderr */
		filename = g.getOptarg();
		break;
	    case 'r':
		/* Use the given class for rendernig */
		rendererClass = g.getOptarg();
		break;
	    case 't' :
		/* Threshold */
		threshold = Double.parseDouble(g.getOptarg());
		break;
	    case 'T' :
		/* Cut method */
		cutMethod = g.getOptarg();
		break;
	    case 'd':
		/* Debug level  */
		arg = g.getOptarg();
		if ( arg != null ) debug = Integer.parseInt(arg.trim());
		else debug = 4;
		break;
	    }
	}
	
	int i = g.getOptind();
	
	if (args.length > i ) {
	    initName = args[i];
	} else {
	    System.out.println("Require the alignement filename");
	    usage();
	    return;
	}

	if ( debug > 1 ) System.err.println(" Filename"+initName);

	try {
	    AlignmentParser aparser = new AlignmentParser( debug );
	    result = aparser.parse( initName, new Hashtable() );
	    if ( debug > 0 ) System.err.println(" Alignment structure parsed");
	    // Set output file
	    OutputStream stream;
	    if (filename == null) {
		//writer = (PrintStream) System.out;
		stream = System.out;
	    } else {
		//writer = new PrintStream(new FileOutputStream(filename));
		stream = new FileOutputStream(filename);
	    }
	    writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);

	    if ( inverse ) result = result.inverse();
	    
	    // Thresholding
	    if (threshold != 0) result.cut( cutMethod, threshold );

	    //result.write( writer );
	    if ( rendererClass == null ) renderer = new RDFRendererVisitor( writer );
	    else {
		try {
		    Object[] mparams = {(Object) writer };
		    java.lang.reflect.Constructor[] rendererConstructors =
			Class.forName(rendererClass).getConstructors();
		    renderer =
			(AlignmentVisitor) rendererConstructors[0].newInstance(mparams);
		} catch (Exception ex) {
		    System.err.println("Cannot create renderer " + 
				       rendererClass + "\n" + ex.getMessage() );
		    usage();
		    return;
		}
	    }
	    result.render( renderer );
	    writer.flush();
	    writer.close();
	    
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public static void usage() {
	System.out.println("usage: ParserPrinter [options] URI");
	System.out.println("options are:");
	//System.out.println("\t--alignment=filename -a filename Start from an XML alignment file");
	System.out.println("\t--debug[=n] -d [n]\t\tReport debug info at level ,");
	System.out.println("\t--renderer=className -r\t\tUse the given class for output.");
	System.out.println("\t--inverse -i\t\tInverse first and second ontology");
	System.out.println("\t--output=filename -o filename\tOutput the alignment in filename");
	System.out.println("\t--help -h\t\t\tPrint this message");

    }
}
