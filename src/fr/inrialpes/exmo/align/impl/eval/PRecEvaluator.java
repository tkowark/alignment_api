/*
 * $Id$
 *
 * Copyright (C) INRIA, 2004-2008
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.impl.eval;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.BasicEvaluator;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.Annotations;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.io.PrintWriter;
import java.net.URI;


/**
 * Evaluate proximity between two alignments.
 * This function implements Precision/Recall/Fallout. The first alignment
 * is thus the expected one.
 *
 * @author Jerome Euzenat
 * @version $Id$ 
 */

public class PRecEvaluator extends BasicEvaluator implements Evaluator {

    private double precision = 0.;

    private double recall = 0.;

    private double fallout = 0.;

    private double overall = 0.;

    private double fmeasure = 0.;

    private long time = 0;

    private int nbexpected = 0;

    private int nbfound = 0;

    private int nbcorrect = 0; // nb of cells correctly identified

    /** Creation **/
    public PRecEvaluator(Alignment align1, Alignment align2) throws AlignmentException {
	super(((BasicAlignment)align1).toURIAlignment(), ((BasicAlignment)align2).toURIAlignment());
    }

    public void init(){
	precision = 0.;
	recall = 0.;
	fallout = 0.;
	overall = 0.;
	fmeasure = 0.;
	time = 0;
	nbexpected = 0;
	nbfound = 0;
	nbcorrect = 0;
    }

    /**
     *
     * The formulas are standard:
     * given a reference alignment A
     * given an obtained alignment B
     * which are sets of cells (linking one entity of ontology O to another of ontolohy O').
     *
     * P = |A inter B| / |B|
     * R = |A inter B| / |A|
     * F = 2PR/(P+R)
     * with inter = set intersection and |.| cardinal.
     *
     * In the implementation |B|=nbfound, |A|=nbexpected and |A inter B|=nbcorrect.
     */
    public double eval(Parameters params) throws AlignmentException {
	return eval( params, (Object)null );
    }
    public double eval(Parameters params, Object cache) throws AlignmentException {
	init();
	nbexpected = 0;
	nbfound = align2.nbCells();
	precision = 0.;
	recall = 0.;

	for ( Enumeration e = align1.getElements(); e.hasMoreElements(); nbexpected++) {
	    Cell c1 = (Cell)e.nextElement();
	    URI uri1 = c1.getObject2AsURI();
	    Set s2 = (Set)align2.getAlignCells1( c1.getObject1() );
	    if( s2 != null ){
		for( Iterator it2 = s2.iterator(); it2.hasNext() && c1 != null; ){
		    Cell c2 = (Cell)it2.next();
		    URI uri2 = c2.getObject2AsURI();	
		    // if (c1.getobject2 == c2.getobject2)
		    if (uri1.toString().equals(uri2.toString())) {
			nbcorrect++;
			c1 = null; // out of the loop.
		    }
		}
	    }
	}

	// What is the definition if:
	// nbfound is 0 (p, r are 0)
	// nbexpected is 0 [=> nbcorrect is 0] (r=NaN, p=0[if nbfound>0, NaN otherwise])
	// precision+recall is 0 [= nbcorrect is 0]
	// precision is 0 [= nbcorrect is 0]
	precision = (double) nbcorrect / (double) nbfound;
	recall = (double) nbcorrect / (double) nbexpected;
	fallout = (double) (nbfound - nbcorrect) / (double) nbfound;
	fmeasure = 2 * precision * recall / (precision + recall);
	overall = recall * (2 - (1 / precision));
	result = recall / precision;
	String timeExt = align2.getExtension( Annotations.ALIGNNS, Annotations.TIME );
	if ( timeExt != null ) time = Long.parseLong(timeExt);
	//System.err.println(">>>> " + nbcorrect + " : " + nbfound + " : " + nbexpected);
	return (result);
    }

    public String HTMLString (){
	String result = "";
	result += "  <div  xmlns:map='http://www.atl.external.lmco.com/projects/ontology/ResultsOntology.n3#' typeof=\"map:output\" href=''>";
	result += "    <dl>";
	//if ( ) {
	//    result += "    <dt>algorithm</dt><dd property=\"map:algorithm\">"+align1.get+"</dd>";
	//}
	try {
	    result += "    <dt>input1</dt><dd rel=\"map:input1\" href=\""+align1.getOntology1URI()+"\">"+align1.getOntology1URI()+"</dd>";
	    result += "    <dt>input2</dt><dd rel=\"map:input2\" href=\""+align1.getOntology2URI()+"\">"+align1.getOntology2URI()+"</dd>";
	} catch (AlignmentException e) { e.printStackTrace(); };
	// Other missing items (easy to get)
	// result += "    <map:falseNegative>");
	// result += "    <map:falsePositive>");
	result += "    <dt>precision</dt><dd property=\"map:precision\">"+precision+"</dd>\n";
	result += "    <dt>recall</dt><dd property=\"map:recall\">"+recall+"</dd>\n";
	result += "    <dt>fallout</dt><dd property=\"map:fallout\">"+fallout+"</dd>\n";
	result += "    <dt>F-measure</dt><dd property=\"map:fMeasure\">"+fmeasure+"</dd>\n";
	result += "    <dt>O-measure</dt><dd property=\"map:oMeasure\">"+overall+"</dd>\n";
	if ( time != 0 ) result += "    <dt>time</dt><dd property=\"map:time\">"+time+"</dd>\n";
    	result += "    <dt>result</dt><dd property=\"map:result\">"+result+"</dd>\n";
	result += "  </dl>\n  </div>\n";
return result;
    }

    /**
     * This now output the results in Lockheed format.
     */
    public void write(PrintWriter writer) throws java.io.IOException {
	writer.println("<?xml version='1.0' encoding='utf-8' standalone='yes'?>");
	writer.println("<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n  xmlns:map='http://www.atl.external.lmco.com/projects/ontology/ResultsOntology.n3#'>");
	writer.println("  <map:output rdf:about=''>");
	//if ( ) {
	//    writer.println("    <map:algorithm rdf:resource=\"http://co4.inrialpes.fr/align/algo/"+align1.get+"\">");
	//}
	try {
	    writer.println("    <map:input1 rdf:resource=\""+align1.getOntology1URI()+"\"/>");
	    writer.println("    <map:input2 rdf:resource=\""+align1.getOntology2URI()+"\"/>");
	} catch (AlignmentException e) { e.printStackTrace(); };
	// Other missing items (easy to get)
	// writer.println("    <map:falseNegative>");
	// writer.println("    <map:falsePositive>");
	writer.print("    <map:precision>");
	writer.print(precision);
	writer.print("</map:precision>\n    <map:recall>");
	writer.print(recall);
	writer.print("</map:recall>\n    <fallout>");
	writer.print(fallout);
	writer.print("</fallout>\n    <map:fMeasure>");
	writer.print(fmeasure);
	writer.print("</map:fMeasure>\n    <map:oMeasure>");
	writer.print(overall);
	writer.print("</map:oMeasure>\n");
	if ( time != 0 ) writer.print("    <time>"+time+"</time>\n");
    	writer.print("    <result>"+result);
	writer.print("</result>\n  </map:output>\n</rdf:RDF>\n");
    }

    public double getPrecision() { return precision; }
    public double getRecall() {	return recall; }
    public double getOverall() { return overall; }
    public double getFallout() { return fallout; }
    public double getFmeasure() { return fmeasure; }
    public int getExpected() { return nbexpected; }
    public int getFound() { return nbfound; }
    public int getCorrect() { return nbcorrect; }
    public long getTime() { return time; }
}

