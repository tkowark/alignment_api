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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;
//import org.testng.annotations.*;

import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Parameters;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.onto.OntologyCache;
import fr.inrialpes.exmo.align.parser.AlignmentParser;


public class READMETest {

    private AlignmentProcess alignment = null;

    @Test(groups = { "full", "routine" })
    public void routineTest1() {
	/*
	  $ java -jar lib/procalign.jar --help
	*/
    }

    @Test(groups = { "full", "routine" })
    public void routineTest2() {
	alignment = new StringDistAlignment();
	assertEquals( alignment.nbCells(), 0 );
	assertNotNull( alignment, "ObjectAlignment should not be null" );
	//alignment.init( , );
	//alignment.align( (Alignment)null, );
	assertEquals( alignment.nbCells(), 0 );
	/*
$ java -jar lib/procalign.jar file://$CWD/examples/rdf/onto1.owl file://$CWD/examples/rdf/onto2.owl

	*/
    }

    @Test(groups = { "full", "routine" })
    public void routineTest3() {
	/*
$ java -jar lib/procalign.jar file://$CWD/examples/rdf/onto1.owl file://$CWD/examples/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -r fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor

	*/
    }

    @Test(groups = { "full", "routine" })
    public void routineTest4() {
	/*
$ java -jar lib/procalign.jar file://$CWD/examples/rdf/onto1.owl file://$CWD/examples/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -t 0.4 -o examples/rdf/sample.rdf

	*/
    }

    @Test(groups = { "full", "routine" })
    public void routineTest5() {
	/*
$ java -cp lib/procalign.jar fr.inrialpes.exmo.align.util.ParserPrinter examples/rdf/newsample.rdf

	*/
    }

    @Test(groups = { "full", "routine" })
    public void routineTest6() {
	/*
$ java -jar lib/procalign.jar file://$CWD/examples/rdf/onto1.owl file://$CWD/examples/rdf/onto2.owl -a examples/rdf/sample.rdf

	*/
    }

    @Test(groups = { "full", "routine" })
    public void routineTest7() {
	/*
$ java -jar lib/Procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl

	*/
    }

    @Test(groups = { "full", "routine" })
    public void routineTest8() {
	/*
$ java -jar lib/Procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -o examples/rdf/bibref.rdf

	*/
    }

    @Test(groups = { "full", "routine" })
    public void routineTest9() {
	/*
$ java -jar lib/Procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=subStringDistance -t .4 -o examples/rdf/bibref2.rdf

	*/
    }

    @Test(groups = { "full", "routine" })
    public void routineEvalTest() {
	/*
$ java -cp lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://$CWD/examples/rdf/bibref2.rdf file://$CWD/examples/rdf/bibref.rdf

	*/
    }

    @Test(groups = { "full", "routine" })
    public void routineMatrixTest() {
	/*
$ java -jar lib/Procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -DprintMatrix=1 -o /dev/null > examples/rdf/matrix.tex
*/
    }

    @Test(groups = { "full", "routine" })
    public void routineJWNLTest() {
    /* For JWNL Readme

$ setenv WNDIR ../WordNet-2.0/dict
$ java -jar lib/alignwn.jar -Dwndict=$WNDIR file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.ling.JWNLAlignment -o examples/rdf/JWNL.rdf

    */
    }
}
