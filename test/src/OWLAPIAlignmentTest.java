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

import fr.inrialpes.exmo.align.impl.OWLAPIAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.onto.OntologyCache;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

public class OWLAPIAlignmentTest {

    private OWLAPIAlignment alignment = null;

    @BeforeClass(groups = { "fast" })
    private void init(){
	alignment = new OWLAPIAlignment();
    }

    @Test(groups = { "fast" })
    public void aFastTest() {
	//System.out.println("Fast test");
	assertNotNull( alignment, "Alignment was null" );
    }

    @Test(groups = { "full" })
    public void loadingAndConvertingTest() throws Exception {
	//System.out.println("Fast test");
	assertNotNull( alignment, "Alignment was null" );
	AlignmentParser aparser = new AlignmentParser( 0 );
	assertNotNull( aparser, "AlignmentParser was null" );
	URIAlignment result = (URIAlignment)aparser.parse( "file:examples/rdf/newsample.rdf" );
	assertNotNull( result, "URIAlignment(result) was null" );
	alignment = OWLAPIAlignment.toOWLAPIAlignment( result, (OntologyCache)null );
	assertNotNull( alignment, "toOWLAPIAlignment(result) was null" );
	assertEquals( alignment.getLevel(), "0" );
	assertEquals( alignment.getType(), "**" );
	assertEquals( alignment.nbCells(), 2 );
    }

}
