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

//Java classes
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.File;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Properties;

//Jena API classes
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.util.FileManager;

// Alignment API implementation classes
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.ontowrap.jena25.JENAOntology;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;

public class GenerateBenchmark {
    private String urlprefix = "http://example.com/"; // Prefix (before testnumber) of test URL
    private String dirprefix = "";                    // Prefix (idem) of directory
    private String filename = "onto.rdf";             // name of ontology to generate
    private String alignname = "refalign.rdf";        // name of alignment to generate

    private String fileName;                                                    //the initial file
    //private String testNumber;                                                  //the test number
    private Properties parameters;                                              //the parameters
    public final String namespace = "http://oaei.ontologymatching.org/2011/gen/";
    private TestGenerator generator;                                  //build an instance of TestGenerator
    private boolean debug = false;

    static String FULL = "1.0f";

    //constructor
    public  GenerateBenchmark( String fileName ) {
        this.fileName = fileName;
	generator = new TestGenerator();
    }

    //gets the URI
    public String getURI( String testNumber ) {
        return urlprefix + "/" + testNumber + "/" + filename + "#"; // Do not like this #...
        //return urlprefix + getPrefix(fileName) + "/" + testNumber + "/" + fileName + "#";
    }

    private String directoryName( String dir, String suffix ) {
	if ( suffix == null ) return dir;
	else return dir+"-"+suffix;
    }

    //loads ontology
    public OntModel loadOntology ( String file ) {
        InputStream in = FileManager.get().open( file );
        OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        model.read( in, null );
        return model;
    }

    //writes ontology
    public static void writeOntology( OntModel model, String destFile, String ns ) {
        try {
            File f = new File( destFile );
            FileOutputStream fout = new FileOutputStream( f );
            Charset defaultCharset = Charset.forName("UTF8");
            RDFWriter writer = model.getWriter("RDF/XML-ABBREV");
            writer.setProperty( "showXmlDeclaration","true" );
            model.setNsPrefix( "", ns );
            model.createOntology(ns);

            writer.setProperty( "xmlbase", ns );
            writer.write(model.getBaseModel(), new OutputStreamWriter(fout, defaultCharset), "");
            fout.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void outputTestDirectory( JENAOntology onto, Alignment align, String testnumber, Properties params ) {
	// build the directory to save the file
	boolean create = new File( dirprefix+"/"+testnumber ).mkdir();
	/* if ( create )   System.err.println(" Succesufully created the directory ");
	   else            System.err.println(" Error creating the directory "); */
	
	// write the ontology into the directory
	if ( onto.getOntology() instanceof OntModel )
	    writeOntology( onto.getOntology(), dirprefix+"/"+testnumber+"/"+filename, getURI( testnumber ));
	
	try {
	    //write the alignment into the directory
	    OutputStream stream = new FileOutputStream( dirprefix+"/"+testnumber+"/"+alignname );
	    // Outputing
	    PrintWriter  writer = new PrintWriter (
						   new BufferedWriter(
								      new OutputStreamWriter( stream, "UTF-8" )), true);
	    AlignmentVisitor renderer = new RDFRendererVisitor( writer );
	    align.render( renderer );
	    writer.flush();
	    writer.close();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    //modifies an ontology
    public Properties modifyOntology( String file, String testNumber, Properties params) {
	System.err.println( "Source: "+file+" generate "+testNumber );
        //modify the model
	if ( debug ) System.err.println( "From test " + file );
	if ( debug ) System.err.println( "Test " + testNumber );
	OntModel model = loadOntology( file );                            //load the initial ontology
	JENAOntology onto = new JENAOntology();                             //cast the model into Ontology
	onto.setOntology( (OntModel)model );
	//set the TestGenerator ontology
	generator.setOntology( onto );
	//set the namespace
	generator.setNamespace( getURI( testNumber ) );
	Alignment align = null;
	if ( file.equals(this.fileName) ) // JE: In my opinion this test is tricky... (and all should be in params)
	    align = generator.generate( onto, params );                               //generate the alignment
	else
	    align = generator.generate( onto, params, parameters );              //generate the alignment
	
	JENAOntology modified = (JENAOntology)generator.getModifiedOntology();      //get the modified ontology
	
	outputTestDirectory( modified, align, testNumber, params );
	//retrieve the parameters
	//parameters = generator.getParameters();
	return generator.getParameters();
    }

    //modifies an ontology from an existing test
    public Properties incrementModifyOntology( String pKey, String pVal, String suffix, String prevTest, String testNb ) {
	Properties p = new Properties();
	p.setProperty( pKey, pVal );
	String prevDirName = directoryName( prevTest, suffix );
	String crtDirName = directoryName( testNb, suffix );
	return modifyOntology( dirprefix+"/"+prevDirName+"/"+this.fileName, crtDirName, p );
    }


    //generates the Benchmark
    public void generate( Properties params ) {
	// Process params
	if ( params.getProperty( "urlprefix" ) != null ) urlprefix = params.getProperty( "urlprefix" );
	if ( params.getProperty( "outdir" )!= null ) dirprefix = params.getProperty( "outdir" );
	if ( params.getProperty( "filename" ) != null ) filename = params.getProperty( "filename" );
	if ( params.getProperty( "alignname" ) != null ) alignname = params.getProperty( "alignname" );

	System.err.println( urlprefix+" / "+dirprefix+" / "+filename+" / "+alignname );

	//private String testnumber = ""; // will not work if no number...
	//testnumber = params.getProperty( "testNumber" );
	//if ( testnumber == null ) 

	// JE: All this should have been solved before


        //keep track of the alignment
        Properties paramsRenameResources;           Properties paramsNoComments;
        Properties paramsNoInstance;                Properties paramsNoProperty;
        Properties paramsFlatten;                   Properties paramsExpand;
        Properties paramsInitial;

        Properties p = new Properties();                                        //modify the ontology according to this parameters
        this.parameters = new Properties();
        
        String prevDirName;
        String crtDirName;

	String mod = params.getProperty( "modality" ); // "mult"
	String hard = params.getProperty( "increment" );
	String max = params.getProperty( "maximum" );
	if ( debug ) System.err.println( " Mod: "+mod+" / Incr: "+hard );

	String SUFFIX = null;

    // JE : >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> SO FAR SO GOOD
        /*
         * Test 101
	 * Here it would be good to change the :
         */
        parameters = modifyOntology( fileName, "101", p ); // JE ~~> The strange thing: it will generate the init
                                                           // Because it calls with the "p", this is the second case, 
	                                                   // Where the initial alignment is generated
        paramsInitial = (Properties)parameters.clone();
        paramsRenameResources = (Properties)parameters.clone();
        prevDirName = "101"+"/";

        /*
         * Tests 201, 202, 248, 249, 253, 258, 259, 250, 254,
         *       262, 257, 260, 265, 261, 266, 251, 252
         */
	// Increment from 20 to 20
        //for ( float i1=0.20f; i1<=1.00f; i1+=0.20f ) {
	// Increment from x to remaining
	// JE: look at the (Float).toString() below...
	boolean multModality = (mod != null && mod.startsWith( "mult" ));
	//float i1 = 0.20f;
	float i1 = 0.0f;
	int maximum = Integer.parseInt( "5" );
	float incr = Float.parseFloat( "0.2" );
	try {
	    if ( hard != null && !hard.equals("") ) incr = Float.parseFloat( hard );
	} catch ( Exception ex ) {
	    ex.printStackTrace(); // continue with the default
	}
	for ( int i = 0; i1 < 1.00f ; i++ ) { // && i < maximum
	    //System.err.println( " **************************** "+i+": i1 = "+i1 );
	    if ( !multModality ) i1 += incr; // traditional
	    else i1 += (1. - i1) * incr; // hardened

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
            parameters = modifyOntology( dirprefix+"/"+prevDirName+this.fileName, directoryName( "201", SUFFIX ), p);

               /* 202-x *** no names + no comments */
	       parameters = incrementModifyOntology( ParametersIds.REMOVE_COMMENTS, FULL, SUFFIX, "201", "202" );

                    /* 248-x *** no names + no comments +  no hierarchy */
	            parameters = incrementModifyOntology( ParametersIds.NO_HIERARCHY, ParametersIds.NO_HIERARCHY, SUFFIX, "202", "248" );

                        /* 253-x *** no names + no comments + no hierarchy + no instance */
	                parameters = incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "248", "253" );

                     /* 249-x *** no names + no comments + no instance */
	             parameters = incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "202", "249" );

                     ////get the parameters
                     paramsRenameResources = (Properties)this.parameters.clone();

                     /* 250-x *** no names + no comments + no property */
 	             parameters = incrementModifyOntology( ParametersIds.REMOVE_PROPERTIES, FULL, SUFFIX, "202", "250" );

                         /* 254-x *** no names + no comments + no property + no hierarchy */
 	                 parameters = incrementModifyOntology( ParametersIds.NO_HIERARCHY, ParametersIds.NO_HIERARCHY, SUFFIX, "250", "254" );

                             /* 262-x *** no names + no comments + no property + no hierarchy + no instance */
 	                     parameters = incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "254", "262" );

                         /* 257-x *** no names + no comments + no property + no instance */
 	                 parameters = incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "250", "257" );

                         /* 261-x *** no names + no comments + no property + expand */
 	                 parameters = incrementModifyOntology( ParametersIds.ADD_CLASSES, FULL, SUFFIX, "250", "261" );

                             /* 266-x *** no names + no comments + no property + expand + no instance */
  	                    parameters = incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "261", "266" );

                         /* 260-x *** no names + no comments + no property + flatten */
			    parameters = incrementModifyOntology( ParametersIds.LEVEL_FLATTENED, "2", SUFFIX, "250", "260" );

                             /* 265-x *** no names + no comments + no property + flatten + no instance */
   	                    parameters = incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "260", "265" );

                     //re-establish the parameters
                     parameters = (Properties)paramsRenameResources.clone();

                     /* 251-x *** no names + no comments + flatten */
   	             parameters = incrementModifyOntology( ParametersIds.LEVEL_FLATTENED, "2", SUFFIX, "202", "251" );

                         /* 258-x *** no names + no comments + flatten + no instance */
   	                 parameters = incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "251", "258" );

                     //re-establish the parameters
                     parameters = (Properties)paramsRenameResources.clone();

                     /* 252-x *** no names + no comments + expand */
   	             parameters = incrementModifyOntology( ParametersIds.ADD_CLASSES, FULL, SUFFIX, "202", "252" );

                         /* 259-x *** no names + no comments + expand + no instance */
   	                 parameters = incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "252", "259" );

//           paramsNoProperty = modifier.getProperties();                           //get the modifed properties

            parameters = (Properties)paramsRenameResources.clone();
            prevDirName = directoryName( "201", SUFFIX ) + "/";
        }

        //re-establish the parameters
	SUFFIX = null;
        parameters = (Properties)paramsInitial.clone();

        /* Tests 221, 232, 233, 241 */

        /* 221 *** no hierarchy */
	parameters = incrementModifyOntology( ParametersIds.NO_HIERARCHY, ParametersIds.NO_HIERARCHY, SUFFIX, "101", "221" );

            /* 232 *** no hierarchy + no instance */
	    parameters = incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "221", "232" );

            /* 233 *** no hierarchy + no property */
	    parameters = incrementModifyOntology( ParametersIds.REMOVE_PROPERTIES, FULL, SUFFIX, "221", "233" );

                /* 241 *** no hierarchy + no property + no instance */
	        parameters = incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "233", "241" );

        //re-establish the parameters
        parameters = (Properties)paramsInitial.clone();

        /* Tests 222, 237 */

        /* 222 *** flatten */
	parameters = incrementModifyOntology( ParametersIds.LEVEL_FLATTENED, "2", SUFFIX, "101", "222" );

            /* 237 *** flatten + no instance */
	    parameters = incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "222", "237" );
                   

        //re-establish the parameters
        parameters = (Properties)paramsInitial.clone();

        /* Tests 223, 238 */

        /* 223 *** expand */
	parameters = incrementModifyOntology( ParametersIds.ADD_CLASSES, FULL, SUFFIX, "101", "223" );

            /* 238 *** expand + no instance */
	    parameters = incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "223", "238" );

        /* 224 *** no instance */
	parameters = incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "101", "224" );

        /* 225 *** no restrictions */
	parameters = incrementModifyOntology( ParametersIds.REMOVE_RESTRICTIONS, FULL, SUFFIX, "101", "225" );

        /* Tests 228, 239, 246, 236, 240, 247 */

        /* 228 *** no property */
	parameters = incrementModifyOntology( ParametersIds.REMOVE_PROPERTIES, FULL, SUFFIX, "101", "228" );

            /* 236 *** no property + no instance */
	    parameters = incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "228", "236" );

            /* 240 *** no property + expand */
	    parameters = incrementModifyOntology( ParametersIds.ADD_CLASSES, FULL, SUFFIX, "228", "240" );

                /* 247 *** no property + expand + no instance */
	        parameters = incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "240", "247" );

            /* 239 *** no property + flatten */
	    parameters = incrementModifyOntology( ParametersIds.LEVEL_FLATTENED, FULL, SUFFIX, "228", "239" );

                /* 246 *** no property + flatten + no instance */
	        parameters = incrementModifyOntology( ParametersIds.REMOVE_INDIVIDUALS, FULL, SUFFIX, "239", "246" );
    }
}
