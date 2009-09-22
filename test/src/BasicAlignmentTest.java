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
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNull;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;
//import org.testng.annotations.*;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;

public class BasicAlignmentTest {
    private Alignment alignment = null;

    @BeforeClass(groups = { "full", "raw" })
    private void init(){
	alignment = new BasicAlignment();
    }

    @Test(groups = { "full", "raw" })
    public void aFastTest() {
	assertNotNull( alignment, "Alignment was null" );
    }

    @Test(groups = { "full", "raw" })
    public void someCutTest() throws AlignmentException {
	// THIS SHOULD BE REPLACED WITH ALIGNMENT BUILT IN PREVIOUS TESTS
	AlignmentParser aparser = new AlignmentParser( 0 );
	assertNotNull( aparser, "AlignmentParser was null" );
	Alignment result = aparser.parse( "file:examples/rdf/newsample.rdf" );
	assertNotNull( result, "URIAlignment(result) was null" );
	assertTrue( result instanceof URIAlignment );
	assertEquals( result.nbCells(), 2, "Alignment should contain 2 cells" );
	result.cut( "hard", .5 );
	assertEquals( result.nbCells(), 1, "Alignment should contain 1 cell" );
    }


}
