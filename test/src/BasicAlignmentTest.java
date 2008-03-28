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

public class BasicAlignmentTest {
    private OWLAPIAlignment alignment = null;

    @BeforeClass(groups = { "fast" })
    private void init(){
	alignment = new OWLAPIAlignment();
    }

    @Test(groups = { "fast" })
    public void aFastTest() {
	assertNotNull( alignment, "Alignment was null" );
    }

}
