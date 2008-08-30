/*
 * $Id: OMWGTest.java 799 2008-08-28 22:07:58Z euzenat $
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
import org.semanticweb.owl.align.Alignment;

import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.onto.Ontology;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import org.xml.sax.SAXException;

/**
 * These tests corresponds to the tests presented in the examples/omwg directory
 */

public class IOTests {

    private Alignment alignment = null;
    private AlignmentParser aparser = null;

    @Test(groups = { "full", "io" }, expectedExceptions = SAXException.class)
    public void loadSOAPErrorTest() throws Exception {
	aparser = new AlignmentParser( 0 );
	assertNotNull( aparser );
	alignment = aparser.parse( "test/input/soap.xml" );
	// error (we forgot to tell the parser that the alignment is embedded)
    }

    @Test(groups = { "full", "io" }, dependsOnMethods = {"loadSOAPErrorTest"})
    public void loadSOAPTest() throws Exception {
	aparser.initAlignment( null );
	aparser.setEmbedded( true );
	alignment = aparser.parse( "test/input/soap.xml" );
	assertNotNull( alignment );
	assertTrue( alignment instanceof URIAlignment );
	assertEquals( alignment.getOntology2URI().toString(), "http://alignapi.gforge.inria.fr/tutorial/edu.mit.visus.bibtex.owl" );
	assertEquals( alignment.nbCells(), 57 );
    }

}
