/*
 * $Id$
 *
 * Copyright (C) INRIA, 2009-2010
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
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.COWLMappingRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.SEKTMappingRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.SKOSRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.SWRLRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.XMLMetadataRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.XSLTRendererVisitor;

import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * These tests corresponds to the README file in the main directory
 */

public class RendererTest {

    private AlignmentProcess alignment = null;

    private boolean valueSimilarTo( int obtained, int expected ) {
	if ( (expected-1 <= obtained) && (obtained <= expected+1) ) return true;
	else return false;
    }

    // Create the Alignement that will be rendered by everyone
    @BeforeClass(groups = { "full", "impl", "raw" })
    private void init() throws Exception {
	Properties params = new Properties();
	params.setProperty( "stringFunction", "levenshteinDistance");
	alignment = new StringDistAlignment();
	assertNotNull( alignment, "ObjectAlignment should not be null" );
	assertEquals( alignment.nbCells(), 0 );
	alignment.init( new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl"), new URI("file:examples/rdf/edu.mit.visus.bibtex.owl"));
	alignment.align( (Alignment)null, params );
	assertEquals( alignment.nbCells(), 44 );
	// Suppress the time label 
    }

    @Test(groups = { "full", "impl", "raw" })
    public void RDFrenderer() throws Exception {
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new RDFRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	//System.err.println( stream.toString() );
	assertTrue( valueSimilarTo( stream.toString().length(), 14285 ), "Rendered differently: expected "+14285+" but was "+stream.toString().length() );
	Properties params = new Properties();
	params.setProperty( "embedded", "1");
    }

    @Test(groups = { "full", "impl", "raw" })
    public void SKOSrenderer() throws Exception {
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new SKOSRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	assertTrue( valueSimilarTo( stream.toString().length(), 7479 ), "Rendered differently: expected "+7479+" but was "+stream.toString().length() );
	Properties params = new Properties();
	params.setProperty( "embedded", "1");
	stream = new ByteArrayOutputStream();
	writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	renderer = new SKOSRendererVisitor( writer );
	renderer.init( params );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	assertTrue( valueSimilarTo( stream.toString().length(), 7424 ), "Rendered differently: expected "+7424+" but was "+stream.toString().length() );
	params.setProperty( "pre2008", "1");
	stream = new ByteArrayOutputStream(); 
	writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	renderer = new SKOSRendererVisitor( writer );
	renderer.init( params );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	assertTrue( valueSimilarTo( stream.toString().length(), 7297 ), "Rendered differently: expected "+7297+" but was "+stream.toString().length() );
    }

    @Test(groups = { "full", "impl", "raw" })
    public void OWLrenderer() throws Exception {
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new OWLAxiomsRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	assertTrue( valueSimilarTo( stream.toString().length(), 7667 ), "Rendered differently: expected "+7667+" but was "+stream.toString().length() );
    }

    @Test(groups = { "full", "impl", "raw" })
    public void SEKTMappingrenderer() throws Exception {
	// not really
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new SEKTMappingRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	assertTrue( valueSimilarTo( stream.toString().length(), 6552 ), "Rendered differently: expected "+6552+" but was "+stream.toString().length() );
    }

    @Test(groups = { "full", "impl", "raw" })
    public void SWRLrenderer() throws Exception {
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new SWRLRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	assertTrue( valueSimilarTo( stream.toString().length(), 21700 ), "Rendered differently: expected "+21700+" but was "+stream.toString().length() );
    }

    @Test(groups = { "full", "impl", "raw" })
    public void XSLTrenderer() throws Exception {
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new XSLTRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	assertTrue( valueSimilarTo( stream.toString().length(), 8204 ), "Rendered differently: expected "+8204+" but was "+stream.toString().length() );
    }

    @Test(groups = { "full", "impl", "raw" })
    public void COWLrenderer() throws Exception {
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new COWLMappingRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	assertTrue( valueSimilarTo( stream.toString().length(), 15395 ), "Rendered differently: expected "+15395+" but was "+stream.toString().length() );
    }

    @Test(groups = { "full", "impl", "raw" })
    public void HTMLrenderer() throws Exception {
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new HTMLRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	//System.err.println( stream.toString() );
	assertTrue( valueSimilarTo( stream.toString().length(), 18394 ), "Rendered differently: expected "+18394+" but was "+stream.toString().length() );
    }

    @Test(groups = { "full", "impl", "raw" })
    public void XMLMetadatarenderer() throws Exception {
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			  new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new XMLMetadataRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	assertTrue( valueSimilarTo( stream.toString().length(), 770 ), "Rendered differently: expected "+770+" but was "+stream.toString().length() );
    }


}
