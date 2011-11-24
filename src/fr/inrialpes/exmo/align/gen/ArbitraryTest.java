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
 * Generates an arbitrary test.
 * The days of this class are counted: it is highly redundant with the first part of
 * GenerateBenchmark and should be merged with TestGenerator.
 */

package fr.inrialpes.exmo.align.gen;

//Java classes
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
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

public class ArbitraryTest {
    //private String fileName;                                                    //the initial file
    //private String testNumber;                                                  //the test number
    //private Properties parameters;                                              //the parameters
    public final String namespace = "http://oaei.ontologymatching.org/2011/gen/";

    //constructor
    public ArbitraryTest() {
        //this.fileName = fileName;
        //this.testNumber = testNumber;
        //parameters = params;
    }

    //get the prefix to build the namespace
    public String getPrefix( String fileName ) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    //get the uri
    public String getURI( String fileName, String testNumber ) {
        return this.namespace + getPrefix(fileName) + "/" + testNumber + "/" + fileName + "#";
    }

    //load ontology
    public OntModel loadOntology ( String fileName ) {
        InputStream in = FileManager.get().open( fileName );
        OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        model.read( in, null );
        return model;
    }

    //write ontology
    public static void writeOntology( OntModel model, String destFile, String ns ) {
        try {
            File f = new File(destFile);
            FileOutputStream fout = new FileOutputStream(f);
            Charset defaultCharset = Charset.forName("UTF8");
            RDFWriter writer = model.getWriter("RDF/XML-ABBREV");
            writer.setProperty("showXmlDeclaration","true");
            model.setNsPrefix("", ns);

            model.createOntology(ns);
            
            writer.setProperty( "xmlbase", ns );
            writer.write(model.getBaseModel(), new OutputStreamWriter(fout, defaultCharset), "");
            fout.close();
        } catch (Exception ex) {
            System.err.println("Exception " + ex.getMessage());
        }
    }

    // JE: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> THE REAL STUFF

    // JE: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> WE SHOULD HAVE INCREMENTAL HERE IN THE SAME WAY!

    //modify ontology
    public void modifyOntology( String fileName, String testNumber, Properties params ) {
        //modify the model
        try {
            OntModel model = loadOntology(fileName);                      //load the initial ontology
            JENAOntology onto = new JENAOntology();                         //cast the model into Ontology
            onto.setOntology( (OntModel)model );
            //set the TestGenerator ontology
            TestGenerator t = new TestGenerator();                          //build an instance of TestGenerator
            t.setOntology(onto);
            //set the namespace
            t.setNamespace( getURI( fileName, testNumber) );
            //t.setNamespace( "http://oaei.ontologymatching.org/2011/gen/onto/101/onto.rdf#" );

            Alignment align = t.generate( onto, params );                 //generate the alignment

            JENAOntology modified = (JENAOntology)t.getModifiedOntology();  //get the modified ontology


            //build the directory to save the file
            boolean create;
            create = new File(testNumber).mkdir();
            if ( create )   System.err.println(" Succesufully created the directory ");
            else            System.err.println(" Error creating the directory ");

            //new File(testNumber).mkdir();
            //write the ontology into the directory
            if ( modified.getOntology() instanceof OntModel )
                writeOntology(modified.getOntology(), testNumber + "/" + fileName, getURI( fileName, testNumber ));            //write the ontology
            //write the alignment into the directory
            OutputStream stream = new FileOutputStream( testNumber + "/" + "refalign.rdf" );
            // Outputing
            PrintWriter  writer = new PrintWriter (
                                    new BufferedWriter(
                                         new OutputStreamWriter( stream, "UTF-8" )), true);
            AlignmentVisitor renderer = new RDFRendererVisitor( writer );
            align.render(renderer);
            writer.flush();
            writer.close();
        }
        catch ( Exception ex ) {
            System.err.println( "Error " + ex.getMessage()  );
        }

        System.err.println( "***" );
        System.err.println( "END" );
        System.err.println( "***" );
    }


}
