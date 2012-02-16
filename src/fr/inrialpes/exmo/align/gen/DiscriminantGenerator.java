/*
 * $Id$
 *
 * Copyright (C) 2012, INRIA
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

/*
 * Generates the OAEI Benchmark dataset from an ontology
 * It can generate it in a continuous way (each test build on top of a previous one)
 * or generate tests independently.
 */

package fr.inrialpes.exmo.align.gen;

import java.util.Properties;

public class DiscriminantGenerator extends TestSet {

    public void initTestCases( Properties params ) {
	// Process params
	debug = ( params.getProperty( "debug" ) != null );

	// JE: ugly 
	secondOntoFile = params.getProperty( "outdir" )+"/000/onto.rdf";

	// Test configuration parameters
	int maximum = 5;
	float incr = 0.2f;
	String mod = params.getProperty( "modality" ); // "mult"
	boolean multModality = (mod != null && mod.startsWith( "mult" ));
	String hard = params.getProperty( "increment" );
	try {
	    if ( hard != null && !hard.equals("") ) incr = Float.parseFloat( hard );
	} catch ( Exception ex ) {
	    ex.printStackTrace(); // continue with the default
	}
	String max = params.getProperty( "maximum" );
	if ( max != null ) maximum = Integer.parseInt( max );
	if ( debug ) System.err.println( " Mod: "+mod+" / Incr: "+incr+" / Max: "+maximum );

        /* Test 000 Generate the initial situation */
	root = initTests( "000" );
	float i1 = 0.0f;

	/* Iterator for gradual change 
	for ( int i = 0; i1 < 1.00f && i < maximum ; i++ ) { //
	} */

	/* Iterator for gradual change
	for ( int i = 0; i1 < 1.00f && i < maximum ; i++ ) { //
	    if ( i > 0 ) PREVTEST = "201"+SUFFIX; // The previous suffix
	    if ( !multModality ) i1 += incr; // traditional
	    else i1 += (1. - i1) * incr; // hardened
	    //if ( debug ) System.err.println( " ******************************************************** "+i+": i1 = "+i1 );

	    if ( i1 < 1.0f ) {
		SUFFIX = "-"+(i+1)*2; //((Float)i1).toString().substring(2, 3); // 2 4 6 8
	    } else {
		SUFFIX = "";
	    }

        }  */

	final String FIRST1 = "0.25f";
	final String HALF = "0.5f";
	final String MORE3 = "0.75f";
	// Then FULL

	// Degradation is 4* .25

	/* 004-x *** no names */
	addTestChild( "000", "001",
		      newProperties( ParametersIds.RENAME_CLASSES, FIRST1,
				     ParametersIds.RENAME_PROPERTIES, FIRST1 ) );
	addTestChild( "000", "002",
		      newProperties( ParametersIds.RENAME_CLASSES, HALF,
				     ParametersIds.RENAME_PROPERTIES, HALF ) );
	addTestChild( "000", "003",
		      newProperties( ParametersIds.RENAME_CLASSES, MORE3,
				     ParametersIds.RENAME_PROPERTIES, MORE3 ) );
	addTestChild( "000", "004",
		      newProperties( ParametersIds.RENAME_CLASSES, FULL,
				     ParametersIds.RENAME_PROPERTIES, FULL ) );
	/* 044 *** no names + no comments */
	addTestChild( "004", "014",
		      newProperties( ParametersIds.REMOVE_COMMENTS, FIRST1 ) );
	addTestChild( "004", "024",
		      newProperties( ParametersIds.REMOVE_COMMENTS, HALF ) );
	addTestChild( "004", "034",
		      newProperties( ParametersIds.REMOVE_COMMENTS, MORE3 ) );
	addTestChild( "004", "044",
		      newProperties( ParametersIds.REMOVE_COMMENTS, FULL ) );
        /* 040 *** no comments */
	addTestChild( "000", "010",
		      newProperties( ParametersIds.REMOVE_COMMENTS, FIRST1 ) );
	addTestChild( "000", "020",
		      newProperties( ParametersIds.REMOVE_COMMENTS, HALF ) );
	addTestChild( "000", "030",
		      newProperties( ParametersIds.REMOVE_COMMENTS, MORE3 ) );
	addTestChild( "000", "040",
		      newProperties( ParametersIds.REMOVE_COMMENTS, FULL ) );
        /* 044 *** no comments */
	addTestChild( "040", "041", 
		      newProperties( ParametersIds.RENAME_CLASSES, FIRST1,
				     ParametersIds.RENAME_PROPERTIES, FIRST1 ) );
	addTestChild( "040", "042", 
		      newProperties( ParametersIds.RENAME_CLASSES, HALF,
				     ParametersIds.RENAME_PROPERTIES, HALF ) );
	addTestChild( "040", "043", 
		      newProperties( ParametersIds.RENAME_CLASSES, MORE3,
				     ParametersIds.RENAME_PROPERTIES, MORE3 ) );
        /* 440 *** no comments */
	addTestChild( "040", "140", 
		      newProperties( ParametersIds.REMOVE_PROPERTIES, FIRST1 ) );
	addTestChild( "040", "240", 
		      newProperties( ParametersIds.REMOVE_PROPERTIES, HALF ) );
	addTestChild( "040", "340", 
		      newProperties( ParametersIds.REMOVE_PROPERTIES, MORE3 ) );
        /* 400 *** no property */
	addTestChild( "000", "100", 
		      newProperties( ParametersIds.REMOVE_PROPERTIES, FIRST1 ) );
	addTestChild( "000", "200", 
		      newProperties( ParametersIds.REMOVE_PROPERTIES, HALF ) );
	addTestChild( "000", "300", 
		      newProperties( ParametersIds.REMOVE_PROPERTIES, MORE3 ) );
	addTestChild( "000", "400", 
		      newProperties( ParametersIds.REMOVE_PROPERTIES, FULL ) );
	/* 270 *** no property + no instance */
	addTestChild( "004", "104", 
		      newProperties( ParametersIds.REMOVE_PROPERTIES, FIRST1 ) );
	addTestChild( "004", "204", 
		      newProperties( ParametersIds.REMOVE_PROPERTIES, HALF ) );
	addTestChild( "004", "304", 
		      newProperties( ParametersIds.REMOVE_PROPERTIES, MORE3 ) );
	addTestChild( "004", "404", 
		      newProperties( ParametersIds.REMOVE_PROPERTIES, FULL ) );
	addTestChild( "400", "401", 
		      newProperties( ParametersIds.RENAME_CLASSES, FIRST1,
				     ParametersIds.RENAME_PROPERTIES, FIRST1 ) );
	addTestChild( "400", "402", 
		      newProperties( ParametersIds.RENAME_CLASSES, HALF,
				     ParametersIds.RENAME_PROPERTIES, HALF ) );
	addTestChild( "400", "403", 
		      newProperties( ParametersIds.RENAME_CLASSES, MORE3,
				     ParametersIds.RENAME_PROPERTIES, MORE3 ) );
	/* 444 *** no property + expand */
	addTestChild( "404", "414",
		      newProperties( ParametersIds.REMOVE_COMMENTS, FIRST1 ) );
	addTestChild( "404", "424",
		      newProperties( ParametersIds.REMOVE_COMMENTS, HALF ) );
	addTestChild( "404", "434",
		      newProperties( ParametersIds.REMOVE_COMMENTS, MORE3 ) );
	/* 280 *** no property + expand */
	addTestChild( "400", "410", 
		      newProperties( ParametersIds.REMOVE_COMMENTS, FIRST1 ) );
	addTestChild( "400", "420", 
		      newProperties( ParametersIds.REMOVE_COMMENTS, HALF ) );
	addTestChild( "400", "430", 
		      newProperties( ParametersIds.REMOVE_COMMENTS, MORE3 ) );
	addTestChild( "400", "440", 
		      newProperties( ParametersIds.REMOVE_COMMENTS, FULL ) );
	addTestChild( "440", "441", 
		      newProperties( ParametersIds.RENAME_CLASSES, FIRST1,
				     ParametersIds.RENAME_PROPERTIES, FIRST1 ) );
	addTestChild( "440", "442", 
		      newProperties( ParametersIds.RENAME_CLASSES, HALF,
				     ParametersIds.RENAME_PROPERTIES, HALF ) );
	addTestChild( "440", "443", 
		      newProperties( ParametersIds.RENAME_CLASSES, MORE3,
				     ParametersIds.RENAME_PROPERTIES, MORE3 ) );
	/* 250-x *** no names + no comments + no property */
	addTestChild( "044", "144",
		      newProperties( ParametersIds.REMOVE_PROPERTIES, FIRST1 ) );
	addTestChild( "044", "244",
		      newProperties( ParametersIds.REMOVE_PROPERTIES, HALF ) );
	addTestChild( "044", "344",
		      newProperties( ParametersIds.REMOVE_PROPERTIES, MORE3 ) );
	addTestChild( "044", "444",
		      newProperties( ParametersIds.REMOVE_PROPERTIES, FULL ) );
    }


}
