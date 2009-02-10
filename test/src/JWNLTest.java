/*
 * $Id$
 *
 * Copyright (C) INRIA, 2008-2009
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.ling.JWNLAlignment;
import fr.inrialpes.exmo.align.ling.JWNLDistances;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

/**
 * These tests corresponds to the JWNL test of the README file in the main directory
 */

public class JWNLTest {

    private AlignmentProcess alignment = null;

    @Test(groups = { "full", "ling" })
    public void routineJWNLAlignmentTest() throws Exception {
    /*
$ setenv WNDIR 
$ java -jar lib/alignwn.jar -D=$WNDIR file://$CWD/examples/rdf/ file://$CWD/examples/rdf/ -i fr.inrialpes.exmo.align.ling.JWNLAlignment -o examples/rdf/JWNL.rdf
    */
	Parameters params = new BasicParameters();
	//System.getenv("WNDIR");
	//params.setParameter( "wndict", "../WordNet-2.0/dict" );
	params.setParameter( "wndict", "../WordNet-3.0/dict" );
	alignment = new JWNLAlignment();
	assertNotNull( alignment, "ObjectAlignment should not be null" );
	assertEquals( alignment.nbCells(), 0 );
	alignment.init( new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl"), new URI("file:examples/rdf/edu.mit.visus.bibtex.owl"));
	alignment.align( (Alignment)null, params );
	assertEquals( alignment.nbCells(), 43 );
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new RDFRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	assertEquals( stream.toString().length(), 14027, "Rendered differently" );
	alignment.cut( "hard", 0.4 );
	assertEquals( alignment.nbCells(), 37 );

	// Different similarity
	params.setParameter( "wnfunction", "cosynonymySimilarity" );
	alignment = new JWNLAlignment();
	alignment.init( new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl"), new URI("file:examples/rdf/edu.mit.visus.bibtex.owl"));
	alignment.align( (Alignment)null, params );
	assertEquals( alignment.nbCells(), 9 );
	alignment.cut( "hard", 0.4 );
	assertEquals( alignment.nbCells(), 9 );

    }

    // This was in the class "main"
    @Test(groups = { "full", "ling" })
    public void routineJWNLDistanceTest() throws Exception {
        JWNLDistances j = new JWNLDistances();
	j.Initialize( "../WordNet-3.0/dict", "3.0" );

	assertEquals( j.compareComponentNames( "French997Guy", "Dutch_Goa77ly" ), 0.23684210526315788);
	assertEquals( j.compareComponentNames( "FREnch997guy21GUIe", "Dutch_GOa77ly." ), 0.09795918367346938);
	assertEquals( j.compareComponentNames( "a997c", "77ly."), .0);
	assertEquals( j.compareComponentNames( "MSc", "PhD"), 0.2777777777777778);
    }
}
