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

//Alignment API classes
import org.semanticweb.owl.align.Alignment;

// Alignment API implementation classes
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.service.jade.messageontology.Parameter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class TestGenerator {

	//load ontology
	public OntModel loadOntology ( String fileName ) {
		InputStream in = FileManager.get().open( fileName );
		OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
		model.read(in, null);
		return model;
	}

	//write ontology
	public void writeOntology(OntModel model, String dest) {
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

	/*
	 * FileName -> the name of the Ontology
	 * GeneratorParameters -> the list of parameters
	 */
	public static void main (String [] args) {
		TestGenerator t = new TestGenerator();
		String fileName = "", destFile = "";
		//String fileName = "onto.rdf";
		//String destFile = "onto1.rdf";
		GeneratorParameters parameters = new GeneratorParameters();

		if ( args.length < 2 ) {
			System.out.println("Usage");
			printUsage();
		}
		else {
			System.out.println( args[0] );
			System.out.println( args[1] );

			fileName = args[0];
			destFile = args[1];
			parameters = new GeneratorParameters();		//initialize the parameters

			for ( int i=2; i<args.length; i+=2 ) {
				if ( args[i].equals("addSubClass") )	/* add percentage classes */
					parameters.setParameter(ParametersIds.ADD_SUBCLASS, args[i+1]);
				//add c classes beginning from level l -> the value of this parameters should be:
				//beginning_level.number_of_classes_to_add
				if ( args[i].equals("addClasses") ) 		/* add c classes beginning from level l */
					parameters.setParameter(ParametersIds.ADD_CLASSES, args[i+1]);

				if ( args[i].equals("removeSubClass") )	/* remove percentage subclasses */
					parameters.setParameter(ParametersIds.REMOVE_SUBCLASS, args[i+1]);

				if ( args[i].equals("removeClasses") )	/* remove classes from level */
					parameters.setParameter(ParametersIds.REMOVE_CLASSES, args[i+1]);

				if ( args[i].equals("addProperty") )	/* add percentage properties */
					parameters.setParameter(ParametersIds.ADD_PROPERTY, args[i+1]);

				if ( args[i].equals("removeProperty") )	/* remove percentage properties */
					parameters.setParameter(ParametersIds.REMOVE_PROPERTY, args[i+1]);

				if ( args[i].equals("renameProperties") )/* rename percentage properties */
					parameters.setParameter(ParametersIds.RENAME_PROPERTIES, args[i+1]);

				if ( args[i].equals("removeComment") )	/* remove percentage comments */
					parameters.setParameter(ParametersIds.REMOVE_COMMENT, args[i+1]);

				if ( args[i].equals("levelFlattened") )	/* flattened level */
					parameters.setParameter(ParametersIds.LEVEL_FLATTENED, args[i+1]);

				if ( args[i].equals("renameClasses") )	/* rename percentage classes */
					parameters.setParameter(ParametersIds.RENAME_CLASSES, args[i+1]);

				if ( args[i].equals("renameResources") )/* rename percentage resources */
					parameters.setParameter(ParametersIds.RENAME_RESOURCES, args[i+1]);

				if ( args[i].equals("removeRestriction") ) /* remove percentage restrictions */
					parameters.setParameter(ParametersIds.REMOVE_RESTRICTION, args[i+1]);

                                if ( args[i].equals("removeIndividuals") ) /* remove percentage individuals */
					parameters.setParameter(ParametersIds.REMOVE_INDIVIDUALS, args[i+1]);
                                if ( args[i].equals( ("noHierarchy")) )     /* no hierarchy */
                                        parameters.setParameter( ParametersIds.NO_HIERARCHY , null);
			}

			//load the model
			OntModel model = t.loadOntology( fileName );				//the initial Ontology
			OntModel modifiedModel = t.loadOntology( fileName );			//the modified Ontology
			Alignment alignment  = new URIAlignment();				//the initial Alignment
			//build the ontology modifier for the first time
			OntologyModifier modifier = new OntologyModifier( model, modifiedModel, alignment);
                        modifier.initializeProperties();                                        //initialize the reference alignment
			//get the max level of the class hierarchy of the ontology
			int level = modifier.getMaxLevel();

			System.out.println( "[-------------------------------------------------]" );
			for ( int i=0; i<parameters.size(); i++ ) {
				Parameter p = parameters.getParameter(i);			//the parameter at position index
                                modifier.modifyOntology( p );					//modify the ontology according to parameter p
				System.out.println( "[We-modified-the-ontology-for-parameter " + p.getName() + "]");
			}
			System.out.println( "[-------------------------------------------------]" );

                        modifier.computeAlignment( "referenceAlignment.rdf" );
			alignment = modifier.getAlignment();			//get the reference alignment
			modifiedModel = modifier.getModifiedOntology();		//get the modified ontology
                        t.writeOntology( modifiedModel, destFile );		//write the model

			System.out.println( "***" );
			System.out.println( "END" );
			System.out.println( "***" );
		}
        }


}