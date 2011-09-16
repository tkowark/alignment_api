/*
 * $Id: ArbitraryTest.java$
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
 */

package fr.inrialpes.exmo.align.gen;

//Java classes
import java.io.FileOutputStream;
import java.io.InputStream;

//Jena API classes
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.util.FileManager;

// Alignment API implementation classes
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Properties;
import fr.inrialpes.exmo.ontowrap.jena25.JENAOntology;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;

public class ArbitraryTest {
    private String fileName;                                                    //the initial file
    private String testNumber;                                                  //the test number
    private String [] params;                                                   //vector to build the parameters
    private Properties parameters;                                              //the parameters
    public final String namespace = "http://oaei.ontologymatching.org/2011/gen/";

    //constructor
    public ArbitraryTest(String fileName, String testNumber, String [] params) {
        this.fileName = fileName;
        this.testNumber = testNumber;
        this.params = params;
    }

    //build the parameters
    public void buildParameters() {
        parameters = new Properties();                                          //initialize the parameters

        for ( int i=0; i<params.length; i+=2 ) {
            if ( params[i].equals("addClasses") )                               /* add percentage classes */
                parameters.setProperty(ParametersIds.ADD_CLASSES, params[i+1]);

            if ( params[i].equals("addProperties") )                            /* add percentage properties */
                parameters.setProperty(ParametersIds.ADD_PROPERTIES, params[i+1]);

            //add c classes beginning from level l -> the value of this parameters should be:
            //beginning_level.number_of_classes_to_add
            if ( params[i].equals("addClassesLevel") )                          /* add c classes beginning from level l */
                parameters.setProperty(ParametersIds.ADD_CLASSES, params[i+1]);
            
            if ( params[i].equals("removeClasses") )                            /* remove percentage classes */
                parameters.setProperty(ParametersIds.REMOVE_CLASSES, params[i+1]);

            if ( params[i].equals("removeProperties") )                         /* remove percentage properties */
                parameters.setProperty(ParametersIds.REMOVE_PROPERTIES, params[i+1]);

            if ( params[i].equals("removeComments") )                           /* remove percentage comments */
                parameters.setProperty(ParametersIds.REMOVE_COMMENTS, params[i+1]);

            if ( params[i].equals("removeRestrictions") )                       /* remove percentage restrictions */
                parameters.setProperty(ParametersIds.REMOVE_RESTRICTIONS, params[i+1]);

            if ( params[i].equals("removeIndividuals") )                        /* remove percentage individuals */
                parameters.setProperty(ParametersIds.REMOVE_INDIVIDUALS, params[i+1]);

            if ( params[i].equals("renameClasses") )                            /* rename percentage classes */
                parameters.setProperty(ParametersIds.RENAME_CLASSES, params[i+1]);

            if ( params[i].equals("renameProperties") )                         /* rename percentage properties */
                parameters.setProperty(ParametersIds.RENAME_PROPERTIES, params[i+1]);

            if ( params[i].equals("levelFlattened") )                           /* flattened level */
                parameters.setProperty(ParametersIds.LEVEL_FLATTENED, params[i+1]);

            if ( params[i].equals( ("noHierarchy")) )                           /* no hierarchy */
                parameters.setProperty( ParametersIds.NO_HIERARCHY, ParametersIds.NO_HIERARCHY);
        }
    }

    //get the prefix to build the namespace
    public String getPrefix(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    //get the uri
    public String getURI(String fileName, String testNumber) {
        return this.namespace + this.getPrefix(fileName) + "/" + testNumber + "/" + fileName + "#";
    }

    //load ontology
    public OntModel loadOntology (String fileName) {
        InputStream in = FileManager.get().open( fileName );
        OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        model.read(in, null);
        return model;
    }

    //write ontology
    public static void writeOntology(OntModel model, String destFile, String ns) {
        try {
            File f = new File(destFile);
            FileOutputStream fout = new FileOutputStream(f);
            Charset defaultCharset = Charset.forName("UTF8");
            RDFWriter writer = model.getWriter("RDF/XML-ABBREV");
            writer.setProperty("showXmlDeclaration","true");
            model.setNsPrefix("", ns);
            writer.setProperty( "xmlbase", ns );
            writer.write(model.getBaseModel(), new OutputStreamWriter(fout, defaultCharset), "");
            fout.close();
        } catch (Exception ex) {
            System.out.println("Exception " + ex.getMessage());
        }
    }


    //modify ontology
    public void modifyOntology(){
        //build the list of parameters
        this.buildParameters();
        //modify the model
        try {
            OntModel model = loadOntology(fileName);                      //load the initial ontology
            TestGenerator t = new TestGenerator();                          //build an instance of TestGenerator
            JENAOntology onto = new JENAOntology();                         //cast the model into Ontology
            onto.setOntology( (OntModel)model );
            //set the TestGenerator ontology
            t.setOntology(onto);
            //set the namespace
            t.setNamespace( this.getURI(this.fileName, this.testNumber) );
            //t.setNamespace( "http://oaei.ontologymatching.org/2011/gen/onto/101/onto.rdf#" );

            Alignment align = t.generate(onto, parameters);                 //generate the alignment

            JENAOntology modified = (JENAOntology)t.getModifiedOntology();  //get the modified ontology


            //build the directory to save the file
            boolean create;
            create = new File(testNumber).mkdir();
            if ( create )   System.out.println(" Succesufully created the directory ");
            else            System.out.println(" Error creating the directory ");

            //new File(testNumber).mkdir();
            //write the ontology into the directory
            if ( modified.getOntology() instanceof OntModel )
                writeOntology(modified.getOntology(), testNumber + "/" + fileName, this.getURI(this.fileName, this.testNumber));            //write the ontology
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

        System.out.println( "***" );
        System.out.println( "END" );
        System.out.println( "***" );
    }


}
