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
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
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

/** A basic class for synthesizing the results of a set of alignments provided by
    different algorithms. The output is a table showing various classical measures
    for each test and for each algorithm. Average is also computed as Harmonic means.
    
    <pre>
    java -cp procalign.jar fr.inrialpes.exmo.align.util.GroupEval [options]
    </pre>

    where the options are:
    <pre>
    -o filename --output=filename
    -f format = prfmot (precision/recall/fallout/f-measure/overall/time) --format=prfmot
    -d debug --debug=level
    -r filename --reference=filename
    -s algo/measure
    -l list of compared algorithms
    -t output --type=output: xml/tex/html/ascii
   </pre>

   The input is taken in the current directory in a set of subdirectories (one per
   test which will be rendered by a line) each directory contains a number of
   alignment files (one per algorithms which will be renderer as a column).

    If output is requested (<CODE>-o</CODE> flags), then output will be written to
    <CODE>output</CODE> if present, stdout by default.

<pre>
$Id$
</pre>

@author Sean K. Bechhofer
@author Jérôme Euzenat
    */

public class GroupEval {

    static Parameters params = null;
    static String filename = null;
    static String reference = "refalign.rdf";
    static String format = "pr";
    static int fsize = 2;
    static String type = "html";
    static String dominant = "s";
    static Vector listAlgo = null;
    static int debug = 0;
    static String color = null;
    static Hashtable loaded = null;

    public static void main(String[] args) {
	try { run( args ); }
	catch (Exception ex) { ex.printStackTrace(); };
    }

    public static void run(String[] args) throws Exception {
	String listFile = "";
	LongOpt[] longopts = new LongOpt[9];
	loaded = new Hashtable();

 	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[2] = new LongOpt("format", LongOpt.REQUIRED_ARGUMENT, null, 'f');
	longopts[3] = new LongOpt("type", LongOpt.REQUIRED_ARGUMENT, null, 't');
	longopts[4] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[5] = new LongOpt("sup", LongOpt.REQUIRED_ARGUMENT, null, 's');
	longopts[6] = new LongOpt("list", LongOpt.REQUIRED_ARGUMENT, null, 'l');
	longopts[7] = new LongOpt("color", LongOpt.OPTIONAL_ARGUMENT, null, 'c');
	longopts[8] = new LongOpt("reference", LongOpt.REQUIRED_ARGUMENT, null, 'r');

	Getopt g = new Getopt("", args, "ho:a:d::l:f:t:r:c::", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch (c) {
	    case 'h' :
		usage();
		return;
	    case 'o' :
		/* Write output here */
		filename = g.getOptarg();
		break;
	    case 'r' :
		/* File name for the reference alignment */
		reference = g.getOptarg();
		break;
	    case 'f' :
		/* Sequence of results to print */
		format = g.getOptarg();
		break;
	    case 't' :
		/* Type of output (tex/html/xml/ascii) */
		type = g.getOptarg();
		break;
	    case 's' :
		/* Print per type or per algo */
		dominant = g.getOptarg();
		break;
	    case 'c' :
		/* Print colored lines */
		arg = g.getOptarg();
		if ( arg != null )  {
		    color = arg.trim();
		} else color = "lightblue";
		break;
	    case 'l' :
		/* List of filename */
		listFile = g.getOptarg();
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
	StringTokenizer st = new StringTokenizer(listFile,",");
	while (st.hasMoreTokens()) {
	    listAlgo.add(st.nextToken());
	}

	params = new BasicParameters();
	if (debug > 0) params.setParameter("debug", new Integer(debug-1));

	print( iterateDirectories() );
    }

    public static Vector iterateDirectories (){
	Vector result = null;
	File [] subdir = null;
	try {
	    subdir = (new File(System.getProperty("user.dir"))).listFiles();
	} catch (Exception e) {
	    System.err.println("Cannot stat dir "+ e.getMessage());
	    usage();
	}
	int size = subdir.length;
	result = new Vector(size);
	int i = 0;
	for ( int j=0 ; j < size; j++ ) {
	    if( subdir[j].isDirectory() ) {
		if ( debug > 0 ) System.err.println("\nEntering directory "+subdir[j]);
		// eval the alignments in a subdirectory
		// store the result
		Object vect = (Object)iterateAlignments( subdir[j] );
		if ( vect != null ){
		    result.add(i, vect);
		    i++;
		}
	    }
	}
	return result;
    }

    public static Vector iterateAlignments ( File dir ) {
	String prefix = dir.toURI().toString()+"/";
	Vector result = new Vector();
	boolean ok = false;
	result.add(0,(Object)dir.getName().toString());
	int i = 1;
	// for all alignments there,
	for ( Enumeration e = listAlgo.elements() ; e.hasMoreElements() ; i++) {
	    // call eval
	    // store the resul in a record
	    // return the record.
	    if ( debug > 1) System.err.println("  Considering result "+i);
	    Evaluator evaluator = (Evaluator)eval( prefix+reference, prefix+(String)e.nextElement()+".rdf");
	    if ( evaluator != null ) ok = true;
	    result.add( i, evaluator );
	}
	// Unload the ontologies.
	try {
	    for ( Enumeration e = loaded.elements() ; e.hasMoreElements();  ){
		OWLOntology o = (OWLOntology)e.nextElement();
		o.getOWLConnection().notifyOntologyDeleted( o );
	    }
	} catch (Exception ex) { System.err.println(ex); };
	if ( ok == true ) return result;
	else return (Vector)null;
    }

    public static Evaluator eval( String alignName1, String alignName2 ) {
	Evaluator eval = null;
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
	    eval = new PRecEvaluator( align1, align2 );
	    // Compare
	    params.setParameter( "debug", new Integer( nextdebug ) );
	    eval.eval( params ) ;
	} catch (Exception ex) { System.err.println(ex); }
	return eval;
    }

    /**
     * This does not only print the results but compute the average as well
     */
    public static void print( Vector result ) {
	// variables for computing iterative harmonic means
	int expected = 0; // expected so far
	int foundVect[]; // found so far
	int correctVect[]; // correct so far
	long timeVect[]; // time so far
	double hMeansPrec[]; // Precision H-means so far
	double hMeansRec[]; // Recall H-means so far
	PrintStream writer = null;
	fsize = format.length();
	try {
	    // Print result
	    if ( filename == null ) {
		writer = (PrintStream)System.out;
	    } else {
		writer = new PrintStream(new FileOutputStream( filename ));
	    }
	    // Print the header
	    writer.println("<html><head></head><body>");
	    writer.println("<table border='2' frame='sides' rules='groups'>");
	    writer.println("<colgroup align='center' />");
	    // for each algo <td spancol='2'>name</td>
	    for ( Enumeration e = listAlgo.elements() ; e.hasMoreElements() ;e.nextElement()) {
		writer.println("<colgroup align='center' span='"+fsize+"' />");
	    }
	    // For each file do a
	    writer.println("<thead valign='top'><tr><th>algo</th>");
	    // for each algo <td spancol='2'>name</td>
	    for ( Enumeration e = listAlgo.elements() ; e.hasMoreElements() ;) {
		writer.println("<th colspan='"+fsize+"'>"+(String)e.nextElement()+"</th>");
	    }
	    writer.println("</tr></thead><tbody><tr><td>test</td>");
	    // for each algo <td>Prec.</td><td>Rec.</td>
	    for ( Enumeration e = listAlgo.elements() ; e.hasMoreElements() ;e.nextElement()) {
		for ( int i = 0; i < fsize; i++){
		    writer.print("<td>");
		    if ( format.charAt(i) == 'p' ) {
			writer.print("Prec.");
		    } else if ( format.charAt(i) == 'r' ) {
			writer.print("Rec.");
		    } else if ( format.charAt(i) == 'f' ) {
			writer.print("Fall.");
		    } else if ( format.charAt(i) == 'm' ) {
			writer.print("FMeas.");
		    } else if ( format.charAt(i) == 'o' ) {
			writer.print("Over.");
		    } else if ( format.charAt(i) == 't' ) {
			writer.print("Time");
		    }
		    writer.println("</td>");
		}
		//writer.println("<td>Prec.</td><td>Rec.</td>");
	    }
	    writer.println("</tr></tbody><tbody>");
	    foundVect = new int[ listAlgo.size() ];
	    correctVect = new int[ listAlgo.size() ];
	    timeVect = new long[ listAlgo.size() ];
	    hMeansPrec = new double[ listAlgo.size() ];
	    hMeansRec = new double[ listAlgo.size() ];
	    for( int k = listAlgo.size()-1; k >= 0; k-- ) {
		foundVect[k] = 0;
		correctVect[k] = 0;
		timeVect[k] = 0;
		hMeansPrec[k] = 1.;
		hMeansRec[k] = 1.;
	    }
	    // </tr>
	    // For each directory <tr>
	    boolean colored = false;
	    for ( Enumeration e = result.elements() ; e.hasMoreElements() ;) {
		int nexpected = -1;
		int oexpected = 0;
		Vector test = (Vector)e.nextElement();
		if ( colored == true && color != null ){
		    colored = false;
		    writer.println("<tr bgcolor=\""+color+"\">");
		} else {
		    colored = true;
		    writer.println("<tr>");
		};
		// Print the directory <td>bla</td>
		writer.println("<td>"+(String)test.get(0)+"</td>");
		// For each record print the values <td>bla</td>
		Enumeration f = test.elements();
		f.nextElement();
		for( int k = 0 ; f.hasMoreElements() ; k++) {
		    PRecEvaluator eval = (PRecEvaluator)f.nextElement();
		    if ( eval != null ){
			// iterative H-means computation
			if ( nexpected == -1 ){
			    nexpected = eval.getExpected();
			    oexpected = expected;
			    expected = oexpected + nexpected;
			}
			int nfound = eval.getFound();
			int ofound = foundVect[k];
			foundVect[k] = ofound + nfound;
			int ncorrect = eval.getCorrect();
			int ocorrect = correctVect[k];
			correctVect[k] = ocorrect + ncorrect;
			timeVect[k] += eval.getTime();

			for ( int i = 0 ; i < fsize; i++){
			    writer.print("<td>");
			    if ( format.charAt(i) == 'p' ) {
				printFormat(writer,eval.getPrecision());
			    } else if ( format.charAt(i) == 'r' ) {
				printFormat(writer,eval.getRecall());
			    } else if ( format.charAt(i) == 'f' ) {
				printFormat(writer,eval.getFallout());
			    } else if ( format.charAt(i) == 'm' ) {
				printFormat(writer,eval.getFmeasure());
			    } else if ( format.charAt(i) == 'o' ) {
				printFormat(writer,eval.getOverall());
			    } else if ( format.charAt(i) == 't' ) {
				if ( eval.getTime() == 0 ){
				    writer.print("-");
				} else {
				    printFormat(writer,eval.getTime());
				}
			    }
			    writer.println("</td>");
			}
		    } else {
			writer.println("<td>n/a</td><td>n/a</td>");
		    }
		}
		writer.println("</tr>");
	    }
	    writer.print("<tr bgcolor=\"yellow\"><td>H-mean</td>");
	    int k = 0;
	    for ( Enumeration e = listAlgo.elements() ; e.hasMoreElements() ; k++) {
		e.nextElement();
		double precision = (double)correctVect[k]/foundVect[k];
		double recall = (double)correctVect[k]/expected;
		for ( int i = 0 ; i < fsize; i++){
		    writer.print("<td>");
		    if ( format.charAt(i) == 'p' ) {
			printFormat(writer,precision);
		    } else if ( format.charAt(i) == 'r' ) {
			printFormat(writer,recall);
		    } else if ( format.charAt(i) == 'f' ) {
			printFormat(writer,(double)(foundVect[k] - correctVect[k])/foundVect[k]);
		    } else if ( format.charAt(i) == 'm' ) {
			printFormat(writer,2 * precision * recall / (precision + recall));
		    } else if ( format.charAt(i) == 'o' ) {
			printFormat(writer,recall * (2 - (1 / precision)));
		    } else if ( format.charAt(i) == 't' ) {
			if ( timeVect[k] == 0 ){
			    writer.print("-");
			} else {
			    printFormat(writer,timeVect[k]);
			}
		    }
		    writer.println("</td>");
		};
	    }
	    writer.println("</tr>");
	    writer.println("</tbody></table>");
	    writer.println("</body></html>");
	    writer.close();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    // Borrowed and enhanced from
    // http://acm.sus.mcgill.ca/20020323/work/acm-19/B.j
    // What a pity that it is not in Java... (wait for 1.5)
    public static void printFormat(PrintStream w, double f){
	// JE: Must add the test is the value is Not a number, print NaN.
	if ( f != f ) {
	    w.print("NaN");
	} else {
	    int tmp = (int)(f*100);
	    int dec = tmp%100;
	    if( (int)(f*1000)%10 >= 5 ) dec++;
	    tmp /= 100;
	    w.print("" + tmp + ".");
	    if(dec < 10) w.print("0");
	    w.print("" + dec);
	}
    }

    public static void usage() {
	System.out.println("usage: GroupEval [options]");
	System.out.println("options are:");
	System.out.println("\t--format=prfmot -r prfmot\tSpecifies the output order (precision/recall/fallout/f-measure/overall/time)");
	// Apparently not implemented
	//System.out.println("\t--sup=algo -s algo\tSpecifies if dominant columns are algorithms or measure");
	System.out.println("\t--output=filename -o filename\tSpecifies a file to which the output will go");
	System.out.println("\t--reference=filename -r filename\tSpecifies the name of the reference alignment file (default: refalign.rdf)");

	System.out.println("\t--type=html|xml|tex|ascii -t html|xml|tex|ascii\tSpecifies the output format");
	System.out.println("\t--list=algo1,...,algon -l algo1,...,algon\tSequence of the filenames to consider");
	System.out.println("\t--color=color -c color\tSpecifies if the output must color even lines of the output");
	System.out.println("\t--debug[=n] -d [n]\t\tReport debug info at level n");
	System.out.println("\t--help -h\t\t\tPrint this message");
    }
}

