/*
 * $Id$
 *
 * Copyright (C) INRIA, 2009-2012
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
import org.testng.annotations.Test;

import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.COWLMappingRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.JSONRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.SEKTMappingRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.SKOSRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.SWRLRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.XMLMetadataRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.XSLTRendererVisitor;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class RendererTest {

    private Alignment alignment = null;
    private ObjectAlignment oalignment = null;

    private boolean valueSimilarTo( int obtained, int expected ) {
	if ( (expected-1 <= obtained) && (obtained <= expected+1) ) return true;
	else return false;
    }

    // Read the alignement that will be rendered by everyone
    @BeforeClass(groups = { "full", "impl", "raw" })
    private void init() throws Exception {
	AlignmentParser aparser = new AlignmentParser( 0 );
        assertNotNull( aparser );
        aparser.initAlignment( null );
        alignment = aparser.parse( "file:test/output/bibref2.rdf" );
        assertNotNull( alignment );
	assertEquals( alignment.nbCells(), 32);
	oalignment = ObjectAlignment.toObjectAlignment( (URIAlignment)alignment );
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
	assertTrue( valueSimilarTo( stream.toString().length(), 10417 ), "Rendered differently: expected "+10417+" but was "+stream.toString().length() );
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
	assertTrue( valueSimilarTo( stream.toString().length(), 5553 ), "Rendered differently: expected "+5553+" but was "+stream.toString().length() );
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
	assertTrue( valueSimilarTo( stream.toString().length(), 5498 ), "Rendered differently: expected "+5498+" but was "+stream.toString().length() );
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
	assertTrue( valueSimilarTo( stream.toString().length(), 5407 ), "Rendered differently: expected "+5407+" but was "+stream.toString().length() );
    }

    @Test(groups = { "full", "impl", "raw" })
    public void OWLrenderer() throws Exception {
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new OWLAxiomsRendererVisitor( writer );
	oalignment.render( renderer ); // test error with alignment
	writer.flush();
	writer.close();
	//System.err.println( stream.toString() );
	assertTrue( valueSimilarTo( stream.toString().length(), 5830 ), "Rendered differently: expected "+5830+" but was "+stream.toString().length() );
    }

    @Test(groups = { "full", "impl", "raw" })
    public void SEKTMappingrenderer() throws Exception {
	// not really
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new SEKTMappingRendererVisitor( writer );
	oalignment.render( renderer ); // test error with alignment
	writer.flush();
	writer.close();
	assertTrue( valueSimilarTo( stream.toString().length(), 4820 ), "Rendered differently: expected "+4820+" but was "+stream.toString().length() );
    }

    @Test(groups = { "full", "impl", "raw" })
    public void SWRLrenderer() throws Exception {
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new SWRLRendererVisitor( writer );
	oalignment.render( renderer ); // test error with alignment
	writer.flush();
	writer.close();
	assertTrue( valueSimilarTo( stream.toString().length(), 16141 ), "Rendered differently: expected "+16141+" but was "+stream.toString().length() );
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
	assertTrue( valueSimilarTo( stream.toString().length(), 6133 ), "Rendered differently: expected "+6133+" but was "+stream.toString().length() );
    }

    @Test(groups = { "full", "impl", "raw" })
    public void COWLrenderer() throws Exception {
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new COWLMappingRendererVisitor( writer );
	oalignment.render( renderer ); // test error with alignment
	writer.flush();
	writer.close();
	//System.err.println( stream.toString() );
	assertTrue( valueSimilarTo( stream.toString().length(), 11492 ), "Rendered differently: expected "+11492+" but was "+stream.toString().length() );
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
	assertTrue( valueSimilarTo( stream.toString().length(), 13589 ), "Rendered differently: expected "+13589+" but was "+stream.toString().length() );
    }

    @Test(groups = { "full", "impl", "raw" })
    public void JSONrenderer() throws Exception {
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new JSONRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	//System.err.println( stream.toString() );
	assertTrue( valueSimilarTo( stream.toString().length(), 8414 ), "Rendered differently: expected "+8414+" but was "+stream.toString().length() );
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
