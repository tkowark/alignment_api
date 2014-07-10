/*
 * $Id$
 *
 * Copyright (C) INRIA, 2009-2011
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

import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.impl.edoal.EDOALCell;
import fr.inrialpes.exmo.align.impl.edoal.Linkkey;
import fr.inrialpes.exmo.align.impl.edoal.LinkkeyBinding;
import fr.inrialpes.exmo.align.impl.edoal.PropertyId;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.Test;

import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Alignment;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.util.NullStream;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import org.semanticweb.owl.align.Cell;
import static org.testng.Assert.assertFalse;

/**
 * These tests corresponds to the tests presented in the examples/omwg directory
 */
public class EDOALParserTest {

    private AlignmentParser aparser1 = null;
    

    @Test(groups = {"full", "omwg", "raw"})
    public void setUp() throws Exception {
        aparser1 = new AlignmentParser(0);
        assertNotNull(aparser1);
    }

    @Test(groups = {"full", "omwg", "raw"}, dependsOnMethods = {"setUp"})
    public void typedParsingTest() throws Exception {
        AlignmentParser aparser2 = new AlignmentParser(2);
        aparser2.initAlignment(null);
        // Would be good to close System.err at that point...
        OutputStream serr = System.err;
        System.setErr(new PrintStream(new NullStream()));
        Alignment al = aparser2.parse("file:examples/omwg/total.xml");
        System.setErr(new PrintStream(serr));
        assertNotNull(al);
    }

    @Test(groups = {"full", "omwg", "raw"}, dependsOnMethods = {"typedParsingTest"})
    public void linkeyParsingTest() throws Exception {
        // Load the full test
        aparser1.initAlignment(null);
        EDOALAlignment alignment = (EDOALAlignment) aparser1.parse("file:test/input/alignment2.rdf");
        assertNotNull(alignment);
        Enumeration<Cell> cells = alignment.getElements();
        EDOALCell cell = (EDOALCell) cells.nextElement();
        assertFalse(cells.hasMoreElements());

        Set<Linkkey> linkkeys = cell.linkkeys();
        assertEquals(linkkeys.size(), 1);
        Linkkey linkkey = linkkeys.iterator().next();
        assertEquals(linkkey.getType(), "weak");

        Set<LinkkeyBinding> bindings = linkkey.bindings();
        assertEquals(bindings.size(), 2);
        Iterator<LinkkeyBinding> bindingIter = bindings.iterator();
        LinkkeyBinding binding = bindingIter.next();
        LinkkeyBinding firstBinding = null;
        LinkkeyBinding secondBinding = null;
        if(binding.getType().equals("eq")){
            firstBinding = binding;
            secondBinding =  bindingIter.next();
        }
        else{
            firstBinding = bindingIter.next();
            secondBinding =  binding;
            
        }
        assertEquals(firstBinding.getType(), "eq");
        assertEquals(((PropertyId)firstBinding.getExpression1()).getURI().toString(), "http://purl.org/ontology/mo/opus");
        assertEquals(((PropertyId)firstBinding.getExpression2()).getURI().toString(), "http://exmo.inrialpes.fr/connectors#number");
        
        assertEquals(secondBinding.getType(), "in");
        assertEquals(((PropertyId)secondBinding.getExpression1()).getURI().toString(), "http://purl.org/ontology/mo/name");
        assertEquals(((PropertyId)secondBinding.getExpression2()).getURI().toString(), "http://exmo.inrialpes.fr/connectors#nom");
    }
    
    @Test(groups = {"full", "omwg", "raw"}, dependsOnMethods = {"linkeyParsingTest"})
    public void roundTripTest() throws Exception {
        // Load the full test
        aparser1.initAlignment(null);
        Alignment alignment = aparser1.parse("file:examples/omwg/total.xml");
        assertNotNull(alignment);
        // Print it in a string
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(stream, "UTF-8")), true);
        AlignmentVisitor renderer = new RDFRendererVisitor(writer);
        alignment.render(renderer);
        writer.flush();
        writer.close();
        String str1 = stream.toString();
        // Read it again
        aparser1 = new AlignmentParser(0);
        aparser1.initAlignment(null);
        //System.err.println( str1 );
        Alignment al = aparser1.parseString(str1);
        assertEquals(alignment.nbCells(), al.nbCells());
        // Print it in another string
        stream = new ByteArrayOutputStream();
        writer = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(stream, "UTF-8")), true);
        renderer = new RDFRendererVisitor(writer);
        al.render(renderer);
        writer.flush();
        writer.close();
        String str2 = stream.toString();
	// They should be the same... (no because of invertion...)
        //assertEquals( str1, str2 );
        // But have the same length
        assertEquals(str1.length(), str2.length());
    }

}
