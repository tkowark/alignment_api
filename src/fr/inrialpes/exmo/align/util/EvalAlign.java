/*
 * $Id$
 *
 * Copyright (C) INRIA, 2003-2008
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

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.BasicParameters;

//Imported JAVA classes
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.lang.Integer;
import java.lang.Double;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;

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
	new EvalAlign().run( args );
    }


    public void run(String[] args) {
	BasicParameters params = new BasicParameters();
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
	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[2] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	//longopts[3] = new LongOpt("renderer", LongOpt.REQUIRED_ARGUMENT, null, 'r');
	longopts[4] = new LongOpt("impl", LongOpt.REQUIRED_ARGUMENT, null, 'i');
	
	Getopt g = new Getopt("", args, "ho:d::i:", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch(c) {
	    case 'h':
		usage();
		return;
	    case 'o':
		/* Output */
		filename = g.getOptarg();
		break;
	    case 'i':
		/* Evaluator class */
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

	params.setParameter( "debug", Integer.toString( debug ) );
	// debug = Integer.parseInt( params.getParameter("debug") );
	
	if (args.length > i+1 ) {
	    alignName1 = args[i];
	    alignName2 = args[i+1];
	} else {
	    System.err.println("Require two alignement filenames");
	    usage();
	    return;
	}

	if ( debug > 1 ) System.err.println(" Filename"+alignName1+"/"+alignName2);

	Alignment align1=null, align2 = null;
	try {
	    // Load alignments
	    AlignmentParser aparser = new AlignmentParser( debug );
	    align1 = aparser.parse( alignName1 );
	    if ( debug > 0 ) System.err.println(" Alignment structure1 parsed");
	    aparser.initAlignment( null );
	    align2 = aparser.parse( alignName2 );
	    if ( debug > 0 ) System.err.println(" Alignment structure2 parsed");
	} catch ( Exception ex ) { ex.printStackTrace(); }

	try {
	    // Create evaluator object
	    if ( classname != null ) {
		try {
		    Object [] mparams = {(Object)align1, (Object)align2};
		    Class oClass = Class.forName("org.semanticweb.owl.align.Alignment");
		    Class[] cparams = { oClass, oClass };
		    Class evaluatorClass =  Class.forName(classname);
		    java.lang.reflect.Constructor evaluatorConstructor = evaluatorClass.getConstructor(cparams);
		    eval = (Evaluator)evaluatorConstructor.newInstance(mparams);
		} catch (ClassNotFoundException ex) {
		    ex.printStackTrace();
		} catch (InstantiationException ex) {
		    ex.printStackTrace();
		} catch (InvocationTargetException ex) {
		    ex.printStackTrace();
		} catch (IllegalAccessException ex) {
		    ex.printStackTrace();
		} catch (NoSuchMethodException ex) {
		    ex.printStackTrace();
		    usage();
		    return;
		}
	    } else { eval = new PRecEvaluator( align1, align2 ); };

	    // Compare
	    eval.eval(params) ;
	} catch (AlignmentException ex) { ex.printStackTrace(); }
	
	// Set output file
	try {
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
	    writer.close();
	} catch ( IOException ex ) {
	    ex.printStackTrace();
	}
    }

    public void usage() {
	System.err.println("usage: EvalAlign [options] file1 file2");
	System.err.println("options are:");
	System.err.println("\t--debug[=n] -d [n]\t\tReport debug info at level n");
	System.err.println("\t--impl=className -i classname\t\tUse the given evaluator implementation.");
	System.err.println("\t--output=filename -o filename\tOutput the result in filename");
	System.err.println("\t--help -h\t\t\tPrint this message");
	System.err.print("\n"+EvalAlign.class.getPackage().getImplementationTitle()+" "+EvalAlign.class.getPackage().getImplementationVersion());
	System.err.println(" ($Id$)\n");

    }
}
