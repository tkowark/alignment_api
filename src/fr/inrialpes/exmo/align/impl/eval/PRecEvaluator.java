/*
 * $Id$
 *
 * Copyright (C) INRIA, 2004-2009, 2010
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
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.parser.SyntaxElement;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.BasicEvaluator;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.Annotations;

import java.util.Enumeration;
import java.util.Properties;
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

    protected double precision = 0.;

    protected double recall = 0.;

    protected double fallout = 0.;

    protected double overall = 0.;

    protected double fmeasure = 0.;

    protected long time = 0;

    protected int nbexpected = 0;

    protected int nbfound = 0;

    protected int nbcorrect = 0; // nb of cells correctly identified

    /** Creation
     * Initiate Evaluator for precision and recall
     * @param align1 : the reference alignment
     * @param align2 : the alignment to evaluate
     **/
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
	result = 1.;
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
    public double eval( Properties params ) throws AlignmentException {
	return eval( params, (Object)null );
    }
    public double eval( Properties params, Object cache ) throws AlignmentException {
	init();
	nbfound = align2.nbCells();

	for ( Cell c1 : align1 ) {
	    URI uri1 = c1.getObject2AsURI();
	    nbexpected++;
	    Set<Cell> s2 = align2.getAlignCells1( c1.getObject1() );
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
	return computeDerived();
    }

    protected double computeDerived() {
	fallout = (double) (nbfound - nbcorrect) / (double) nbfound;
	fmeasure = 2 * precision * recall / (precision + recall);
	overall = recall * (2 - (1 / precision));
	result = recall / precision;
	String timeExt = align2.getExtension( Namespace.ALIGNMENT.uri, Annotations.TIME );
	if ( timeExt != null ) time = Long.parseLong(timeExt);
	//System.err.println(">>>> " + nbcorrect + " : " + nbfound + " : " + nbexpected);
	return (result);
    }

    public String HTMLString (){
	String result = "";
	result += "  <div  xmlns:"+Namespace.ATLMAP.shortCut+"='"+Namespace.ATLMAP.prefix+"' typeof=\""+Namespace.ATLMAP.shortCut+":output\" href=''>";
	result += "    <dl>";
	//if ( ) {
	//    result += "    <dt>algorithm</dt><dd property=\""+Namespace.ATLMAP.shortCut+":algorithm\">"+align1.get+"</dd>";
	//}
	try {
	    result += "    <dt>input1</dt><dd rel=\""+Namespace.ATLMAP.shortCut+":input1\" href=\""+align1.getOntology1URI()+"\">"+align1.getOntology1URI()+"</dd>";
	    result += "    <dt>input2</dt><dd rel=\""+Namespace.ATLMAP.shortCut+":input2\" href=\""+align1.getOntology2URI()+"\">"+align1.getOntology2URI()+"</dd>";
	} catch (AlignmentException e) { e.printStackTrace(); };
	// Other missing items (easy to get)
	// result += "    <"+Namespace.ATLMAP.shortCut+":falseNegative>");
	// result += "    <"+Namespace.ATLMAP.shortCut+":falsePositive>");
	result += "    <dt>precision</dt><dd property=\""+Namespace.ATLMAP.shortCut+":precision\">"+precision+"</dd>\n";
	result += "    <dt>recall</dt><dd property=\""+Namespace.ATLMAP.shortCut+":recall\">"+recall+"</dd>\n";
	result += "    <dt>fallout</dt><dd property=\""+Namespace.ATLMAP.shortCut+":fallout\">"+fallout+"</dd>\n";
	result += "    <dt>F-measure</dt><dd property=\""+Namespace.ATLMAP.shortCut+":fMeasure\">"+fmeasure+"</dd>\n";
	result += "    <dt>O-measure</dt><dd property=\""+Namespace.ATLMAP.shortCut+":oMeasure\">"+overall+"</dd>\n";
	if ( time != 0 ) result += "    <dt>time</dt><dd property=\""+Namespace.ATLMAP.shortCut+":time\">"+time+"</dd>\n";
    	result += "    <dt>result</dt><dd property=\""+Namespace.ATLMAP.shortCut+":result\">"+result+"</dd>\n";
	result += "  </dl>\n  </div>\n";
return result;
    }

    /**
     * This now output the results in Lockheed format.
     */
    public void write(PrintWriter writer) throws java.io.IOException {
	writer.println("<?xml version='1.0' encoding='utf-8' standalone='yes'?>");
	writer.println("<"+SyntaxElement.RDF.print()+" xmlns:"+Namespace.RDF.shortCut+"='"+Namespace.RDF.prefix+"'\n  xmlns:"+Namespace.ATLMAP.shortCut+"='"+Namespace.ATLMAP.prefix+"'>");
	writer.println("  <"+Namespace.ATLMAP.shortCut+":output "+SyntaxElement.RDF_ABOUT.print()+"=''>");
	//if ( ) {
	//    writer.println("    <"+Namespace.ATLMAP.shortCut+":algorithm "+SyntaxElement.RDF_RESOURCE.print()+"=\"http://co4.inrialpes.fr/align/algo/"+align1.get+"\">");
	//}
	try {
	    writer.println("    <"+Namespace.ATLMAP.shortCut+":input1 "+SyntaxElement.RDF_RESOURCE.print()+"=\""+align1.getOntology1URI()+"\"/>");
	    writer.println("    <"+Namespace.ATLMAP.shortCut+":input2 "+SyntaxElement.RDF_RESOURCE.print()+"=\""+align1.getOntology2URI()+"\"/>");
	} catch (AlignmentException e) { e.printStackTrace(); };
	// Other missing items (easy to get)
	// writer.println("    <"+Namespace.ATLMAP.shortCut+":falseNegative>");
	// writer.println("    <"+Namespace.ATLMAP.shortCut+":falsePositive>");
	writer.print("    <"+Namespace.ATLMAP.shortCut+":precision>");
	writer.print(precision);
	writer.print("</"+Namespace.ATLMAP.shortCut+":precision>\n    <"+Namespace.ATLMAP.shortCut+":recall>");
	writer.print(recall);
	writer.print("</"+Namespace.ATLMAP.shortCut+":recall>\n    <fallout>");
	writer.print(fallout);
	writer.print("</fallout>\n    <"+Namespace.ATLMAP.shortCut+":fMeasure>");
	writer.print(fmeasure);
	writer.print("</"+Namespace.ATLMAP.shortCut+":fMeasure>\n    <"+Namespace.ATLMAP.shortCut+":oMeasure>");
	writer.print(overall);
	writer.print("</"+Namespace.ATLMAP.shortCut+":oMeasure>\n");
	if ( time != 0 ) writer.print("    <time>"+time+"</time>\n");
    	writer.print("    <result>"+result);
	writer.print("</result>\n  </"+Namespace.ATLMAP.shortCut+":output>\n</"+SyntaxElement.RDF.print()+">\n");
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

