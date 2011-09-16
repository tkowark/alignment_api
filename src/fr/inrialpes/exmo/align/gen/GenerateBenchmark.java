/*
 * $Id: GenerateBenchmark.java$
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
 * Generates the Benchmark dataset.
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

public class GenerateBenchmark {
    private String fileName;                                                    //the initial file
    private String testNumber;                                                  //the test number
    private Properties parameters;                                              //the parameters
    public final String namespace = "http://oaei.ontologymatching.org/2011/gen/";

    //constructor
    public  GenerateBenchmark(String fileName) {
        this.fileName = fileName;
    }

    //gets the prefix to build the namespace
    public String getPrefix(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    //gets the uri
    public String getURI(String fileName, String testNumber) {
        return this.namespace + this.getPrefix(fileName) + "/" + testNumber + "/" + fileName + "#";
    }

    //loads ontology
    public OntModel loadOntology (String fileName) {
        InputStream in = FileManager.get().open( fileName );
        OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        model.read(in, null);
        return model;
    }

    //writes ontology
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

    
    //modifies an ontology
    public void modifyOntology(TestGenerator t, String fileName, String testNumber, Properties params) {
        //modify the model
        try {

            System.out.println("From test " + fileName);
            System.out.println("Test " + testNumber);
            OntModel model = loadOntology(fileName);                            //load the initial ontology
            JENAOntology onto = new JENAOntology();                             //cast the model into Ontology
            onto.setOntology( (OntModel)model );
            //set the TestGenerator ontology
            t.setOntology(onto);
            //set the namespace
            t.setNamespace( this.getURI(this.fileName, testNumber) );
            //t.setNamespace( "http://oaei.ontologymatching.org/2011/gen/onto/101/onto.rdf#" );

            Alignment align = null;
            if ( fileName.equals(this.fileName) )
                align = t.generate(onto, params);                               //generate the alignment
            else
                align = t.generate(onto, params, this.parameters);              //generate the alignment

            JENAOntology modified = (JENAOntology)t.getModifiedOntology();      //get the modified ontology

            //build the directory to save the file
            boolean create;
            create = new File(testNumber).mkdir();
            /* if ( create )   System.out.println(" Succesufully created the directory ");
            else            System.out.println(" Error creating the directory "); */

            //write the ontology into the directory
            if ( modified.getOntology() instanceof OntModel )
                writeOntology(modified.getOntology(), testNumber + "/" + this.fileName, this.getURI( this.fileName, testNumber));            //write the ontology

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
            
            //retrieve the parameters
            this.parameters = t.getParameters();


            //System.out.println("Alignmnent");
            


        }
        catch ( Exception ex ) {
            System.err.println( "Error " + ex.getMessage()  );
        }
        
    }

    public String directoryName(float i1, String dir) {
        String dirName = "";
        if ( ((Float)i1).toString().substring(0, 1).equals("0") ) {
            dirName = dir + "-" + ((Float)i1).toString().substring(2, 3);
        }
        else {
            dirName = dir;
        }

        return dirName;
    }

    //generates the Benchmark
    public void generate() {
        //keep track of the alignment
        Properties paramsRenameResources;           Properties paramsNoComments;
        Properties paramsNoInstance;                Properties paramsNoProperty;
        Properties paramsFlatten;                   Properties paramsExpand;
        Properties paramsInitial;
        Properties p = new Properties();                                        //modify the ontology according to this parameters

        String prevDirName;
        String crtDirName;

        this.parameters = new Properties();
        crtDirName = this.fileName;
        
        TestGenerator t = new TestGenerator();                              //build an instance of TestGenerator
       
        /*
         * Test 101
         */
        this.modifyOntology(t, this.fileName, "101", p);
        paramsInitial = (Properties)parameters.clone();
        paramsRenameResources = (Properties)parameters.clone();
        prevDirName = "101" + "/";


        /*
         * Tests 201, 202, 248, 249, 253, 258, 259, 250, 254,
         *       262, 257, 260, 265, 261, 266, 251, 252
         */

        for ( float i1=0.20f; i1<=1.00f; i1+=0.20f ) {

            /*
             * 201-x *** no names
             */
            p = new Properties();
            p.setProperty(ParametersIds.RENAME_CLASSES, ((Float)i1).toString());
            p.setProperty(ParametersIds.RENAME_PROPERTIES, ((Float)i1).toString());
            crtDirName = directoryName(i1, "201");
            this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

               /*
                * 202-x *** no names + no comments
                */
                p = new Properties();
                p.setProperty(ParametersIds.REMOVE_COMMENTS, ((Float)1.00f).toString());
                prevDirName = directoryName(i1, "201") + "/";
                crtDirName = directoryName(i1, "202");
                this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                    /*
                     * 248-x *** no names + no comments +  no hierarchy
                     */
                     p = new Properties();
                     p.setProperty(ParametersIds.NO_HIERARCHY, ParametersIds.NO_HIERARCHY);
                     prevDirName = directoryName(i1, "202") + "/";
                     crtDirName = directoryName(i1, "248");
                     this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                        /*
                         * 253-x *** no names + no comments + no hierarchy + no instance
                         */
                         p = new Properties();
                         p.setProperty(ParametersIds.REMOVE_INDIVIDUALS, ((Float)1.00f).toString());
                         prevDirName = directoryName(i1, "248") + "/";
                         crtDirName = directoryName(i1, "253");
                         this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                     /*
                      * 249-x *** no names + no comments + no instance
                      */
                     p = new Properties();
                     p.setProperty(ParametersIds.REMOVE_INDIVIDUALS, ((Float)1.00f).toString());
                     prevDirName = directoryName(i1, "202") + "/";
                     crtDirName = directoryName(i1, "249");
                     this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                     ////get the parameters
                     paramsRenameResources = (Properties)this.parameters.clone();

                     /*
                      * 250-x *** no names + no comments + no property
                      */
                     p = new Properties();
                     p.setProperty(ParametersIds.REMOVE_PROPERTIES, ((Float)1.00f).toString());
                     prevDirName = directoryName(i1, "202") + "/";
                     crtDirName = directoryName(i1, "250");
                     this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                         /*
                          * 254-x *** no names + no comments + no property + no hierarchy
                          */
                         p = new Properties();
                         p.setProperty(ParametersIds.NO_HIERARCHY, ParametersIds.NO_HIERARCHY);
                         prevDirName = directoryName(i1, "250") + "/";
                         crtDirName = directoryName(i1, "254");
                         this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                             /*
                              * 262-x *** no names + no comments + no property + no hierarchy + no instance
                              */
                             p = new Properties();
                             p.setProperty(ParametersIds.REMOVE_INDIVIDUALS, ((Float)1.00f).toString());
                             prevDirName = directoryName(i1, "254") + "/";
                             crtDirName = directoryName(i1, "262");
                             this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                         /*
                          * 257-x *** no names + no comments + no property + no instance
                          */
                         p = new Properties();
                         p.setProperty(ParametersIds.REMOVE_INDIVIDUALS, ((Float)1.00f).toString());
                         prevDirName = directoryName(i1, "250") + "/";
                         crtDirName = directoryName(i1, "257");
                         this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                         /*
                          * 261-x *** no names + no comments + no property + expand
                          */
                         p = new Properties();
                         p.setProperty(ParametersIds.ADD_CLASSES, ((Float)1.00f).toString());
                         prevDirName = directoryName(i1, "250") + "/";
                         crtDirName = directoryName(i1, "261");
                         this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                             /*
                              * 266-x *** no names + no comments + no property + expand + no instance
                              */
                             p = new Properties();
                             p.setProperty(ParametersIds.REMOVE_INDIVIDUALS, ((Float)1.00f).toString());
                             prevDirName = directoryName(i1, "261") + "/";
                             crtDirName = directoryName(i1, "266");
                             this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                         /*
                          * 260-x *** no names + no comments + no property + flatten
                          */
                         p = new Properties();
                         p.setProperty(ParametersIds.LEVEL_FLATTENED, "2");
                         prevDirName = directoryName(i1, "250") + "/";
                         crtDirName = directoryName(i1, "260");
                         this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                             /*
                              * 265-x *** no names + no comments + no property + flatten + no instance
                              */
                             p = new Properties();
                             p.setProperty(ParametersIds.REMOVE_INDIVIDUALS, ((Float)1.00f).toString());
                             prevDirName = directoryName(i1, "260") + "/";
                             crtDirName = directoryName(i1, "265");
                             this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                     //re-establish the parameters
                     this.parameters = (Properties)paramsRenameResources.clone();

                     /*
                      * 251-x *** no names + no comments + flatten
                      */
                     p = new Properties();
                     p.setProperty(ParametersIds.LEVEL_FLATTENED, "2");
                     prevDirName = directoryName(i1, "202") + "/";
                     crtDirName = directoryName(i1, "251");
                     this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                         /*
                          * 258-x *** no names + no comments + flatten + no instance
                          */
                         p = new Properties();
                         p.setProperty(ParametersIds.REMOVE_INDIVIDUALS, ((Float)1.00f).toString());
                         prevDirName = directoryName(i1, "251") + "/";
                         crtDirName = directoryName(i1, "258");
                         this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                     //re-establish the parameters
                     this.parameters = (Properties)paramsRenameResources.clone();

                     /*
                      * 252-x *** no names + no comments + expand
                      */
                     p = new Properties();
                     p.setProperty(ParametersIds.ADD_CLASSES, ((Float)1.00f).toString());
                     prevDirName = directoryName(i1, "202") + "/";
                     crtDirName = directoryName(i1, "252");
                     this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                         /*
                          * 259-x *** no names + no comments + expand + no instance
                          */
                         p = new Properties();
                         p.setProperty(ParametersIds.REMOVE_INDIVIDUALS, ((Float)1.00f).toString());
                         prevDirName = directoryName(i1, "252") + "/";
                         crtDirName = directoryName(i1, "259");
                         this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

//           paramsNoProperty = modifier.getProperties();                           //get the modifed properties

            this.parameters = (Properties)paramsRenameResources.clone();
            prevDirName = directoryName(i1, "201") + "/";
        }

        //re-establish the parameters
        this.parameters = (Properties)paramsInitial.clone();

        /*
         * Tests 221, 232, 233, 241
         */

        /*
         * 221 *** no hierarchy
         */
        p = new Properties();
        p.setProperty(ParametersIds.NO_HIERARCHY, ParametersIds.NO_HIERARCHY);
        prevDirName = directoryName(1.00f, "101") + "/";
        crtDirName = directoryName(1.00f, "221");
        this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);
        
            /*
             * 232 *** no hierarchy + no instance
             */
            p = new Properties();
            p.setProperty(ParametersIds.REMOVE_INDIVIDUALS, ((Float)1.00f).toString());
            prevDirName = directoryName(1.00f, "221") + "/";
            crtDirName = directoryName(1.00f, "232");
            this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

            /*
             * 233 *** no hierarchy + no property
             */
            p = new Properties();
            p.setProperty(ParametersIds.REMOVE_PROPERTIES, ((Float)1.00f).toString());
            prevDirName = directoryName(1.00f, "221") + "/";
            crtDirName = directoryName(1.00f, "233");
            this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                /*
                 * 241 *** no hierarchy + no property + no instance
                 */
                p = new Properties();
                p.setProperty(ParametersIds.REMOVE_INDIVIDUALS, ((Float)1.00f).toString());
                prevDirName = directoryName(1.00f, "233") + "/";
                crtDirName = directoryName(1.00f, "241");
                this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

        //re-establish the parameters
        this.parameters = (Properties)paramsInitial.clone();

        /*
         * Tests 222, 237
         */

        /*
         * 222 *** flatten
         */
        p = new Properties();
        p.setProperty(ParametersIds.LEVEL_FLATTENED, "2");
        prevDirName = directoryName(1.00f, "101") + "/";
        crtDirName = directoryName(1.00f, "222");
        this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

            /*
             * 237 *** flatten + no instance
             */
            p = new Properties();
            p.setProperty(ParametersIds.REMOVE_INDIVIDUALS, ((Float)1.00f).toString());
            prevDirName = directoryName(1.00f, "222") + "/";
            crtDirName = directoryName(1.00f, "237");
            this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);
                   

        //re-establish the parameters
        this.parameters = (Properties)paramsInitial.clone();

        /*
         * Tests 223, 238
         */

        /*
         * 223 *** expand
         */
        p = new Properties();
        p.setProperty(ParametersIds.ADD_CLASSES, ((Float)1.00f).toString());
        prevDirName = directoryName(1.00f, "101") + "/";
        crtDirName = directoryName(1.00f, "223");
        this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

            /*
             * 238 *** expand + no instance
             */
            p = new Properties();
            p.setProperty(ParametersIds.REMOVE_INDIVIDUALS, ((Float)1.00f).toString());
            prevDirName = directoryName(1.00f, "223") + "/";
            crtDirName = directoryName(1.00f, "238");
            this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

        /*
         * 224 *** no instance
         */
        p = new Properties();
        p.setProperty(ParametersIds.REMOVE_INDIVIDUALS, ((Float)1.00f).toString());
        prevDirName = directoryName(1.00f, "101") + "/";
        crtDirName = directoryName(1.00f, "224");
        this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

        /*
         * 225 *** no restrictions
         */
        p = new Properties();
        p.setProperty(ParametersIds.REMOVE_RESTRICTIONS, ((Float)1.00f).toString());
        prevDirName = directoryName(1.00f, "101") + "/";
        crtDirName = directoryName(1.00f, "225");
        this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

        /*
         * Tests 228, 239, 246, 236, 240, 247
         */

        /*
         * 228 *** no property
         */
        p = new Properties();
        p.setProperty(ParametersIds.REMOVE_PROPERTIES, ((Float)1.00f).toString());
        prevDirName = directoryName(1.00f, "101") + "/";
        crtDirName = directoryName(1.00f, "228");
        this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

            /*
             * 236 *** no property + no instance
             */
            p = new Properties();
            p.setProperty(ParametersIds.REMOVE_INDIVIDUALS, ((Float)1.00f).toString());
            prevDirName = directoryName(1.00f, "228") + "/";
            crtDirName = directoryName(1.00f, "236");
            this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

            /*
             * 240 *** no property + expand
             */
            p = new Properties();
            p.setProperty(ParametersIds.ADD_CLASSES, ((Float)1.00f).toString());
            prevDirName = directoryName(1.00f, "228") + "/";
            crtDirName = directoryName(1.00f, "240");
            this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                /*
                 * 247 *** no property + expand + no instance
                 */
                p = new Properties();
                p.setProperty(ParametersIds.REMOVE_INDIVIDUALS, ((Float)1.00f).toString());
                prevDirName = directoryName(1.00f, "240") + "/";
                crtDirName = directoryName(1.00f, "247");
                this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

            /*
             * 239 *** no property + flatten
             */
            p = new Properties();
            p.setProperty(ParametersIds.LEVEL_FLATTENED, "2");
            prevDirName = directoryName(1.00f, "228") + "/";
            crtDirName = directoryName(1.00f, "239");
            this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

                /*
                 * 246 *** no property + flatten + no instance
                 */
                p = new Properties();
                p.setProperty(ParametersIds.REMOVE_INDIVIDUALS, ((Float)1.00f).toString());
                prevDirName = directoryName(1.00f, "239") + "/";
                crtDirName = directoryName(1.00f, "246");
                this.modifyOntology(t, prevDirName + this.fileName, crtDirName, p);

    }

}
