/*
 * $Id: Main.java
 *
 * Copyright (C) 2003-2010, INRIA
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
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Properties;
import fr.inrialpes.exmo.ontowrap.jena25.JENAOntology;
import org.semanticweb.owl.align.Alignment;

public class Main {

      //load ontology
    public static OntModel loadOntology ( String fileName ) {
        InputStream in = FileManager.get().open( fileName );
        OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        model.read(in, null);
        return model;
    }

    //write ontology
    public static void writeOntology(OntModel model, String dest) {
        try {
            File f = new File(dest);
            FileOutputStream fout = new FileOutputStream(f);
            Charset defaultCharset = Charset.forName("UTF8");
            RDFWriter writer = model.getWriter("RDF/XML-ABBREV");
            writer.setProperty("showXmlDeclaration","true");
            writer.write(model.getBaseModel(), new OutputStreamWriter(fout, defaultCharset), "");
            fout.close();
        } catch (Exception ex) {
            System.out.println("Exception " + ex.getMessage());
        }
    }


    public static void printUsage() {
        System.out.println( "inputOntology outputOntology parameters" );
        System.out.println( "[--------------------------------------------------------------------------]" );
        System.out.println( "[------------- The list of all modification is the following --------------]" );

        System.out.println( "[1. Remove percentage subclasses       \"removeSubClass\"    --------------]" );
        System.out.println( "[2. Remove percentage properties       \"removeProperty\"    --------------]" );
        System.out.println( "[3. Remove percentage comments         \"removeComment\"     --------------]" );
        System.out.println( "[4. Remove percentage restrictions     \"removeRestriction\" --------------]" );
        System.out.println( "[5. Add percentage subclasses          \"addSubClass\"       --------------]" );
        System.out.println( "[6. Add percentage properties          \"addProperty\"       --------------]" );
        System.out.println( "[7. Rename percentage classes          \"renameClasses\"     --------------]" );
        System.out.println( "[8. Rename percentage properties       \"renameProperties\"  --------------]" );

        System.out.println( "[9. Remove all the classes from a level\"removeClasses\"    ---------------]" );
        System.out.println( "[10. Add nbClasses to a specific level \"addClasses\"       ---------------]" );
        System.out.println( "[11. Level flattened                   \"levelFlattened\"   ---------------]" );
        System.out.println( "[12. Remove individuals                \"removeIndividuals\"   ------------]" );
        //noHierarchy
        System.out.println( "[--------------------------------------------------------------------------]" );
        System.exit(-1);
    }

    public static void main (String [] args) {
        String fileName = "", destFile = "";
        Properties parameters;

        if ( args.length < 2 ) {
            System.out.println("Usage");
            printUsage();
        }
        else {
            System.out.println( "Input ontology:  [" + args[0] + "]" );
            System.out.println( "Output ontology: [" + args[1] + "]" );

            fileName = args[0];
            destFile = args[1];
            parameters = new Properties();                                      //initialize the parameters

            for ( int i=2; i<args.length; i+=2 ) {
                if ( args[i].equals("addSubClass") )                            /* add percentage classes */
                    parameters.setProperty(ParametersIds.ADD_SUBCLASS, args[i+1]);

                //add c classes beginning from level l -> the value of this parameters should be:
                //beginning_level.number_of_classes_to_add
                if ( args[i].equals("addClasses") )                             /* add c classes beginning from level l */
                    parameters.setProperty(ParametersIds.ADD_CLASSES, args[i+1]);

                if ( args[i].equals("removeSubClass") )                         /* remove percentage classes */
                    parameters.setProperty(ParametersIds.REMOVE_SUBCLASS, args[i+1]);

                if ( args[i].equals("removeClasses") )                          /* remove classes from level */
                    parameters.setProperty(ParametersIds.REMOVE_CLASSES, args[i+1]);

                if ( args[i].equals("addProperty") )                            /* add percentage properties */
                    parameters.setProperty(ParametersIds.ADD_PROPERTY, args[i+1]);

                if ( args[i].equals("removeProperty") )                         /* remove percentage properties */
                    parameters.setProperty(ParametersIds.REMOVE_PROPERTY, args[i+1]);

                if ( args[i].equals("renameProperties") )                       /* rename percentage properties */
                    parameters.setProperty(ParametersIds.RENAME_PROPERTIES, args[i+1]);

                if ( args[i].equals("removeComment") )                          /* remove percentage comments */
                    parameters.setProperty(ParametersIds.REMOVE_COMMENT, args[i+1]);

                if ( args[i].equals("levelFlattened") )                         /* flattened level */
                    parameters.setProperty(ParametersIds.LEVEL_FLATTENED, args[i+1]);

                if ( args[i].equals("renameClasses") )                          /* rename percentage classes */
                    parameters.setProperty(ParametersIds.RENAME_CLASSES, args[i+1]);

                if ( args[i].equals("renameResources") )                        /* rename percentage resources */
                    parameters.setProperty(ParametersIds.RENAME_RESOURCES, args[i+1]);

                if ( args[i].equals("removeRestriction") )                      /* remove percentage restrictions */
                    parameters.setProperty(ParametersIds.REMOVE_RESTRICTION, args[i+1]);

                if ( args[i].equals("removeIndividuals") )                      /* remove percentage individuals */
                    parameters.setProperty(ParametersIds.REMOVE_INDIVIDUALS, args[i+1]);

                if ( args[i].equals( ("noHierarchy")) )                         /* no hierarchy */
                    parameters.setProperty( ParametersIds.NO_HIERARCHY, ParametersIds.NO_HIERARCHY);
            }

            try {
                OntModel model = loadOntology( fileName );                      //load the initial ontology
                TestGenerator t = new TestGenerator();                          //build an instance of TestGenerator
                JENAOntology onto = new JENAOntology();                         //cast the model into Ontology
                onto.setOntology( (OntModel)model );

                Alignment align = t.generate(onto, parameters);                 //generate the alignment
                
                JENAOntology modified = (JENAOntology)t.getModifiedOntology();  //get the modified ontology
                if ( modified.getOntology() instanceof OntModel ) 
                    writeOntology(modified.getOntology(), destFile);            //write the ontology

                /* Prints the reference alignment
                OutputStream stream = new FileOutputStream( "fancyAlignment.rdf" );
                // Outputing
                PrintWriter  writer = new PrintWriter (
                                        new BufferedWriter(
                                            new OutputStreamWriter( stream, "UTF-8" )), true);
                AlignmentVisitor renderer = new RDFRendererVisitor( writer );
                align.render(renderer);
                writer.flush();
                writer.close();
                */
            }
            catch ( Exception ex ) {
                System.err.println( "Error " + ex.getMessage()  );

            }

            System.out.println( "***" );
            System.out.println( "END" );
            System.out.println( "***" );
        }
    }


}
