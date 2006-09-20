/*
 * $Id$
 *
 * Copyright (C) 2003 The University of Manchester
 * Copyright (C) 2003 The University of Karlsruhe
 * Copyright (C) 2003-2005, INRIA Rhône-Alpes
 * Copyright (C) 2004, Université de Montréal
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

/* This program evaluates the results of several ontology aligners in a row.
*/
package fr.inrialpes.exmo.align.util;

import org.semanticweb.owl.model.OWLOntology;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.eval.PRGraphEvaluator;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.lang.Double;
import java.lang.Integer;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;

import org.xml.sax.SAXException;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

import fr.inrialpes.exmo.align.parser.AlignmentParser;

/** A basic class for synthesizing the alignment results of an algorithm by a
 * precision recall graph.
 *
 * These graphs are however computed on averaging the precision recall/graphs
 * on test directories instead of recording the actual precision recall graphs
 * which would amount at recoding all the valid and invalid alignment cells and
 * their level.
 *  
 *  <pre>
 *  java -cp procalign.jar fr.inrialpes.exmo.align.util.GenPlot [options]
 *  </pre>
 *
 *  where the options are:
 *  <pre>
 *  -o filename --output=filename
 *  -d debug --debug=level
 *  -l list of compared algorithms
 *  -t output --type=output: xml/tex/html/ascii
 * </pre>
 *
 * The input is taken in the current directory in a set of subdirectories (one per
 * test) each directory contains a the alignment files (one per algorithm) for that test and the
 * reference alignment file.
 *
 * If output is
 * requested (<CODE>-o</CODE> flags), then output will be written to
 *  <CODE>output</CODE> if present, stdout by default. In case of the Latex output, there are numerous files generated (regardless the <CODE>-o</CODE> flag).
 *
 * <pre>
 * $Id$
 * </pre>
 *
 * @author Jérôme Euzenat
 */

public class GenPlot {

    static int STEP = 10;
    static Parameters params = null;
    static Vector listAlgo;
    static String fileNames = "";
    static String outFile = null;
    static String type = "tsv";
    static int debug = 0;
    static Hashtable loaded = null;
    static PrintWriter output = null;

    public static void main(String[] args) {
	try { run( args ); }
	catch (Exception ex) { ex.printStackTrace(); };
    }

    public static void run(String[] args) throws Exception {
	LongOpt[] longopts = new LongOpt[8];
	loaded = new Hashtable();

 	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[3] = new LongOpt("type", LongOpt.REQUIRED_ARGUMENT, null, 't');
	longopts[4] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[6] = new LongOpt("list", LongOpt.REQUIRED_ARGUMENT, null, 'l');

	Getopt g = new Getopt("", args, "ho:d::l:t:", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch (c) {
	    case 'h' :
		usage();
		return;
	    case 'o' :
		/* Write output here */
		outFile = g.getOptarg();
		break;
	    case 't' :
		/* Type of output (tex/tsv(/html/xml/ascii)) */
		type = g.getOptarg();
		break;
	    case 'l' :
		/* List of filename */
		fileNames = g.getOptarg();
		break;
	    case 'd' :
		/* Debug level  */
		arg = g.getOptarg();
		if ( arg != null ) debug = Integer.parseInt(arg.trim());
		else debug = 4;
		break;
	    }
	}

	// JE: StringTokenizer is obsoleted in Java 1.4 in favor of split: to change
	listAlgo = new Vector();
	StringTokenizer st = new StringTokenizer(fileNames,",");
	while (st.hasMoreTokens()) {
	    listAlgo.add(st.nextToken());
	}

	params = new BasicParameters();
	if (debug > 0) params.setParameter("debug", new Integer(debug-1));

	params.setParameter("step", new Integer(STEP));

	// Set output file
	OutputStream stream;
	if (outFile == null) {
	    stream = System.out;
	} else {
	    stream = new FileOutputStream(outFile);
	}
	output = new PrintWriter (
		   new BufferedWriter(
		     new OutputStreamWriter( stream, "UTF-8" )), true);

	// type
	if ( type.equals("tsv") ){
	    printTSV( iterateDirectories() );
	} else if ( type.equals("tex") ) {
	    printPGFTex( iterateDirectories() );
	} else System.err.println("Flag -t "+type+" : not implemented yet");
    }

    /**
     * Iterate on each subdirectory
     * Returns a vector[ each algo ] of vector [ each point ]
     * The points are computed by aggregating the values
     *  (and in the end computing the average)
     */
    public static double[][] iterateDirectories (){
	File [] subdir = null;
	try {
	    subdir = (new File(System.getProperty("user.dir"))).listFiles();
	} catch (Exception e) {
	    System.err.println("Cannot stat dir "+ e.getMessage());
	    usage();
	}

	// Initialize the vector of results
	double[][] result = new double[listAlgo.size()][STEP+1];
	for( int i=0; i < listAlgo.size(); i++){
	    for( int j=0; j <= STEP; j++){
		result[i][j] = 0.0;
	    }
	}
	
	int size = 0;
	// Evaluate the results in each directory
	for ( int k = subdir.length-1 ; k >= 0; k-- ) {
	    if( subdir[k].isDirectory() ) {
		// eval the alignments in a subdirectory
		iterateAlignments( subdir[k], result );
		size++;
	    }
	}

	// Compute the average by dividing each value by the number of tests
	// (not a very good method anyway)
	for( int i=0; i < listAlgo.size(); i++){
	    for( int j=0; j <= STEP; j++){
		result[i][j] = result[i][j] / size;
	    }
	}
	
	return result;
    }

    public static void iterateAlignments ( File dir, double[][] result ) {
	String prefix = dir.toURI().toString()+"/";
	int i = 0;

	if( debug > 0 ) System.err.println("Directory : "+dir);
	// for all alignments there,
	for ( Enumeration e = listAlgo.elements() ; e.hasMoreElements() ; i++) {
	    String algo = (String)e.nextElement();
	    // call eval
	    if ( debug > 0 ) System.err.println("  Considering result "+algo+" ("+i+")");
	    PRGraphEvaluator evaluator = eval( prefix+"refalign.rdf", prefix+algo+".rdf");
	    // store the result
	    if ( evaluator != null ){
		for( int j = 0; j <= STEP ; j++ ){
		    result[i][j] = result[i][j] + evaluator.getPrecision(j);
		}
	    }
	}
	// Unload the ontologies.
	try {
	    for ( Enumeration e = loaded.elements() ; e.hasMoreElements();  ){
		OWLOntology o = (OWLOntology)e.nextElement();
		o.getOWLConnection().notifyOntologyDeleted( o );
	    }
	} catch (Exception ex) { System.err.println(ex); };
    }

    public static PRGraphEvaluator eval( String alignName1, String alignName2 ) {
	PRGraphEvaluator eval = null;
	try {
	    int nextdebug;
	    if ( debug < 2 ) nextdebug = 0;
	    else nextdebug = debug - 2;
	    // Load alignments
	    AlignmentParser aparser1 = new AlignmentParser( nextdebug );
	    Alignment align1 = aparser1.parse( alignName1, loaded );
	    if ( debug > 1 ) System.err.println(" Alignment structure1 parsed");
	    AlignmentParser aparser2 = new AlignmentParser( nextdebug );
	    Alignment align2 = aparser2.parse( alignName2, loaded );
	    if ( debug > 1 ) System.err.println(" Alignment structure2 parsed");
	    // Create evaluator object
	    eval = new PRGraphEvaluator( align1, align2 );
	    // Compare
	    params.setParameter( "debug", new Integer( nextdebug ) );
	    eval.eval( params ) ;

	    // Unload the ontologies.
	    for ( Enumeration e = loaded.elements() ; e.hasMoreElements();  ){
		OWLOntology o = (OWLOntology)e.nextElement();
		o.getOWLConnection().notifyOntologyDeleted( o );
	    }
	} catch (Exception ex) { ex.printStackTrace(); };
	return eval;
    }


    /**
     * This does average plus plot
     *
     */
    public static void printPGFTex( double[][] result ){
	int i = 0;
	String marktable[] = { "+", "*", "x", "-", "|", "o", "asterisk", "star", "oplus", "oplus*", "otimes", "otimes*", "square", "square*", "triangle", "triangle*", "diamond", "diamond*", "pentagon", "pentagon*"};
	String colortable[] = { "black", "white", "red", "green", "blue", "cyan", "magenta", "yellow" }	;
	output.println("\\documentclass[11pt]{book}");
	output.println();
	output.println("\\usepackage{pgflibraryplotmarks}");
	output.println("\\usepackage{pgf}");
	output.println("\\usepackage{tikz}");
	output.println();
	output.println("\\begin{document}");
	output.println("\\date{today}");
	output.println("");
	output.println("\\pgfuseplotmark{-}");
	output.println("\\pgfuseplotmark{|}");
	output.println("\\pgfuseplotmark{o}");
	output.println("\\pgfuseplotmark{asterisk}");
	output.println("\\pgfuseplotmark{star}"); 
	output.println("\\pgfuseplotmark{oplus}");
	output.println("\\pgfuseplotmark{oplus*}");
	output.println("\\pgfuseplotmark{otimes}"); 
	output.println("\\pgfuseplotmark{otimes*}");
	output.println("\\pgfuseplotmark{square}");
	output.println("\\pgfuseplotmark{square*}");
	output.println("\\pgfuseplotmark{triangle}");
	output.println("\\pgfuseplotmark{triangle*}");
	output.println("\\pgfuseplotmark{diamond}");
	output.println("\\pgfuseplotmark{diamond*}");
	output.println("\\pgfuseplotmark{pentagon}");
	output.println("\\pgfuseplotmark{pentagon*}");
	output.println("");
	output.println("\n%% Plot generated by GenPlot of alignapi");
	output.println("\\begin{tikzpicture}[cap=round]");
	output.println("% Draw grid");
	output.println("\\draw[step="+(STEP/10)+"cm,very thin,color=gray] (-0.2,-0.2) grid ("+STEP+","+STEP+");");
	output.println("\\draw[->] (-0.2,0) -- (10.2,0) node[below] {$recall$}; ");
	output.println("\\draw (0,-0.3) node {0.}; ");
	output.println("\\draw (10,-0.3) node {1.}; ");
	output.println("\\draw[->] (0,-0.2) -- (0,10.2) node[above] {$precision$}; ");
	output.println("\\draw (-0.3,0) node {0.}; ");
	output.println("\\draw (-0.3,10) node {1.}; ");
	output.println("% Plots");
	for ( Enumeration e = listAlgo.elements() ; e.hasMoreElements() ; i++) {
	    output.println("\\draw["+colortable[i%8]+"] plot[mark="+marktable[i%20]+",smooth] file {"+(String)e.nextElement()+".table};");
	    //output.println("\\draw plot[mark=+,smooth] file {"+(String)e.nextElement()+".table};");
	}
	// And a legend
	output.println("% Legend");
	output.println("\\end{tikzpicture}");
	output.println();
	output.println("\\end{document}");

	i = 0;
	for ( Enumeration e = listAlgo.elements() ; e.hasMoreElements() ; i++) {
	    String algo = (String)e.nextElement();
	    // Open one file
	    PrintWriter writer;
	    try {
		writer = new PrintWriter (
				    new BufferedWriter(
                                       new OutputStreamWriter(
                                            new FileOutputStream(algo+".table"), "UTF-8" )), true);
		// Print header
		writer.println("#Curve 0, "+(STEP+1)+" points");
		writer.println("#x y type");
		writer.println("%% Plot generated by GenPlot of alignapi");
		writer.println("%% Include in PGF tex by:\n");
		writer.println("%% \\begin{tikzpicture}[cap=round]");
		writer.println("%% \\draw[step="+(STEP/10)+"cm,very thin,color=gray] (-0.2,-0.2) grid ("+STEP+","+STEP+");");
		writer.println("%% \\draw[->] (-0.2,0) -- (10.2,0) node[right] {$precision$}; ");
		writer.println("%% \\draw[->] (0,-0.2) -- (0,10.2) node[above] {$recall$}; ");
		writer.println("%% \\draw plot[mark=+,smooth] file {"+algo+".table};");
		writer.println("%% \\end{tikzpicture}");
		writer.println();
		for( int j = 0; j <= STEP; j++ ){
		    writer.print((double)j*10/STEP);
		    writer.println(" "+result[i][j]*10);
		}
		writer.close();
	    } catch (Exception ex) { ex.printStackTrace(); }
	    // UnsupportedEncodingException + FileNotFoundException
	}
    }

    public static void printTSV( double[][] result ) {
	// Print first line
	for ( Enumeration e = listAlgo.elements() ; e.hasMoreElements() ; ) {
	    output.print("\t"+(String)e.nextElement() );
	}
	output.println();

	// Print others
	for( int j = 0; j <= STEP; j++ ){
	    output.print((double)j/STEP);
	    for( int i = 0; i < listAlgo.size(); i++ ){
		output.print("\t"+result[i][j]);
	    }
	    output.println();
	}
    }

    public static void usage() {
	System.out.println("usage: GenPlot [options]");
	System.out.println("options are:");
	System.out.println("\t--type=tsv|tex|(html|xml) -t tsv|tex|(html|xml)\tSpecifies the output format");
	System.out.println("\t--list=algo1,...,algon -l algo1,...,algon\tSequence of the filenames to consider");
	System.out.println("\t--debug[=n] -d [n]\t\tReport debug info at level n");
	System.out.println("\t--help -h\t\t\tPrint this message");
    }
}
