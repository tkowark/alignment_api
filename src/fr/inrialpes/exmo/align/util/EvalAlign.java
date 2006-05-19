/*
 * $Id$
 *
 * Copyright (C) 2003-2006 INRIA Rhône-Alpes.
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
import org.semanticweb.owl.io.Renderer;
import org.semanticweb.owl.io.Parser;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Evaluator;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.impl.BasicEvaluator;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.BasicParameters;

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

/** A really simple utility that loads and alignment and prints it.
    A basic class for an OWL ontology alignment processing. The processor
    will parse ontologies, align them and renderings the resulting alignment.
    Command synopsis is as follows:
    
    <pre>
    java fr.inrialpes.exmo.align.util.EvalAlign [options] input [output]
    </pre>

    where the options are:
    <pre>
        --alignment=filename -a filename Start from an XML alignment file
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

public class EvalAlign {

    public static void main(String[] args) {
	try { run( args ); }
	catch (Exception e) { e.printStackTrace(); };
    }


    public static void run(String[] args) {
	Parameters params = new BasicParameters();
	Evaluator eval = null;
	String alignName1 = null;
	String alignName2 = null;
	String filename = null;
	String classname = null;
	PrintWriter writer = null;
	LongOpt[] longopts = new LongOpt[7];
	int debug = 0;
	
	// abcdefghijklmnopqrstuvwxyz?
	// x  x    i      x x x x    x 
	longopts[2] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	//longopts[3] = new LongOpt("renderer", LongOpt.REQUIRED_ARGUMENT, null, 'r');
	
	Getopt g = new Getopt("", args, "ho:d::i:", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch(c) {
	    case 'h':
		usage();
		return;
	    case 'o':
		/* Write warnings to stdout rather than stderr */
		filename = g.getOptarg();
		break;
	    case 'i':
		/* Write warnings to stdout rather than stderr */
		classname = g.getOptarg();
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

	params.setParameter("debug",new Integer(debug));
	// debug = ((Integer)params.getParameter("debug")).intValue();
	
	if (args.length > i+1 ) {
	    alignName1 = args[i];
	    alignName2 = args[i+1];
	} else {
	    System.out.println("Require two alignement filenames");
	    usage();
	    return;
	}

	if ( debug > 1 ) System.err.println(" Filename"+alignName1+"/"+alignName2);

	try {
	    // Load alignments
	    Hashtable loaded = new Hashtable();
	    AlignmentParser aparser1 = new AlignmentParser( debug );
	    Alignment align1 = aparser1.parse( alignName1, loaded );
	    if ( debug > 0 ) System.err.println(" Alignment structure1 parsed");
	    AlignmentParser aparser2 = new AlignmentParser( debug );
	    Alignment align2 = aparser2.parse( alignName2, loaded );
	    if ( debug > 0 ) System.err.println(" Alignment structure2 parsed");
	    // Create evaluator object
	    if ( classname != null ) {
		try {
		    Object [] mparams = {(Object)align1, (Object)align2};
		    Class oClass = Class.forName("org.semanticweb.owl.align.Alignment");
		    Class[] cparams = { oClass, oClass };
		    Class evaluatorClass =  Class.forName(classname);
		    java.lang.reflect.Constructor evaluatorConstructor = evaluatorClass.getConstructor(cparams);
		    eval = (Evaluator)evaluatorConstructor.newInstance(mparams);
		} catch (Exception ex) {
		    System.err.println("Cannot create evaluator " + 
				       classname + "\n" + ex.getMessage() );
		    usage();
		    return;
		}
	    } else { eval = new PRecEvaluator( align1, align2 ); };

	    // Compare
	    eval.eval(params) ;

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
	    eval.write( writer );
	    writer.flush();
	    
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public static void usage() {
	System.out.println("usage: EvalAlign [options] file1 file2");
	System.out.println("options are:");
	System.out.println("\t--debug[=n] -d [n]\t\tReport debug info at level n");
	System.out.println("\t--impl=className -i classname\t\tUse the given evaluator implementation.");
	System.out.println("\t--output=filename -o filename\tOutput the result in filename");
	System.out.println("\t--help -h\t\t\tPrint this message");

    }
}
