/*
 * $Id$
 *
 * Copyright (C) 2011, INRIA
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
 * 
 * Variations can also be obtained.
 */

package fr.inrialpes.exmo.align.gen;

import java.util.Properties;

public class GenerateBenchmark {

    private String initOntoFile;                                                    //the initial file
    private Properties align;                                              //the alignment parameter
    private TestGenerator generator;                                  // a TestGenerator
    private boolean debug = false;

    static String FULL = "1.0f";

    //constructor
    public  GenerateBenchmark( String fileName ) {
        initOntoFile = fileName;
	generator = new TestGenerator();
    }

    //generates the Benchmark
    public void generate( Properties params ) {
	// Process params
	if ( params.getProperty( "urlprefix" ) != null ) generator.setURLPrefix( params.getProperty( "urlprefix" ) );
	if ( params.getProperty( "outdir" )!= null )  generator.setDirPrefix( params.getProperty( "outdir" ) );
	String ontoname = params.getProperty( "ontoname" );
	if ( ontoname != null ) {
	    generator.setOntoFilename( params.getProperty( "ontoname" ) );
	} else {
	    ontoname = "onto.rdf"; // could be better
	}
	if ( params.getProperty( "alignname" ) != null ) generator.setAlignFilename( params.getProperty( "alignname" ) );

	debug = ( params.getProperty( "debug" ) != null );
	generator.setDebug( debug );

	//private String testnumber = ""; // will not work if no number...
	//testnumber = params.getProperty( "testNumber" );
	//if ( testnumber == null ) 

        //keep track of the alignment
        Properties alignRenameResources;
        Properties alignInitial;

        Properties p = new Properties();                                        //modify the ontology according to this parameters
        
        String prevDirName;

	String mod = params.getProperty( "modality" ); // "mult"
	String hard = params.getProperty( "increment" );
	String max = params.getProperty( "maximum" );
	if ( debug ) System.err.println( " Mod: "+mod+" / Incr: "+hard );

	String SUFFIX = null;

        /*
         * Test 101
	 * Generate the initial situation
         */
        align = generator.modifyOntology( initOntoFile, (Properties)null, "101", p );
        alignInitial = (Properties)align.clone();
        alignRenameResources = (Properties)align.clone();
        prevDirName = "101"+"/";

        /*
         * Tests 201, 202, 248, 249, 253, 258, 259, 250, 254,
         *       262, 257, 260, 265, 261, 266, 251, 252
         */
	// JE: look at the (Float).toString() below...
	boolean multModality = (mod != null && mod.startsWith( "mult" ));
	float i1 = 0.0f;
	int maximum = Integer.parseInt( "5" );
	float incr = Float.parseFloat( "0.2" );
	try {
	    if ( hard != null && !hard.equals("") ) incr = Float.parseFloat( hard );
	} catch ( Exception ex ) {
	    ex.printStackTrace(); // continue with the default
	}
	for ( int i = 0; i1 < 1.00f ; i++ ) { // && i < maximum
	    if ( !multModality ) i1 += incr; // traditional
	    else i1 += (1. - i1) * incr; // hardened
	    if ( debug ) System.err.println( " ******************************************************** "+i+": i1 = "+i1 );

	    if ( i1 < 1.0f ) {
		SUFFIX = ((Float)i1).toString().substring(2, 3);
	    } else {
		SUFFIX = null;
	    }

            /* 201-x *** no names */
	    // This bootstraps because the 101 has no SUFFIX
            p = new Properties();
            p.setProperty( ParametersIds.RENAME_CLASSES, ((Float)i1).toString() );
            p.setProperty( ParametersIds.RENAME_PROPERTIES, ((Float)i1).toString() );
	    // This dirprefix business could have been solved before...
	    String dirprefix = params.getProperty( "outdir" );
	    if (dirprefix != null ) { dirprefix = dirprefix+"/"; } else { dirprefix = ""; }
	    align = generator.modifyOntology( dirprefix+prevDirName+ontoname, align, generator.directoryName( "201", SUFFIX ), p);

               /* 202-x *** no names + no comments */
	       align = generator.incrementModifyOntology( ParametersIds.REMOVE_COMMENTS, FULL, SUFFIX, "201", align, "202" );

                    /* 248-x *** no names + no comments +  no hierarchy */
	            align = generator.incrementModifyOntology( ParametersIds.NO_HIERARCHY, ParametersIds.NO_HIERARCHY, SUFFIX, "202", align, "248" );

                        /* 253-x *** no names + no comments + no hierarchy + no instance */
	                align = generator.incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "248", align, "253" );

                     /* 249-x *** no names + no comments + no instance */
	             align = generator.incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "202", align, "249" );

                     ////get the parameters
                     alignRenameResources = (Properties)align.clone();

                     /* 250-x *** no names + no comments + no property */
 	             align = generator.incrementModifyOntology( ParametersIds.REMOVE_PROPERTIES, FULL, SUFFIX, "202", align, "250" );

                         /* 254-x *** no names + no comments + no property + no hierarchy */
 	                 align = generator.incrementModifyOntology( ParametersIds.NO_HIERARCHY, ParametersIds.NO_HIERARCHY, SUFFIX, "250", align, "254" );

                             /* 262-x *** no names + no comments + no property + no hierarchy + no instance */
 	                     align = generator.incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "254", align, "262" );

                         /* 257-x *** no names + no comments + no property + no instance */
 	                 align = generator.incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "250", align, "257" );

                         /* 261-x *** no names + no comments + no property + expand */
 	                 align = generator.incrementModifyOntology( ParametersIds.ADD_CLASSES, FULL, SUFFIX, "250", align, "261" );

                             /* 266-x *** no names + no comments + no property + expand + no instance */
  	                    align = generator.incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "261", align, "266" );

                         /* 260-x *** no names + no comments + no property + flatten */
			    align = generator.incrementModifyOntology( ParametersIds.LEVEL_FLATTENED, "2", SUFFIX, "250", align, "260" );

                             /* 265-x *** no names + no comments + no property + flatten + no instance */
   	                    align = generator.incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "260", align, "265" );

                     //re-establish the parameters
                     align = (Properties)alignRenameResources.clone();

                     /* 251-x *** no names + no comments + flatten */
   	             align = generator.incrementModifyOntology( ParametersIds.LEVEL_FLATTENED, "2", SUFFIX, "202", align, "251" );

                         /* 258-x *** no names + no comments + flatten + no instance */
   	                 align = generator.incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "251", align, "258" );

                     //re-establish the parameters
                     align = (Properties)alignRenameResources.clone();

                     /* 252-x *** no names + no comments + expand */
   	             align = generator.incrementModifyOntology( ParametersIds.ADD_CLASSES, FULL, SUFFIX, "202", align, "252" );

                         /* 259-x *** no names + no comments + expand + no instance */
   	                 align = generator.incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "252", align, "259" );

//           alignNoProperty = modifier.getProperties();                           //get the modifed properties

            align = (Properties)alignRenameResources.clone();
            prevDirName = generator.directoryName( "201", SUFFIX ) + "/";
        }

        //re-establish the parameters
	SUFFIX = null;
        align = (Properties)alignInitial.clone();

        /* Tests 221, 232, 233, 241 */

        /* 221 *** no hierarchy */
	align = generator.incrementModifyOntology( ParametersIds.NO_HIERARCHY, ParametersIds.NO_HIERARCHY, SUFFIX, "101", align, "221" );

            /* 232 *** no hierarchy + no instance */
	    align = generator.incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "221", align, "232" );

            /* 233 *** no hierarchy + no property */
	    align = generator.incrementModifyOntology( ParametersIds.REMOVE_PROPERTIES, FULL, SUFFIX, "221", align, "233" );

                /* 241 *** no hierarchy + no property + no instance */
	        align = generator.incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "233", align, "241" );

        //re-establish the align
        align = (Properties)alignInitial.clone();

        /* Tests 222, 237 */

        /* 222 *** flatten */
	align = generator.incrementModifyOntology( ParametersIds.LEVEL_FLATTENED, "2", SUFFIX, "101", align, "222" );

            /* 237 *** flatten + no instance */
	    align = generator.incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "222", align, "237" );
                   

        //re-establish the parameters
        align = (Properties)alignInitial.clone();

        /* Tests 223, 238 */

        /* 223 *** expand */
	align = generator.incrementModifyOntology( ParametersIds.ADD_CLASSES, FULL, SUFFIX, "101", align, "223" );

            /* 238 *** expand + no instance */
	    align = generator.incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "223", align, "238" );

        /* 224 *** no instance */
	align = generator.incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "101", align, "224" );

        /* 225 *** no restrictions */
	align = generator.incrementModifyOntology( ParametersIds.REMOVE_RESTRICTIONS, FULL, SUFFIX, "101", align, "225" );

        /* Tests 228, 239, 246, 236, 240, 247 */

        /* 228 *** no property */
	align = generator.incrementModifyOntology( ParametersIds.REMOVE_PROPERTIES, FULL, SUFFIX, "101", align, "228" );

            /* 236 *** no property + no instance */
	    align = generator.incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "228", align, "236" );

            /* 240 *** no property + expand */
	    align = generator.incrementModifyOntology( ParametersIds.ADD_CLASSES, FULL, SUFFIX, "228", align, "240" );

                /* 247 *** no property + expand + no instance */
	        align = generator.incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "240", align, "247" );

            /* 239 *** no property + flatten */
	    align = generator.incrementModifyOntology( ParametersIds.LEVEL_FLATTENED, FULL, SUFFIX, "228", align, "239" );

                /* 246 *** no property + flatten + no instance */
	        align = generator.incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "239", align, "246" );
    }
}
