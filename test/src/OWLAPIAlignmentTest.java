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

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLEntity;

import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.OWLAPIAlignment;
import fr.inrialpes.exmo.align.impl.OWLAPICell;
import fr.inrialpes.exmo.align.impl.rel.EquivRelation;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.onto.OntologyCache;
import fr.inrialpes.exmo.align.onto.LoadedOntology;
import fr.inrialpes.exmo.align.onto.Ontology;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Set;

public class OWLAPIAlignmentTest {

    private String localURIPrefix = "file://" + System.getProperty("user.dir");

    private OWLAPIAlignment alignment = null;
    private Ontology onto1 = null;
    private Ontology onto2 = null;

    @BeforeClass(groups = { "fast" })
    private void init(){
	alignment = new OWLAPIAlignment();
    }

    @Test(groups = { "fast" })
    public void aFastTest() {
	assertNotNull( alignment, "Alignment was null" );
    }

    @Test(groups = { "full" })
    public void loadingAndConvertingTest() throws Exception {
	assertNotNull( alignment, "Alignment was null" );
	AlignmentParser aparser = new AlignmentParser( 0 );
	assertNotNull( aparser, "AlignmentParser was null" );
	URIAlignment result = (URIAlignment)aparser.parse( "file:examples/rdf/newsample.rdf" );
	assertNotNull( result, "URIAlignment(result) was null" );
	alignment = OWLAPIAlignment.toOWLAPIAlignment( result, (OntologyCache)null );
	assertNotNull( alignment, "toOWLAPIAlignment(result) was null" );
	result = alignment.toURIAlignment();
	assertNotNull( result, "toURIAlignment() was null" );
	assertTrue( result instanceof URIAlignment );
    }

    @Test(groups = { "full" }, dependsOnMethods = {"loadingAndConvertingTest"})
    public void basicAttributeTest() throws Exception {
	assertEquals( alignment.getLevel(), "0" );
	assertEquals( alignment.getType(), "**" );
	assertEquals( alignment.getExtension( Annotations.ALIGNNS, Annotations.METHOD), "fr.inrialpes.exmo.align.impl.method.StringDistAlignment" );
	assertEquals( alignment.getExtension( Annotations.ALIGNNS, Annotations.TIME), "7" );
    }

    @Test(groups = { "full" }, dependsOnMethods = {"loadingAndConvertingTest"})
    public void ontologyTest() throws Exception {
	onto1 = alignment.getOntologyObject1();
	onto2 = alignment.getOntologyObject2();
	assertTrue( onto1 instanceof LoadedOntology );
	assertTrue( onto2 instanceof LoadedOntology );
	assertTrue( alignment.getOntology1() instanceof OWLOntology );
	assertTrue( alignment.getOntology2() instanceof OWLOntology );
	assertEquals( alignment.getOntology1URI().toString(), "http://www.example.org/ontology1" );
	assertEquals( alignment.getOntology2URI().toString(), "http://www.example.org/ontology2" );
	assertEquals( onto1.getURI().toString(), "http://www.example.org/ontology1" );
	assertEquals( onto2.getURI().toString(), "http://www.example.org/ontology2" );
	assertEquals( onto1.getFile().toString(), localURIPrefix+"/examples/rdf/onto1.owl" );
	assertEquals( onto2.getFile().toString(), localURIPrefix+"/examples/rdf/onto2.owl" );
	assertEquals( onto1.getFormalism(), "OWL1.0" );
	assertEquals( onto2.getFormalism(), "OWL1.0" );
	assertEquals( onto1.getFormURI().toString(), "http://www.w3.org/2002/07/owl#" );
	assertEquals( onto2.getFormURI().toString(), "http://www.w3.org/2002/07/owl#" );
    }

    @Test(groups = { "full" }, dependsOnMethods = {"ontologyTest"})
    public void basicCellTest() throws Exception {
	assertEquals( alignment.nbCells(), 2 );
	Object ob2 = ((LoadedOntology)onto2).getEntity( new URI("http://www.example.org/ontology2#journalarticle") );
	assertTrue( ob2 instanceof OWLEntity );
	Set<Cell> s2 = alignment.getAlignCells2( ob2 );
	assertEquals( s2.size(), 2 );
	for( Cell c2 : s2 ){
	    assertTrue( c2 instanceof OWLAPICell );
	    assertTrue( c2.getRelation() instanceof EquivRelation );
	    assertTrue( c2.getObject1() instanceof OWLEntity );
	}
	Object ob1 = ((LoadedOntology)onto1).getEntity( new URI("http://www.example.org/ontology1#journalarticle") );
	assertTrue( ob1 instanceof OWLEntity );
	Set<Cell> s1 = alignment.getAlignCells1( ob1 );
	assertEquals( s1.size(), 1 );
	for( Cell c1 : s1 ){
	    assertTrue( c1 instanceof OWLAPICell );
	    assertTrue( c1.getRelation() instanceof EquivRelation );
	    assertEquals( c1.getStrength(), 1. );
	    assertTrue( c1.getObject2() instanceof OWLEntity );
	    assertEquals( ((LoadedOntology)onto2).getEntityURI( c1.getObject2() ).toString(), "http://www.example.org/ontology2#journalarticle" );
	}
	ob1 = ((LoadedOntology)onto1).getEntity( new URI("http://www.example.org/ontology1#reviewedarticle") );
	assertTrue( ob1 instanceof OWLEntity );
	s1 = alignment.getAlignCells1( ob1 );
	assertEquals( s1.size(), 1 );
	for( Cell c1 : s1 ){
	    assertTrue( c1 instanceof OWLAPICell );
	    assertTrue( c1.getRelation() instanceof EquivRelation );
	    assertEquals( c1.getStrength(), .4666666666666667 );
	    assertTrue( c1.getObject2() instanceof OWLEntity );
	    assertEquals( ((LoadedOntology)onto2).getEntityURI( c1.getObject2() ).toString(), "http://www.example.org/ontology2#journalarticle" );
	}
    }

    @Test(groups = { "full" }, dependsOnMethods = {"loadingAndConvertingTest"})
    public void rendererTest() throws Exception {
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	//FileOutputStream stream = new FileOutputStream("result.rdf");
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);

	AlignmentVisitor renderer = new RDFRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	assertEquals( stream.toString().length(), 1740, "Rendered differently" );
    }

}
