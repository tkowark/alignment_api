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
 * Generates a single test by taking as input either an ontology
 * or an ontology and an alignment and generating a modified ontology
 * and an alignment between this one and either the initial ontology
 * or the first ontology of the alignments.
 *
 * Alterations are specified in a list of parameters.
 */

package fr.inrialpes.exmo.align.gen;

// Alignment API implementation classes
import fr.inrialpes.exmo.align.gen.inter.AlignedOntologyGenerator;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;

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

import java.util.Properties;

public class TestGenerator implements AlignedOntologyGenerator {
    private String urlprefix = "http://example.com/"; // Prefix (before testnumber) of test URL
    private String dirprefix = "";                    // Prefix (idem) of directory
    private String ontoname = "onto.rdf";             // name of ontology to generate
    private String alignname = "refalign.rdf";        // name of alignment to generate

    private Properties params;                                                  //the modifications
    private OntModel modifiedOntology;                                             //modified ontology
    private Alignment resultAlignment;                                                //the reference alignment
    private String namespace = "";                                             //the namespace
    private OntologyModifier modifier = null;                                   //the modifier
    private boolean debug = false;

    public TestGenerator() {}

    // ******************************************************* SETTERS
    public void setURLPrefix( String u ) { urlprefix = u; }

    public void setDirPrefix( String d ) { dirprefix = d; }

    public void setOntoFilename( String o ) { ontoname = o; }

    public void setAlignFilename( String a ) { alignname = a; }

    public void setDebug( boolean d ) { debug = d; }

    //returns the modified ontology
    public OntModel getModifiedOntology() { return modifiedOntology ; }

    public void setNamespace( String ns ) { namespace = ns; }
    //return the newNamespace
    public String getNamespace() { return namespace; }

    // ******************************************************* GB STUFF

    //gets the URI
    public String getURI( String testNumber ) {
        return urlprefix + "/" + testNumber + "/" + ontoname + "#"; // Do not like this #...
        //return urlprefix + getPrefix(ontoname) + "/" + testNumber + "/" + ontoname + "#";
    }

    public static String directoryName( String dir, String suffix ) {
	if ( suffix == null ) return dir;
	else return dir+"-"+suffix;
    }

    // ******************************************************* FACILITIES

    public OntModel loadOntology ( String file ) {
        InputStream in = FileManager.get().open( file );
        OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        model.read( in, null );
	return model;
    }

    //writes ontology
    public static void writeOntology( OntModel model, String destFile, String ns ) {
	// JE: How to ensure that it generates the owl:Ontology close?
	// Otherwise, some parsers cannot parse it correctly
        try {
            File f = new File( destFile );
            FileOutputStream fout = new FileOutputStream( f );
            Charset defaultCharset = Charset.forName("UTF8");
            RDFWriter writer = model.getWriter("RDF/XML-ABBREV");
            writer.setProperty( "showXmlDeclaration","true" );
            model.setNsPrefix( "", ns ); // JE: what about it?
            writer.setProperty( "xmlbase", ns );
            model.createOntology( ns );
            writer.write( model.getBaseModel(), new OutputStreamWriter(fout, defaultCharset), "");
            fout.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void outputTestDirectory( OntModel onto, Alignment align, String testnumber ) {
	// build the directory to save the file
	boolean create = new File( dirprefix+"/"+testnumber ).mkdir();
	/* if ( create )   System.err.println(" Succesufully created the directory ");
	   else            System.err.println(" Error creating the directory "); */
	
	// write the ontology into the directory
	writeOntology( onto, dirprefix+"/"+testnumber+"/"+ontoname, getURI( testnumber ));
	
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

    //modifies an ontology from an existing test
    /**
     * Generate a test by altering an existing test
     */
    public Properties incrementModifyOntology( String pKey, String pVal, String suffix, String prevTest, Properties al, String testNb ) {
	Properties p = new Properties();
	p.setProperty( pKey, pVal );
	String prevDirName = directoryName( prevTest, suffix );
	String crtDirName = directoryName( testNb, suffix );
	return modifyOntology( dirprefix+"/"+prevDirName+"/"+ontoname, al, crtDirName, p );
    }

    /**
     * Generate a test by altering an existing test
     */
    public Properties incrementModifyOntology( String prevTestDir, Properties al, String testDir, Properties params ) {
	// JE: maybe ERROR crtDirName
	return modifyOntology( dirprefix+"/"+prevTestDir+"/"+ontoname, al, testDir, params );
    }

    //modifies an ontology
    /**
     * Generate a test from an ontology
     */
    public Properties modifyOntology( String file, Properties al, String testNumber, Properties params) {
	if ( debug ) System.err.println( "Source: "+file+" Target "+testNumber );
	//set the TestGenerator ontology
	OntModel onto = loadOntology( file );
	//set the namespace
	setNamespace( getURI( testNumber ) );
	Alignment align = null;
	if ( al == null )
	    align = generate( onto, params );
	else
	    align = generate( onto, params, al );
	
	outputTestDirectory( getModifiedOntology(), align, testNumber );
	return modifier.getProperties();
    }


    // ******************************************************* GENERATOR
    //generate the alingnment
    public Alignment generate( OntModel onto, Properties params, Properties initalign ) {
        if ( debug ) {
	    System.err.println( "[-------------------------------------------------]" );
	    System.err.println( urlprefix+" / "+dirprefix+" / "+ontoname+" / "+alignname );
	}
	// Initialise the modifier class
        modifier = new OntologyModifier( onto, new URIAlignment() );
	modifier.setDebug( debug );
        modifier.setNewNamespace( namespace );
	// Initialize the reference alignment
        if ( initalign == null ) {
	    modifier.initializeAlignment();                                         
	} else {
	    modifier.initializeAlignment( initalign );
	}

	// Apply all modifications
	// JE: Here there is an obvious problems that the modifications are NOT applied in the specified order!
	// Hence we should have a mega reordering of these parameter (for all of these, if they are here, do something)
	// That would be better as a list in this case than parameters
	// But parameters are more flexible...
	applyModification( modifier, params, ParametersIds.REMOVE_CLASSES );
	applyModification( modifier, params, ParametersIds.REMOVE_PROPERTIES );
	applyModification( modifier, params, ParametersIds.REMOVE_RESTRICTIONS );
	applyModification( modifier, params, ParametersIds.REMOVE_COMMENTS );

	applyModification( modifier, params, ParametersIds.ADD_CLASSES );
	applyModification( modifier, params, ParametersIds.ADD_PROPERTIES );

	// SUPPRESSED FOR TESTING THAT THIS IS THE CULPRIT
	//if ( params.getProperty( ParametersIds.ADD_CLASSES ) == null ) applyModification( modifier, params, ParametersIds.RENAME_CLASSES );
	applyModification( modifier, params, ParametersIds.RENAME_CLASSES );
	applyModification( modifier, params, ParametersIds.RENAME_PROPERTIES );
	// UNTIL HERE, WE USE THE DOCUMENTED ORDER

	applyModification( modifier, params, ParametersIds.REMOVE_CLASSESLEVEL );
	applyModification( modifier, params, ParametersIds.LEVEL_FLATTENED );
	applyModification( modifier, params, ParametersIds.NO_HIERARCHY );
	applyModification( modifier, params, ParametersIds.ADD_CLASSESLEVEL );
	applyModification( modifier, params, ParametersIds.REMOVE_INDIVIDUALS );

	/*
        for( String key : params.stringPropertyNames() ) {
            String value = params.getProperty(key);
	    //if ( debug ) System.out.println( "[" +key + "] => [" + value + "]");
	    modifier.modifyOntology( key, value );					//modify the ontology according to it
        }
	*/

	//saves the alignment into the file "refalign.rdf", null->System.out
        modifier.computeAlignment( alignname );                  //at the end, compute the reference alignment
        resultAlignment = modifier.getAlignment();                               //get the reference alignment
        modifiedOntology = modifier.getModifiedOntology();                    //get the modified ontology
        return resultAlignment;
    }

    public Alignment generate( OntModel onto, Properties p ) {
	return generate( onto, p, (Properties)null );
    }

    public void applyModification( OntologyModifier modifier, Properties p, String m ) {
	modifier.modifyOntology( m, p.getProperty( m ) );
    }
}

