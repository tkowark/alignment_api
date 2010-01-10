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

//package test.com.acme.dona.dep;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;
//import org.testng.annotations.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

// JE: this one is too much
import org.omwg.mediation.parser.rdf.RDFParserException;

import fr.inrialpes.exmo.align.impl.BasicOntologyNetwork;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.OntologyNetwork;
import fr.inrialpes.exmo.align.onto.Ontology;
import fr.inrialpes.exmo.align.onto.OntologyFactory;
import fr.inrialpes.exmo.align.util.OntologyNetworkWeakener;

public class OntologyNetworkTest {
    private OntologyNetwork noo = null;

    @BeforeClass(groups = { "full", "raw" })
    private void init(){
	noo = new BasicOntologyNetwork();
    }

    @Test(groups = { "full", "raw" })
    public void aFastTest() {
	assertNotNull( noo, "Alignment was null" );
    }

    @Test(groups = { "full", "raw" }, dependsOnMethods = {"aFastTest"})
	public void ontologyTest() throws URISyntaxException, AlignmentException {
	assertEquals( noo.getOntologies().size(), 0 );
	// Load
	URI u1 = new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl");
	URI u2 = new URI("file:examples/rdf/edu.mit.visus.bibtex.owl");
	// addOntology
	noo.addOntology( u1 );
	assertEquals( noo.getOntologies().size(), 1 );
	// addOntology
	noo.addOntology( u2 );
	assertEquals( noo.getOntologies().size(), 2 );
	noo.addOntology( u2 );
	assertEquals( noo.getOntologies().size(), 2 );
	// remOntology
	noo.remOntology( u1 );
	assertEquals( noo.getOntologies().size(), 1 );
	noo.addOntology( u1 );
	assertEquals( noo.getOntologies().size(), 2);
    }

    @Test(groups = { "full", "raw" }, dependsOnMethods = {"ontologyTest"})
	public void alignmentTest() throws ParserConfigurationException, SAXException, IOException, URISyntaxException, RDFParserException, AlignmentException {
	assertEquals( noo.getAlignments().size(), 0 );
	assertEquals( noo.getOntologies().size(), 2);
	// addAlignment
	Alignment al1 = new AlignmentParser( 0 ).parse( "file:examples/rdf/newsample.rdf" );
	noo.addAlignment( al1 );
	assertEquals( noo.getOntologies().size(), 4);
	// addAlignment
	Alignment al2 = new URIAlignment();
	al2.init( al1.getOntology1URI(), al1.getOntology2URI() );
	noo.addAlignment( al2 );
	assertEquals( noo.getAlignments().size(), 2 );
	assertEquals( noo.getOntologies().size(), 4);
	noo.addAlignment( al2 );
	assertEquals( noo.getAlignments().size(), 2 );
	assertEquals( noo.getOntologies().size(), 4);
	// remAlignment
	noo.remAlignment( al1 );
	assertEquals( noo.getAlignments().size(), 1 );
	// addAlignment
	noo.addAlignment( al1 );
	assertEquals( noo.getAlignments().size(), 2 );
	// impact on ontologies?
    }

    @Test(groups = { "full", "raw" }, dependsOnMethods = {"ontologyTest","alignmentTest"})
    public void lambdaTest() throws URISyntaxException {
	URI u = new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl");
	assertEquals( noo.getTargetingAlignments(u).size(), 0 );
	assertEquals( noo.getSourceAlignments(u).size(), 0 );
	u = new URI("file:examples/rdf/edu.mit.visus.bibtex.owl");
	assertEquals( noo.getTargetingAlignments(u).size(), 0 );
	assertEquals( noo.getSourceAlignments(u).size(), 0 );
	u = new URI("http://www.example.org/ontology1");
	assertEquals( noo.getTargetingAlignments(u).size(), 0 );
	assertEquals( noo.getSourceAlignments(u).size(), 2 );
	u = new URI("http://www.example.org/ontology2");
	assertEquals( noo.getTargetingAlignments(u).size(), 2 );
	assertEquals( noo.getSourceAlignments(u).size(), 0 );
    }

    @Test(groups = { "full", "raw" }, dependsOnMethods = {"lambdaTest"})
	public void weakenTest() throws URISyntaxException, AlignmentException {
	OntologyNetwork noon = null;
	noon = OntologyNetworkWeakener.weakenAlignments( noo, 1., true );
	Set<Alignment> s = noon.getTargetingAlignments(new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl"));


	noon = OntologyNetworkWeakener.weakenAlignments( noo, .5, true );
	noon = OntologyNetworkWeakener.weakenAlignments( noo, 0., true );
	noon = OntologyNetworkWeakener.weakenAlignments( noo, 0., false );
	noon = OntologyNetworkWeakener.weakenAlignments( noo, .5, false );
	noon = OntologyNetworkWeakener.weakenAlignments( noo, 1., false );
    }

    @Test(groups = { "full", "raw" }, dependsOnMethods = {"lambdaTest"},expectedExceptions = AlignmentException.class)
    public void weakenExceptionTest1() throws URISyntaxException, AlignmentException {
	OntologyNetworkWeakener.weakenAlignments( noo, 1.2, true );
    }
    @Test(groups = { "full", "raw" }, dependsOnMethods = {"lambdaTest"},expectedExceptions = AlignmentException.class)
    public void weakenExceptionTest2() throws URISyntaxException, AlignmentException {
	OntologyNetworkWeakener.weakenAlignments( noo, -.2, true );
    }
    @Test(groups = { "full", "raw" }, dependsOnMethods = {"lambdaTest"},expectedExceptions = AlignmentException.class)
    public void weakenExceptionTest3() throws URISyntaxException, AlignmentException {
	OntologyNetworkWeakener.weakenAlignments( noo, 1.2, false );
    }
    @Test(groups = { "full", "raw" }, dependsOnMethods = {"lambdaTest"},expectedExceptions = AlignmentException.class)
    public void weakenExceptionTest4() throws URISyntaxException, AlignmentException {
	OntologyNetworkWeakener.weakenAlignments( noo, -.2, false );
    }
}
