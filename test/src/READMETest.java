/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2008
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

//package test.com.acme.dona.dep;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;
//import org.testng.annotations.*;

import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.onto.OntologyCache;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class READMETest {

    private AlignmentProcess alignment = null;

    @Test(groups = { "full", "impl" })
    public void routineTest1() {
	/*
$ java -jar lib/procalign.jar --help
	*/
    }

    @Test(groups = { "full", "impl" })
    public void routineTest2() throws Exception {
	/*
$ java -jar lib/procalign.jar file://$CWD/examples/rdf/onto1.owl file://$CWD/examples/rdf/onto2.owl
	*/
	Parameters params = new BasicParameters();
	alignment = new StringDistAlignment();
	assertNotNull( alignment, "ObjectAlignment should not be null" );
	assertEquals( alignment.nbCells(), 0 );
	alignment.init( new URI("file:examples/rdf/onto1.owl"), new URI("file:examples/rdf/onto2.owl"));
	alignment.align( (Alignment)null, params );
	assertEquals( alignment.nbCells(), 1 );
    }

    @Test(groups = { "full", "impl" })
    public void routineTest3() throws Exception {
	/*
$ java -jar lib/procalign.jar file://$CWD/examples/rdf/onto1.owl file://$CWD/examples/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -r fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor
	*/
	Parameters params = new BasicParameters();
	params.setParameter( "stringFunction", "levenshteinDistance");
	alignment = new StringDistAlignment();
	assertNotNull( alignment, "ObjectAlignment should not be null" );
	assertEquals( alignment.nbCells(), 0 );
	alignment.init( new URI("file:examples/rdf/onto1.owl"), new URI("file:examples/rdf/onto2.owl"));
	alignment.align( (Alignment)null, params );
	assertEquals( alignment.nbCells(), 3 );
	// Rendering
	// Playing with threshold
	/*
$ java -jar lib/procalign.jar file://$CWD/examples/rdf/onto1.owl file://$CWD/examples/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -t 0.4 -o examples/rdf/sample.rdf
	*/
	alignment.cut( "hard", 0.4 );
	assertEquals( alignment.nbCells(), 2 );
    }

    @Test(groups = { "full", "impl" })
    public void routineTest5() throws Exception {
	/*
$ java -cp lib/procalign.jar fr.inrialpes.exmo.align.util.ParserPrinter examples/rdf/newsample.rdf
	*/
	AlignmentParser aparser = new AlignmentParser( 0 );
	assertNotNull( aparser, "AlignmentParser was null" );
	Alignment result = aparser.parse( "file:examples/rdf/newsample.rdf" );
	assertNotNull( result, "URIAlignment(result) was null" );
	assertTrue( result instanceof URIAlignment );
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	//FileOutputStream stream = new FileOutputStream("result.rdf");
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new RDFRendererVisitor( writer );
	result.render( renderer );
	writer.flush();
	writer.close();
	assertEquals( stream.toString().length(), 1740, "Rendered differently" );
    }

    @Test(groups = { "full", "impl" }, dependsOnMethods = {"routineTest3"})
    public void routineTest6() throws Exception {
	/*
$ java -jar lib/procalign.jar file://$CWD/examples/rdf/onto1.owl file://$CWD/examples/rdf/onto2.owl -a examples/rdf/sample.rdf

	*/
    }

    @Test(groups = { "full", "impl" }, dependsOnMethods = {"routineTest3"})
    public void routineTest7() throws Exception {
	/*
$ java -jar lib/Procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl
	*/
	Parameters params = new BasicParameters();
	alignment = new StringDistAlignment();
	assertNotNull( alignment, "ObjectAlignment should not be null" );
	assertEquals( alignment.nbCells(), 0 );
	alignment.init( new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl"), new URI("file:examples/rdf/edu.mit.visus.bibtex.owl"));
	alignment.align( (Alignment)null, params );
	assertEquals( alignment.nbCells(), 10 );
    }

    @Test(groups = { "full", "impl" }, dependsOnMethods = {"routineTest7"})
    public void routineTest8() throws Exception {
	/*
$ java -jar lib/Procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -o examples/rdf/bibref.rdf
	*/
	Parameters params = new BasicParameters();
	params.setParameter( "stringFunction", "levenshteinDistance");
	alignment = new StringDistAlignment();
	assertNotNull( alignment, "ObjectAlignment should not be null" );
	assertEquals( alignment.nbCells(), 0 );
	alignment.init( new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl"), new URI("file:examples/rdf/edu.mit.visus.bibtex.owl"));
	alignment.align( (Alignment)null, params );
	assertEquals( alignment.nbCells(), 43 );
	FileOutputStream stream = new FileOutputStream("examples/rdf/bibref.rdf");
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new RDFRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	//assertEquals( stream.toString().length(), 1740, "Rendered differently" );
	/*
$ java -jar lib/Procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=subStringDistance -t .4 -o examples/rdf/bibref2.rdf
	*/
	alignment.cut( "hard", 0.55 );
	assertEquals( alignment.nbCells(), 32 ); /* With  .4, I have either 36 or 35! */
	stream = new FileOutputStream("examples/rdf/bibref2.rdf");
	writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	alignment.render( new RDFRendererVisitor( writer ) );
	writer.flush();
	writer.close();
	//assertEquals( stream.toString().length(), 1740, "Rendered differently" );
    }

    @Test(groups = { "full", "impl" }, dependsOnMethods = {"routineTest8"})
    public void routineEvalTest() throws Exception {
	/*
$ java -cp lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://$CWD/examples/rdf/bibref2.rdf file://$CWD/examples/rdf/bibref.rdf
	*/
	AlignmentParser aparser1 = new AlignmentParser( 0 );
	assertNotNull( aparser1 );
	Alignment align1 = aparser1.parse( "examples/rdf/bibref2.rdf" );
	assertNotNull( align1 );
	    //AlignmentParser aparser2 = new AlignmentParser( debug );
	Alignment align2 = aparser1.parse( "examples/rdf/bibref.rdf" );
	assertNotNull( align2 );
	Parameters params = new BasicParameters();
	assertNotNull( params );
	Evaluator eval = new PRecEvaluator( align1, align2 );
	assertNotNull( eval );
	eval.eval( params ) ;
	    /*
		stream = new FileOutputStream(filename);
	    }
	    writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	    eval.write( writer );
	    writer.flush();
	    */
	/*
<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
  xmlns:map='http://www.atl.external.lmco.com/projects/ontology/ResultsOntology.n3#'>
  <map:output rdf:about=''>
    <map:input1 rdf:resource="http://ebiquity.umbc.edu/v2.1/ontology/publication.owl#publication"/>
    <map:input2 rdf:resource="file:examples/rdf/edu.mit.visus.bibtex.owl"/>
    <map:precision>0.8372093023255814</map:precision>
    <map:recall>1.0</map:recall>
    <fallout>0.16279069767441862</fallout>
    <map:fMeasure>0.9113924050632911</map:fMeasure>
    <map:oMeasure>0.8055555555555556</map:oMeasure>
    <result>1.1944444444444444</result>
  </map:output>
</rdf:RDF>
	*/
    }

    @Test(groups = { "full", "impl" })
    public void routineMatrixTest() throws Exception {
	/*
$ java -jar lib/Procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -DprintMatrix=1 -o /dev/null > examples/rdf/matrix.tex
	*/
    }

    @Test(groups = { "full", "impl" }, dependsOnMethods = {"routineTest3"})
    public void routineJWNLTest() throws Exception {
    /*
$ setenv WNDIR ../WordNet-2.0/dict
$ java -jar lib/alignwn.jar -Dwndict=$WNDIR file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.ling.JWNLAlignment -o examples/rdf/JWNL.rdf
    */
    }
}
