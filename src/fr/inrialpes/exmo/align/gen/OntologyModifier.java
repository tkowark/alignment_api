package fr.inrialpes.exmo.align.gen;

//Java classes
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

//Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.service.jade.messageontology.Parameter;
/*
//Google API classes
import com.google.api.GoogleAPI;
import com.google.api.translate.Language;
import com.google.api.translate.Translate;
*/

//JENA classes
import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.SomeValuesFromRestriction;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

//WordNet API classes
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;


//activeRandomString is true -> we replace the label with a random string
//activeTranslateString is true -> we translate the label

/*
* This class receives as input two ontologies (the original ontology, the ontology that must be modified),
* an alignment and a parameter with the modification that must be applied to the input ontology.
* After the modification of the initial ontology the alignment must be computed
* The file in which we store the alignment is "referenceAlignment.rdf"
*/

public class OntologyModifier {
	private ClassHierarchy classHierarchy;						//the class hierarchy
	private OntModel model;                                                         //the model - the original Ontology
	private OntModel modifiedModel;							//the modified 	Ontology
	private String namespace;							//the Namespace
	private String namespaceNew;
        private Parameter parameter;							//the parameter for which we must apply the changes
	private Alignment alignment;							//the alignment of the two Ontologies
	private Properties params;							//the alignment
	private boolean isBuild;							//keep track if the class hierarchy is build
	private boolean isAlign;							//keep track it the initial alignment has already been computed
	private boolean isChanged;                                                      //keep track if the namespace of the new ontology is changed
        public static String fileName = "referenceAlignment.rdf";                       //the reference alignment

	//Ontology init, Ontology modified, Alignment align
	public OntologyModifier ( OntModel model, OntModel modifiedModel, Alignment alignment ) {
		this.model = model;
		this.modifiedModel = modifiedModel;
		this.alignment = alignment;
		this.parameter = null;
		this.isBuild = false;
		this.isAlign = false;
                this.isChanged = false;
		this.namespace = model.getNsPrefixURI("");
                this.namespaceNew = "";
		this.params = new Properties();
	}
	
	//no-args constructor
	public OntologyModifier () {	
	}
	
	//if we add / remove a class we need to keep track of the class hierarchy
	public void buildClassHierarchy () {
		this.classHierarchy = new ClassHierarchy();
		this.classHierarchy.buildClassHierarchy( this.modifiedModel );
		//this.classHierarchy.printClassHierarchy();
	}

	//generate random string with the length "length"
	public String getRandomString() {
		Random generator = new Random();
		String characters = "abcdefghijklmnopqrstuvwxyz";
		int length = characters.length();
		char[] text = new char[length];
		for (int i = 0; i < length; i++) {
			text[i] = characters.charAt( generator.nextInt(length) );
		}
		return new String(text).toUpperCase();
	}

	//remove spaces from a string
	public String removeSpaces ( String str ) {
		//return str.replaceAll("\\s+", "");
		if ( !str.contains( " " ) )
			return str;
		else {
			String aux = "", aux1="";
			int index;
			
			if ( str.contains( " " ) ) {
				while ( str.indexOf( " " ) != -1 ) {
					index = str.indexOf( " " ); 
					aux += str.substring( 0, index );
					aux1 = str.substring(index+2);
					str = str.substring(index+1, index+2).toUpperCase().concat( aux1 );
				}
				aux += str;
				return aux;
			}
		}
		return str;
	}

        public String translateString( String source ) {
	    return source;
	}


        /*
	//translate the string from English to French
	public String translateString( String source ) {
	    String translatedText = "";
	    GoogleAPI.setHttpReferrer("http://code.google.com/p/google-api-translate-java/");
	    //Translate.setHttpReferrer("http://code.google.com/p/google-api-translate-java/");
		try {
			translatedText = Translate.execute(source, Language.ENGLISH, Language.FRENCH);
		} catch (Exception e) {
			System.out.println( "Exception " + e.getMessage() );
		}
	    return removeSpaces ( translatedText );
	  }
         */
	
	//string to upperCase
	public String toUpperCase ( String source ) { 
		return source.toUpperCase();
	}
	
	//string to lowerCase
	public String toLowerCase ( String source ) { 
		return source.toLowerCase();
	}
	
	//synonym of the word
	public String getSynonym ( String source ) {
		String synonym = "";
		//set this variable according to your WordNet installation folder
		//see : http://lyle.smu.edu/~tspell/jaws/index.html
		System.setProperty("wordnet.database.dir", "/usr/Wordnet/WordNet-3.0/dict");
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset[] synsets = database.getSynsets( source );
		if (synsets.length > 0) {
			for (int i = 0; i < synsets.length; i++) {
				String[] wordForms = synsets[i].getWordForms();
				for (int j = 0; j < wordForms.length; j++) {
					if ( !wordForms[j].equals( source ) )	{
						synonym = removeSpaces ( wordForms[j] );
						return synonym;
					}
				} 
			}
		}
		else 
			return source;	
		return source;
	}
	
	//count - the number of elements from the vector
	//the random numElems that must be selected
	//use the Fisher and Yates method to shuffle integers from an array
	public int [] randNumbers (int count, int numElems) {
		int [] vect = new int[count];
		int [] n    = new int[numElems];
		int aux, rand;
		Random generator = new Random();
		
		for ( int i=0; i<count; i++ )			//fill the array with sorted elements
			vect[i] = i;
		for ( int j=0; j<numElems; j++ ) {
			rand = generator.nextInt( count-j );	//choose a random number from the interval
			n[j] = vect[rand];			//build the new vector
			aux = vect[rand];			//swap
			vect[rand] = vect[count-j-1];
			vect[count-j-1] = aux;
		}	/* for ( int j=0; j<numElems; j++ )	System.out.print( " [" + n[j] + "]" );*/
		return n;
	}
	
	//replace the label of the property
	public void replacePropertyLabel( String uri, String newLabel, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation ) {
		OntProperty prop = this.modifiedModel.getOntProperty( uri );
		if ( prop.getLabel( "en" ) != null ) {
			if ( activeTranslateString ) {	
				prop.setLabel( newLabel, "fr" );
			} else { 
				prop.setLabel( newLabel, "en" );
			}
		}
	}
	
	//get the URIs of the properties and their translation
	public HashMap<String, String> getPropertiesIdentifiers ( float percentage, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation) {
		HashMap<String, String> propertiesIdentifiers = new HashMap<String, String>();	//the HashMap of the properties identifiers
		List<OntProperty> properties = getOntologyProperties();                         //the list of all the properties
		List<String> propertiesName = new ArrayList<String>();                          //the properties identifiers
		List<OntProperty> propertiesTo = new ArrayList<OntProperty>();
		int nbProperties = properties.size();
		int toBeRenamed = (int)( percentage*nbProperties );
		//build the list of properties to be renamed
		int [] n = this.randNumbers(nbProperties, toBeRenamed);
		for ( int i=0; i<toBeRenamed; i++ ) {
			OntProperty p = properties.get(n[i]);
			propertiesTo.add(p);
		}
		
		for ( OntProperty prop : propertiesTo ) 
			if ( prop.getNameSpace().equals( this.namespace ) )
				propertiesName.add( prop.getLocalName() );
		
		for ( OntProperty prop : propertiesTo ) {
			String nameSpace = prop.getNameSpace();
			String localName = prop.getLocalName();
			//has the same Namespace as the Ontology Namespace
			if ( nameSpace.equals( this.namespace ) ) {
				if ( !propertiesIdentifiers.containsKey( localName ) ) {		
					if ( activeTranslateString ) { 			//replace the URI with the translated one
						String translateStrg = translateString( localName );
						propertiesIdentifiers.put( localName , translateStrg );
						replacePropertyLabel( prop.getURI(), translateStrg, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
						this.params.remove( prop.getURI() );
						this.params.put( prop.getURI() , this.namespace + translateStrg );	//the reference alignment
					}
					else if ( activeRandomString ) {		//replace the URI with a random string
						String newStrg = getRandomString();
						propertiesIdentifiers.put( localName , newStrg );
						replacePropertyLabel( prop.getURI(), newStrg, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
						this.params.remove( prop.getURI() );
						this.params.put( prop.getURI() , this.namespace + newStrg);		//the reference alignment
					}
					else if ( activeSynonym ) {			//replace the URI with a synonym
						String synonym = getSynonym( localName );
						if ( propertiesName.contains( synonym ) ) 
							propertiesIdentifiers.put( localName, localName );
						else  {
							propertiesIdentifiers.put( localName, synonym );
							replacePropertyLabel( prop.getURI(), synonym, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
							this.params.remove( prop.getURI() );
							this.params.put( prop.getURI(), this.namespace + synonym );	//the reference alignment
						}
					}
					else if ( activeStringOperation == 1 ) {        //replace the URI with the UpperCase URI
						propertiesIdentifiers.put( localName , toUpperCase( localName ) );
						replacePropertyLabel( prop.getURI(), toUpperCase( localName ), activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
						this.params.remove( prop.getURI() );
						this.params.put( prop.getURI(), this.namespace + toUpperCase( localName ) );	//the reference alignment
					}
					else if ( activeStringOperation == 2 ) {        //replace the URI with the LowerCase URI
						propertiesIdentifiers.put( localName , toLowerCase( localName ) );
						replacePropertyLabel( prop.getURI(), toLowerCase( localName ), activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
						this.params.remove( prop.getURI() );
						this.params.put( prop.getURI(), this.namespace + toLowerCase( localName ) );	//the reference alignment
					}
					else {
						propertiesIdentifiers.put( localName,  localName + "PROPERTY" );
						replacePropertyLabel( prop.getURI(), localName + "PROPERTY", activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
						this.params.remove( prop.getURI() );
						this.params.put( prop.getURI(), this.namespace + localName + "PROPERTY" );
					}
				}
			}
		}
		/*	for debugging
		System.out.println( "\n\nPROPERTIES_TRANSLATION" );
		System.out.println("percentage " + percentage );
		Set<String> e1 = propertiesIdentifiers.keySet();
		for ( Iterator it = e1.iterator(); it.hasNext(); ) {
			String key = (String)it.next();
			String value = propertiesIdentifiers.get( key );
			System.out.println( "key = [" + key + "][" + value + "]" );
		}
		System.out.println( "PROPERTIES_TRANSLATION\n\n" );
		*/
		return propertiesIdentifiers;
	}

	//replace the label of the class
	public void replaceClassLabel( String uri, String newLabel, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation ) {
		OntClass c = this.modifiedModel.getOntClass( uri );
		
		if ( c.getLabel( "en" ) != null ) {
			if ( activeTranslateString ) {	
				c.setLabel( newLabel, "fr" );
			} else  
				c.setLabel( newLabel, "en" );
		}
	}

	//get the URIs of the classes and their translation
	public HashMap<String, String> getClassesIdentifiers ( float percentage, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation ) {
		HashMap<String, String> classesIdentifiers = new HashMap<String, String>();	//the HashMap of classes identifiers
		List<OntClass> classes = this.getOntologyClasses();
		List<OntClass> classesTo = new ArrayList<OntClass>();
		int nbClasses = classes.size();
		int toBeRenamed = (int)( percentage*nbClasses );
		//build the list of classes to be renamed
		int [] n = this.randNumbers(nbClasses, toBeRenamed);
		for ( int i=0; i<toBeRenamed; i++ ) {
			OntClass cls = classes.get(n[i]);
			classesTo.add(cls);
		}
		
		for ( OntClass cls : classesTo ) {
			if ( !cls.isRestriction() ) {
				if ( !cls.isAnon() ) {
					String nameSpace = cls.getNameSpace();
					String localName = cls.getLocalName();
					
					//has the same Namespace as the Ontology Namespace
					if ( nameSpace.equals( this.namespace ) ) {
						if ( !classesIdentifiers.containsKey( localName ) ) {
							if ( activeTranslateString ) {			//replace the URI with the translated one
								String translateStrg = translateString ( localName );
								classesIdentifiers.put( localName , translateStrg );
								replaceClassLabel( cls.getURI(), translateStrg, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
								this.params.remove( cls.getURI() );
								this.params.put( cls.getURI() , this.namespace + translateStrg);	//the reference alignment
							}
							else if ( activeRandomString )	{		//replace the URI with a random string
								String newStrg = getRandomString();
								classesIdentifiers.put( localName , newStrg );
								this.params.remove( cls.getURI() );
								replaceClassLabel( cls.getURI(), newStrg, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
								//setProperty calls the Hashtable method put.
								this.params.put( cls.getURI(), this.namespace + newStrg );			//the reference alignment
							}
							else if ( activeSynonym ) {			//replace the URI with a synonym
								String synonym = getSynonym( localName );
								classesIdentifiers.put( localName, synonym );
								this.params.remove( cls.getURI() );
								replaceClassLabel( cls.getURI(), synonym, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
								this.params.put( cls.getURI(), this.namespace + synonym );			//the reference alignment
							}
							else if ( activeStringOperation == 1 ){         //replace the URI with the UpperCase URI
								classesIdentifiers.put( localName , toUpperCase( localName ) );
								this.params.remove( cls.getURI() );
								replaceClassLabel( cls.getURI(), toUpperCase(localName), activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
								this.params.put( cls.getURI(), this.namespace + toUpperCase( localName ) );	//the reference alignment
							}
							else if ( activeStringOperation == 2 ){         //replace the URI with the LowerCase URI
								classesIdentifiers.put( localName , toLowerCase( localName ) );
								this.params.remove( cls.getURI() );
								replaceClassLabel( cls.getURI(), toLowerCase(localName), activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
								this.params.put( cls.getURI(), this.namespace + toLowerCase( localName ) ); //the reference alignment
							}
							else {
								classesIdentifiers.put( localName, localName + "CLASS" );
								this.params.remove( cls.getURI() );
								replaceClassLabel( cls.getURI(), localName + "CLASS", activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
								this.params.put( cls.getURI(), this.namespace + localName + "CLASS" );
							}
						}
					}
				}
			}
		}
		return classesIdentifiers;
	}
	
	//rename percentage properties and classes 
	//activeProperties -> if true, then rename properties
	//activeClasses -> if true, then rename classes
	public OntModel renameResource ( boolean activeProperties, boolean activeClasses, float percentage, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation) {
		List<Statement> statements = null;				//the list of all statements
		HashMap<String, String> propertiesIdentifiers = null;           //the HashMap of the properties identifiers
		HashMap<String, String> classesIdentifiers = null;		//the HashMap of the classes identifiers
		String subjectLocalName,   subjectNameSpace;
		String predicateLocalName, predicateNameSpace;
		String objectLocalName,    objectNameSpace;
		boolean isPred, isSubj, isObj;
		isPred = isSubj = isObj = false;
		OntModel newModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);		//create new Model
		//get properties and classes identifiers
		if ( activeProperties )
			propertiesIdentifiers = getPropertiesIdentifiers( percentage, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation);
		if ( activeClasses )
			classesIdentifiers    = getClassesIdentifiers   ( percentage, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation);
		statements = this.modifiedModel.listStatements().toList();//get all the statements of the model
	
		//iterate and modify the identifiers
		for ( Statement stm : statements ) {
			Resource subject   = stm.getSubject();		//the subject
			Property predicate = stm.getPredicate();	//the predicate
			RDFNode object     = stm.getObject();		//the object
			Resource subj = null;
			Property pred = null;
			Resource obj  = null;
			isPred = isSubj = isObj = false;
			//if it is the subject of the statement
			if ( subject.getLocalName() != null ) {		
				if ( activeProperties ) {
				if ( propertiesIdentifiers.containsKey( subject.getLocalName() ) ) {
						//if the namespace of the subject is the same as the namespace of the property identifier
						if ( subject.getNameSpace().equals( this.namespace ) ) {	//that we want to remove
							isSubj = true;
							subjectNameSpace = subject.getNameSpace();
							subjectLocalName = subject.getLocalName();
							subj = newModel.createResource( subjectNameSpace + propertiesIdentifiers.get( subjectLocalName ) );
						}
					} 
				}
				
				if ( activeClasses ) {
					if ( classesIdentifiers.containsKey( subject.getLocalName() ) ) {
							//if the namespace of the subject is the same as the namespace of the property identifier
							//that we want to remove
							if (subject.getNameSpace().equals( this.namespace ) ) {
								isSubj = true;
								subjectNameSpace = subject.getNameSpace();
								subjectLocalName = subject.getLocalName();
								subj = newModel.createResource( subjectNameSpace + classesIdentifiers.get( subjectLocalName ) );
							}
						}
				}
			}
			//if it is the predicate of the statement
			if ( activeProperties ) {
				if ( propertiesIdentifiers.containsKey( predicate.getLocalName() ) ) {
					//if the namespace of the predicate is the same as the namespace of the property identifier
					//that we want to remove
					if ( predicate.getNameSpace().equals( this.namespace ) ) {	
						isPred = true;
						predicateNameSpace = predicate.getNameSpace();
						predicateLocalName = predicate.getLocalName();
						pred = newModel.createProperty(predicateNameSpace, propertiesIdentifiers.get( predicateLocalName ) );
					}
				}
			}
			
			if ( activeClasses ) {
				if ( classesIdentifiers.containsKey( predicate.getLocalName() ) ) {
					//if the namespace of the predicate is the same as the namespace of the property identifier
					//that we want to remove
					if ( predicate.getNameSpace().equals( this.namespace ) ) {
						isPred = true;		
						predicateNameSpace = predicate.getNameSpace();
						predicateLocalName = predicate.getLocalName();
						pred = newModel.createProperty(predicateNameSpace, classesIdentifiers.get( predicateLocalName ) );
					}
				}	
			}
			//if it is the object of the statement
			if ( object.canAs( Resource.class ) )
				if ( object.isURIResource() ) {
					if ( activeProperties ) {
						if ( propertiesIdentifiers.containsKey( object.asResource().getLocalName() ) ) {
							//if the namespace of the object is the same as the namespace of the property identifier
							//that we want to remove
							if ( object.asResource().getNameSpace().equals( this.namespace ) ) {
								isObj = true;
								objectNameSpace = object.asResource().getNameSpace();
								objectLocalName = object.asResource().getLocalName();
								obj = newModel.createResource(objectNameSpace + propertiesIdentifiers.get( objectLocalName ) );
							}
						}
					}
					
					if ( activeClasses ) {
						if ( classesIdentifiers.containsKey( object.asResource().getLocalName() ) ) {
							//if the namespace of the object is the same as the namespace of the property identifier
							//that we want to remove
							if ( object.asResource().getNameSpace().equals( this.namespace ) ) {
								isObj = true;
								objectNameSpace = object.asResource().getNameSpace();
								objectLocalName = object.asResource().getLocalName();
								obj = newModel.createResource(objectNameSpace + classesIdentifiers.get( objectLocalName ) );
							}
						}
					}
				}
			
			if ( isSubj ) {
				if ( isPred ) {
					if ( isObj )
						newModel.add( subj, pred, obj );
					else
						newModel.add( subj, pred, object );
				}
				else {
					if ( isObj )
						newModel.add( subj, predicate, obj );
					else 
						newModel.add( subj, predicate, object );
				}
			} else {
				if ( isPred ) {
					if ( isObj )
						newModel.add( subject, pred, obj );
					else
						newModel.add( subject, pred, object );
				}
				else {
					if ( isObj )
						newModel.add( subject, predicate, obj );
					else
						newModel.add( subject, predicate, object );
				}
			}
		}
		return newModel;
	}
		
	//check if the class hierarchy is build
	public void checkClassHierarchy() { 
		if ( !this.isBuild ) {
			buildClassHierarchy();
			this.isBuild = true;
		}
	 }
	
	//get the Ontology classes
	@SuppressWarnings("unchecked")
	public List<OntClass> getOntologyClasses () {
		List<OntClass> classes = new ArrayList<OntClass>();
		for ( Iterator it = this.modifiedModel.listNamedClasses(); it.hasNext(); ) {
			OntClass aux = (OntClass)it.next();
			if ( ( aux ).getNameSpace().equals( this.namespace ) ) {
				classes.add( aux );
			}
		}
		return classes;
	}
	
	//get the Ontology properties
	@SuppressWarnings("unchecked")
	public List<OntProperty> getOntologyProperties () {
		List<OntProperty> properties = new ArrayList<OntProperty>();
		for ( Iterator it = this.modifiedModel.listAllOntProperties(); it.hasNext(); ) {
			OntProperty prop = (OntProperty)it.next();
			if ( prop.getNameSpace().equals( this.namespace ) ) 
				properties.add( prop );
		}
		return properties;
	}
	
	//adds a class with a random URI to the parent class parentURI
	public OntClass addClass ( OntClass parentClass, String childURI ) {
		OntClass childClass = this.modifiedModel.createClass( this.namespace + childURI );			//create a new class to the model
		this.classHierarchy.addClass( this.namespace + childURI, parentClass.getURI() );			//add the node in the hierarchy of classes
		parentClass.addSubClass( childClass );														//add the childClass as subclass of parentClass
		this.modifiedModel.add( childClass, RDFS.subClassOf, parentClass );					//add the class to the model
		return childClass;
	}
		
	//add the percentage of the specified subclasses
	public void addSubClasses ( float percentage ) {
		List<OntClass> classes = getOntologyClasses(); 	//get the list of classes from the Ontology
		int nbClasses = classes.size();					//number of classes from the Ontology
		int toAdd = (int) ( percentage * nbClasses );

		checkClassHierarchy();							//check if the classHierarchy is built		
		//build the list of properties to be renamed
		int [] n = this.randNumbers(nbClasses, toAdd);
		for ( int i=0; i<toAdd; i++ ) {
			String childURI = this.getRandomString();	//give a random URI to the new class
			addClass( classes.get(n[i]), childURI );
		}
	}
	
	//add nbClasses beginning from level
	public void addClasses ( int level, int nbClasses ) {
		String classURI = this.getRandomString();		//give a random URI to the new class
		//the parent class -> if level is 1 then we create a new class
		//else we get a random class from the level : level-1 to be the parent of the class
		OntClass parentClass;
		OntClass childClass;
		checkClassHierarchy();		//check if the classHierarchy is built
		if ( level == 1 ) {			//the parent of the class is Thing, we add the class and then the rest of the classes
			parentClass = this.modifiedModel.createClass( this.namespace + classURI );	//create a new class to the model
			this.classHierarchy.addClass( this.namespace + classURI, "Thing" );	//add the node in the hierarchy of classes
		}
		else {
			parentClass = this.classHierarchy.getRandomClassFromLevel( this.modifiedModel, level-1 );
			childClass = addClass ( parentClass, classURI );
			parentClass = childClass;
		}
		
		for ( int i=level+1; i<level + nbClasses; i++ ) {
			classURI = "IS_" + classURI;
			childClass = addClass (parentClass, classURI);
			parentClass = childClass;
		}	//this.classHierarchy.printClassHierarchy();
	}
	
	//the class to be removed appears in the domain / range of the property -> change with the parent class
	@SuppressWarnings("unchecked")
	public void changeDomainRange ( OntClass child, OntClass parent ) {
		boolean isDomain, isRange;
		List<OntProperty> properties = this.modifiedModel.listAllOntProperties().toList();//get the list of all the model properties
		String URI = child.getURI();
		
		for ( OntProperty prop : properties ) {
			isDomain = isRange = false;				
			if ( prop.isObjectProperty() ) {						//if the prop is ObjectProperty			
				if ( prop.hasDomain(null) ) {						//if it has domain
					OntResource res = prop.getDomain();				//the domain
					if ( res.canAs(UnionClass.class) ) {                            //the domain is a union of classes
						UnionClass uncls = res.as( UnionClass.class );
						for ( Iterator it = uncls.listOperands(); it.hasNext(); ) {
							OntClass aux = (OntClass)it.next();
							if ( aux.getURI().equals( URI ) )
								isDomain = true;
						}						
						if ( isDomain ) {					//if the domain is a union of classes
							uncls.removeOperand( child );			//remove the child from the union
							uncls.addOperand( parent );			//add the parent to the union
						}
					}
				}
			}
			if ( prop.isDatatypeProperty()  ) {						//if prop is DatatypeProperty
				if ( prop.hasDomain(null) ) {						//if it has domain
					Resource res = prop.getDomain();				//get the domain
					if ( res.canAs(UnionClass.class) ) {                            //if domain is a union of classes
						UnionClass uncls = res.as( UnionClass.class );	
						for ( Iterator it = uncls.listOperands(); it.hasNext(); ) {
							OntClass aux = (OntClass)it.next();
							if ( aux.getURI().equals( URI ) )
								isDomain = true;
						}
						if ( isDomain ) {					//if the domain is a union of classes
							uncls.removeOperand( child );			//remove the child class from the union
							uncls.addOperand( parent );			//add the parent class to the union
						}
					}
				}					
				if ( prop.hasRange(null) ) {						//if the prop has range		
					Resource res = prop.getRange();					//get the resource		
					if ( res.canAs(UnionClass.class) ) {    			//the range of property is a union of classes
						UnionClass uncls = res.as( UnionClass.class );
						for ( Iterator it = uncls.listOperands(); it.hasNext(); ) {
							OntClass aux = (OntClass)it.next();
							if ( aux.getURI().equals( URI ) ) {		
								isRange = true;
							}
						}			
						if ( isRange ) {					//if the range is a union of classes
							uncls.removeOperand( child );			//remove the child class from the union
							uncls.addOperand( parent );			//add the parent class to the union
						}
					}
				}	

			}
		}
	}
	 
	//check if the removed class appears as AllValueFrom or SomeValueFrom in a restriction
	@SuppressWarnings("unchecked")
	public void checkClassesRestrictions ( OntClass childClass, OntClass parentClass )  {
		 Restriction restr = null;
		 for ( Iterator it = this.modifiedModel.listRestrictions(); it.hasNext(); ) {
			 restr = (Restriction)it.next();					//get the restriction
			 if ( restr.isAllValuesFromRestriction() )  { 			
				 AllValuesFromRestriction av = restr.asAllValuesFromRestriction();
				 if ( av.getAllValuesFrom().getURI() != null )                  //if points to the childClass
					 if ( av.getAllValuesFrom().getURI().equals( childClass.getURI() ) ) 
						 av.setAllValuesFrom( parentClass );		//change to point to the parentClass
				 }
			 if ( restr.isSomeValuesFromRestriction() ) {
				 SomeValuesFromRestriction sv = restr.asSomeValuesFromRestriction();
				 if ( sv.getSomeValuesFrom().getURI() != null )                 //if points to the childClass
					 if ( sv.getSomeValuesFrom().getURI().equals( childClass.getURI() ) ) 
						 sv.setSomeValuesFrom( parentClass );		//change to point to the parentClass
			 }
		 }
	 }
	 
	//remove class
	@SuppressWarnings("unchecked")
	public void removeClass ( OntClass cls ) { 
		OntClass parentClass;
		ArrayList<OntClass> subClasses = new ArrayList<OntClass>();			//the list of all the subclasses of the class
		OntClass thing = this.modifiedModel.createClass( OWL.Thing.getURI() );		//Thing class
		checkClassHierarchy();								//check if the class hierarchy is built
		parentClass = this.classHierarchy.removeClass( this.modifiedModel, cls );	//get the parent of the class
		
		for (Iterator it1 = cls.listSubClasses(); it1.hasNext(); ) {                    //build the list of subclasses
			OntClass subCls = (OntClass)it1.next();					//because we can't change the 
			subClasses.add( subCls );						//model while we are iterating
		}
		
		if ( parentClass != thing )							//now we change the superclass of classes
			for (OntClass clss : subClasses) 					//new superclass =>
				clss.setSuperClass( parentClass );				//=>the superclass of the node
			
		changeDomainRange(cls, parentClass);						//change the domain and the range for each property
		checkClassesRestrictions( cls, parentClass );					//change the union of different classes
		this.params.remove( cls.getURI() );						//remove the URI of the class from the reference alignment
		cls.remove();									//remove the class from the Ontology		
	 }
	
	//remove the subClasses from the list
	public void removeSubClasses ( float percentage ) {
		List<OntClass> classes = this.getOntologyClasses();				//the list of classes from Ontologu
		List<OntClass> removedClasses = new ArrayList<OntClass>();
		int nbClasses = classes.size();							//number of classes
		checkClassHierarchy();								//check if the class hierarchy is built
		int toBeRemoved =  (int) (percentage*nbClasses);				//the number of classes to be removed
		
		//build the list of classes to be removed
		int [] n = this.randNumbers(nbClasses, toBeRemoved);
		for ( int i=0; i<toBeRemoved; i++ ) {
			OntClass cls = classes.get(n[i]);
			removedClasses.add( cls );
		}
		
		for ( OntClass cls : removedClasses ) {						//remove the classes from the list
			removeClass (cls);
		}	
	}

	//remove all the classes from a specific level
	public void removeClassesFromLevel ( int level ) {
		//System.out.println( "Level " + level );
		/*	if ( level == 1 )							//except classes from level 1
				return;	*/
		List<OntClass> classes = new ArrayList<OntClass>();
		checkClassHierarchy();								//check if the class hierarchy is built
		classes = this.classHierarchy.getClassesFromLevel(this.modifiedModel, level);
		for ( int i=0; i<classes.size(); i++ ) {                                        //remove the classes from the hierarchy
			removeClass ( classes.get(i) );						
		}
	}

	//remove percentage individuals from Ontology
	public void removeIndividuals( float percentage ) {
		boolean isSubj, isObj;								//the individual can appear as subject or object
		List<Individual> individuals = this.modifiedModel.listIndividuals().toList();
		List<Individual> individualsTo = new ArrayList<Individual>();                   //the list of individuals to be removed
		List<Statement> statements = this.modifiedModel.listStatements().toList();
		int nbIndividuals = individuals.size();						//the number of individuals
		int toBeRemoved = (int)( percentage*nbIndividuals );                            //the number of individuals to be removed
		
		int [] n = this.randNumbers(nbIndividuals, toBeRemoved);                        //build the list of individuals to be removed
		for ( int i=0; i<toBeRemoved; i++ ) {
			Individual indiv = individuals.get(n[i]);				//remove the individual from the reference alignment
			individualsTo.add( indiv );
			this.params.remove( indiv.getURI() );
		}
		
		for ( Statement st : statements ) {
			Resource subject   = st.getSubject();
			RDFNode object     = st.getObject();
			isSubj = isObj = false;
			
			if ( individualsTo.contains( subject ) )
				isSubj = true;
			if ( object.canAs( Resource.class ) )
				if ( individualsTo.contains( object.asResource() ) )
					isObj = true;
			if ( isSubj )	//the individual appears as subject in the statement
				this.modifiedModel.remove( st );
			if ( isObj )	//the individual appears as object in the statement
				this.modifiedModel.remove( st );
		}
	}	
	//remove properties from the model
	@SuppressWarnings("unchecked")
	public void removeProperties ( float percentage ) {
		List <OntProperty> properties = this.getOntologyProperties();                   //the list of all properties from the model
		ArrayList <OntProperty> propertiesToBeRemoved = new ArrayList<OntProperty>();
		ArrayList<Restriction> restrictions = new ArrayList<Restriction>();
		boolean isObj, isSubj, isPred;
		
		List<Statement> statements = this.modifiedModel.listStatements().toList();	//get all of the statements
		int nbProperties = properties.size();						//the number of properties
		int toBeRemoved = (int)( percentage*nbProperties );				//the number of properties to be removed		
		
		//build the list of classes to be removed
		int [] n = this.randNumbers(nbProperties, toBeRemoved);
		for ( int i=0; i<toBeRemoved; i++ ) {						//build the list of properties to be removed
			OntProperty p = properties.get(n[i]);
			propertiesToBeRemoved.add( p );
			this.params.remove( p.getURI() );
			for ( Iterator it = p.listReferringRestrictions(); it.hasNext();  ) {	//get the restrictions of that property
				restrictions.add( (Restriction)it.next() );
			}
			for ( Restriction r : restrictions )					//delete all the restrictions
				r.remove();
		}
		
		for ( Statement st : statements ) {						//remove the declaration of properties from the model
			Resource subject   = st.getSubject();
			Property predicate = st.getPredicate();
			RDFNode object     = st.getObject();
			isSubj = isPred = isObj = false;
			
			if ( propertiesToBeRemoved.contains( subject ) )			//if appears as subject
				if ( subject.getNameSpace().equals( this.namespace ) )
					isSubj = true;
			
			if ( propertiesToBeRemoved.contains( predicate ) )			//if appears as predicate
				if ( predicate.getNameSpace().equals( this.namespace ) )
					isPred = true;
			
			if ( object.canAs( Resource.class ) )					//if appears as object
				if ( propertiesToBeRemoved.contains( object ) )
					if ( object.asResource().getNameSpace().equals( this.namespace ) )
						isObj = true;
			
			if ( isSubj || isPred || isObj )					//remove the statement in which the prop
				this.modifiedModel.remove( st );				//appears as subject, predicate or object
		}
	}

	//add object properties to the Ontology
	public void addProperties ( float percentage ) {
		List<OntProperty> properties = this.getOntologyProperties();
		List<OntClass> classes = this.getOntologyClasses();
		ObjectProperty p = null;
		DatatypeProperty d = null;
		Random classRand = new Random();
		int index;	
		int nbClasses = classes.size();                                                 //the number of classes
		int nbProperties = properties.size();                                           //the number of properties
		int toBeAdd = (int)( percentage*nbProperties );                                 //the number of properties to be add
		
		for ( int i=0; i<toBeAdd/2; i++ ) {                                             //add object properties
			//p = this.modifiedModel.createObjectProperty( this.namespace + "OBJECT_PROPERTY_" + getRandomString() );
			p = this.modifiedModel.createObjectProperty( this.namespace + getRandomString() );
			index = classRand.nextInt( nbClasses );                                 //pick random domain
			p.addDomain( classes.get( index ) );
			index = classRand.nextInt( nbClasses );                                 //pick random range
			p.addRange( classes.get( index ) );
		}
			
		for ( int i=toBeAdd/2; i<toBeAdd; i++ ) {                                       //add datatype properties
			//d = this.modifiedModel.createDatatypeProperty( this.namespace + "DATATYPE_PROPERTY_" + getRandomString() );
			d = this.modifiedModel.createDatatypeProperty( this.namespace +  getRandomString() );
			index = classRand.nextInt( nbClasses );                                 //add domain
			d.addDomain( classes.get( index ) );
			d.addRange( XSD.xstring );						//add range -> string
		}
	}

	//remove classes comments
	@SuppressWarnings("unchecked")
	public void removeClassesComments ( float percentage ) {
		ArrayList<Literal> comments = new ArrayList<Literal>();
		List<OntClass> classes = this.modifiedModel.listNamedClasses().toList();
		ArrayList<OntClass> classesTo = new ArrayList<OntClass>();
		int nbClasses = classes.size();						
		int toBeRemoved = (int)( percentage * nbClasses );                              //number of classes comments to be removed
		
		int [] n = this.randNumbers(nbClasses, toBeRemoved);
		for ( int i=0; i<toBeRemoved; i++ ) {
			OntClass cls = classes.get(n[i]);
			classesTo.add( cls );
		}
		
		for ( OntClass c : classesTo ) {
			for (Iterator it2 = c.listComments(null); it2.hasNext();) 
				comments.add(((Literal) it2.next()));
			for (Literal lit : comments)                                            // remove comments
				c.removeComment( lit );
			comments.clear();
		}
	}

	//remove properties comments
	@SuppressWarnings("unchecked")
	public void removePropertiesComments ( float percentage ) {
		ArrayList<Literal> comments = new ArrayList<Literal>();                         // an array list to hold all the comments
		List<OntProperty> properties = this.modifiedModel.listAllOntProperties().toList();
		ArrayList<OntProperty> propertiesTo = new ArrayList<OntProperty>();
		int nbProperties = properties.size();					
		int toBeRemoved = (int)( percentage * nbProperties );                           //the number of properties comments to be removed
		
		int [] n = this.randNumbers(nbProperties, toBeRemoved);
		for ( int i=0; i<toBeRemoved; i++ ) {
			OntProperty p = properties.get(n[i]);
			propertiesTo.add( p );
		}
		
		for ( OntProperty prop : propertiesTo ) {
			for (Iterator it2 = prop.listComments(null); it2.hasNext();)            // get all comments
				comments.add(((Literal) it2.next()));
			for (Literal lit : comments) 						//remove comments
				prop.removeComment( lit );
			comments.clear();
		}
	}

	//remove individuals comments
	@SuppressWarnings("unchecked")
	public void removeIndividualsComments ( float percentage ) {
		ArrayList<Literal> comments = new ArrayList<Literal>();                         // an array list to hold all the comments
		List<Individual> individuals = this.modifiedModel.listIndividuals().toList();
		ArrayList<Individual> individualsTo = new ArrayList<Individual>();
		int nbIndividuals = individuals.size();					
		int toBeRemoved = (int)( percentage * nbIndividuals );                          //number of classes to be removed
		
		int [] n = this.randNumbers(nbIndividuals, toBeRemoved);
		for ( int i=0; i<toBeRemoved; i++ ) {
			Individual indiv = individuals.get(n[i]);
			individualsTo.add( indiv );
		}
		for ( Individual indiv : individuals ) {
			for (Iterator it2 = indiv.listComments(null); it2.hasNext(); )      	//get all comments
				comments.add( ((Literal) it2.next()) );
			for (Literal lit : comments )						//remove comments
				indiv.removeComment( lit );
			comments.clear();
		}
	}

	//remove Ontologies comments
	@SuppressWarnings("unchecked")
	public void removeOntologiesComments ( float percentage ) {
		ArrayList<Literal> comments = new ArrayList<Literal>();                         // an array list to hold all the comments
		List<Ontology> ontologies = this.modifiedModel.listOntologies().toList();
		ArrayList<Ontology> ontologiesTo = new ArrayList<Ontology>();
		int nbOntologies = ontologies.size();
		int toBeRemoved = (int)( percentage * nbOntologies );                           //the number of Ontologies comments to be removed
		
		int [] n = this.randNumbers(nbOntologies, toBeRemoved);
		for ( int i=0; i<toBeRemoved; i++ ) {
			Ontology onto = ontologies.get(n[i]);
			ontologiesTo.add( onto );
		}

		for ( Ontology onto : ontologies ) {
			for (Iterator it2 = onto.listComments(null); it2.hasNext(); )           // get all comments
				comments.add(((Literal) it2.next()));
			for ( Literal lit : comments )						//remove all comments
				onto.removeComment( lit );
			comments.clear();
		}
	}

	//remove percentage comments 
	public void removeComments ( float percentage ) {
		removeClassesComments ( percentage );
		removeIndividualsComments ( percentage );
		removePropertiesComments ( percentage );
		removeOntologiesComments ( percentage );
	}

	//flatten level
	public void levelFlattened ( int level ) {
		int size;
		boolean active = false;
		ArrayList<OntClass> levelClasses = new ArrayList<OntClass>();		//the list of classes from that level	
		ArrayList<OntClass> parentLevelClasses = new ArrayList<OntClass>();	//the list of parent of the child classes from that level
		ArrayList<OntClass> superLevelClasses = new ArrayList<OntClass>();	//the list of parent of the parent classes from that level
		if ( level == 1 )				//no change
			return;
		checkClassHierarchy();			//check if the class hierarchy is built
		active = this.classHierarchy.flattenClassHierarchy( this.modifiedModel, level, levelClasses, parentLevelClasses, superLevelClasses);	
		size = levelClasses.size();
		
		for ( int i=0; i<size; i++ ) {
			OntClass childClass = levelClasses.get( i );			//child class
			OntClass parentClass = parentLevelClasses.get( i );		//parent class	
			//all the classes are subclasses of owl: Thing
			if (  active ) {						//if ( !parentClass.getURI().equals( "Thing" ) ) {
				OntClass superClass = superLevelClasses.get( i );	//parent class of the child class parents
				childClass.addSuperClass( superClass );
				parentClass.removeSubClass( childClass );
			} else { 
				parentClass.removeSubClass( childClass );
			}	
		}				
	}
	
	//remove percentage restrictions from the model
	public void removeRestrictions( float percentage )  { 
		List<Restriction> restrictions   = new ArrayList<Restriction>();
		List<Restriction> restrictionsTo = new ArrayList<Restriction>();	//the array list of restrictions to be removed
		restrictions = this.modifiedModel.listRestrictions().toList();
		int nbRestrictions = restrictions.size();				//the number of restrictions
		int toBeRemoved = (int)( percentage*nbRestrictions );			//the number of restrictions to be removed

		int [] n = this.randNumbers(nbRestrictions, toBeRemoved);		//build the list of restrictions to be removed
		for ( int i=0; i<toBeRemoved; i++ ) {
			Restriction res = restrictions.get(n[i]);
			restrictionsTo.add( res );
		}
		
		for ( Restriction res : restrictionsTo )
			res.remove();
	}

	//the initial reference alignment
	public void initializeProperties() {	
		List<OntClass> classes       = this.modifiedModel.listNamedClasses().toList();		//all classes
		List<OntProperty> properties = this.modifiedModel.listAllOntProperties().toList();	//all properties
		List<Individual> individuals = this.modifiedModel.listIndividuals().toList();		//all individuals
		List<Ontology> ontologies    = this.modifiedModel.listOntologies().toList();		//all Ontologies
		
		this.isAlign = true;									//the alignment has been computed for the first time
		
		for ( OntClass cls : classes )                                                          //list all classes
			if ( cls.getNameSpace().equals( this.namespace ) )
				this.params.put( cls.getURI() , cls.getURI() );	//add them to the initial alignment
		
		for ( OntProperty prop : properties )				//list all properties
			if ( prop.getNameSpace().equals( this.namespace ) )
				this.params.put( prop.getURI() , prop.getURI() );//add them to the initial alignment
		
		for ( Individual indiv : individuals )	{			//list all individuals
			if ( indiv.getURI() != null ) { 
			//System.out.println( "[" + indiv.getURI() + "]" );
				this.params.put( indiv.getURI() , indiv.getURI() );//add them to the initial alignment
			}
		}
		/*
		for ( Ontology onto : ontologies )								//list all ontologies
			if ( onto.getNameSpace().equals( this.namespace ) )
				this.params.put( onto.getURI() , onto.getURI() );//add them to the initial alignment
		*/
	}
	
	//compute the alignment after the modifications
	@SuppressWarnings("unchecked")
	public void computeAlignment( String fileName ) {
		URI onto1 = null;
		URI onto2 = null;

                System.out.println("Namespace = " + this.namespace);

                if ( !this.isChanged )      //if the namespace of the ontology is not changed
                    this.namespaceNew = "http://oaei.ontologymatching.org/2010/benchmarks/101/onto1.rdf#";

		try {
			onto1 = new URI ( this.namespace );
			onto2 = new URI ( this.namespaceNew );
			
			this.alignment.init(onto1, onto2);
			
			long time = System.currentTimeMillis();
			
			//Cell addAlignCell(Object ob1, Object ob2, String relation, double measure) throws AlignmentException {
			//System.out.println( "\nPARAMS" );
			Set<Object> e1 = params.keySet();
			for ( Iterator it = e1.iterator(); it.hasNext(); ) {
				String key = (String)it.next();
				String value = (String)params.get( key );
                		URI uri1 = URI.create( key );
                                //the namespace of value
                                //System.out.println( "key = [" + key + "]");	//System.out.println(	"value = [" + value + "]" );

                                String newValue = null;
                                URI uri2 = URI.create( value );
                                if ( value.contains( this.namespace ) )
                                {
                                    newValue = value.substring( this.namespace.length() );
                                    uri2 = URI.create( this.namespaceNew + newValue );
                                }
                                else {
                                    newValue = value;
                                    uri2 = URI.create( newValue );
                                }
				this.alignment.addAlignCell( uri1, uri2, "=", 1 );
			}

		    long newTime = System.currentTimeMillis();
		    this.alignment.setExtension( Namespace.ALIGNMENT.uri, Annotations.TIME, Long.toString(newTime - time) );

			OutputStream stream ;//= new FileOutputStream(filename);
			if ( fileName == null ) {
				stream = System.out;
			} else {
				stream = new FileOutputStream( fileName );
			}
			
			// Outputing
		    PrintWriter  writer = new PrintWriter (
					  	new BufferedWriter(
                                                    new OutputStreamWriter( stream, "UTF-8" )), true);
		    AlignmentVisitor renderer = new RDFRendererVisitor( writer );
		    this.alignment.render(renderer);
		    writer.flush();
		    writer.close();
		} catch (AlignmentException aex)  { System.out.println( "Exception " + aex.getMessage() );
		} catch (Exception ex) {  System.err.println("Exception " + ex.getMessage());
		}
	}

	//change the namespace of the modified ontology
	public OntModel changeNamespace () {
		List<Statement> statements = this.modifiedModel.listStatements().toList();	//the statements of the model
		String newNamespace;
		
		//newNamespace = this.namespace.substring(0, this.namespace.length()-1 ) + "1#";
		newNamespace = "http://oaei.ontologymatching.org/2010/benchmarks/101/onto1.rdf#";
                this.namespaceNew = newNamespace;
                this.isChanged = true;
		boolean isSubj, isPred, isObj;
		
		OntModel newModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );	//create new Model
		
		//iterate through all the statements and change the namespace
		for ( Statement stm : statements ) {
			Resource subject   = stm.getSubject();		//the subject
			Property predicate = stm.getPredicate();	//the predicate
			RDFNode object     = stm.getObject();		//the object
			
			Resource subj = null;
			Property pred = null;
			Resource obj  = null;
			
			isPred = isSubj = isObj = false;
			
			if ( subject.getLocalName() != null )
				if ( !subject.isLiteral() )
					if ( subject.getNameSpace().equals( this.namespace ) ) {	//System.out.print("[s][" + subject.getLocalName() + "]");
						subj = newModel.createResource( newNamespace + subject.getLocalName() );
						isSubj = true;
					}
			
			if ( !object.isLiteral() )
				if ( object.canAs( Resource.class ) )
					if ( object.isURIResource() ) 
						if ( object.asResource().getNameSpace().equals( this.namespace ) ) {
						//System.out.print("[o][" + object.asResource().getLocalName() + "]");
						obj = newModel.createResource( newNamespace + object.asResource().getLocalName() );
						isObj = true;
				}
			
			if ( !predicate.isLiteral() )
			if ( predicate.getNameSpace().equals( this.namespace ) ) {
				//System.out.print( "[p][" + predicate.getLocalName() + "]" );
				pred = newModel.createProperty( newNamespace + predicate.getLocalName() );
				isPred = true;
			}
				
			//System.out.println();
			
			if ( isSubj ) {
				if ( isPred ) {
					if ( isObj )
						newModel.add( subj, pred, obj );
					else
						newModel.add( subj, pred, object );
				}
				else {
					if ( isObj )
						newModel.add( subj, predicate, obj );
					else 
						newModel.add( subj, predicate, object );
				}
			} else {
				if ( isPred ) {
					if ( isObj )
						newModel.add( subject, pred, obj );
					else
						newModel.add( subject, pred, object );
				}
				else {
					if ( isObj )
						newModel.add( subject, predicate, obj );
					else
						newModel.add( subject, predicate, object );
				}
			}
		}
				
		//rename the namespace of owl:Ontology
		List<Ontology> ontos = newModel.listOntologies().toList();	
		for ( Ontology o : ontos ) {
			ResourceUtils.renameResource( o, newNamespace );
		}
		
		//this.namespace = newNamespace;						//change the namespace
		return newModel;	
	}
	
	//returns the modified ontology after changing the namespace
	public OntModel getModifiedOntology () { 
		System.out.println( "->change namespace" );
		if ( !this.isChanged )
                    this.modifiedModel = changeNamespace();					//change the namespace of the modified ontology
		System.out.println( "->namespace changed" );
		return this.modifiedModel;
	}	
	
	//returns the alignment 
	public Alignment getAlignment() {
		return this.alignment;
	}
	
	//must have the max level of the class hierarchy
	public int getMaxLevel() {
		checkClassHierarchy();			//check if the class hierarchy is built
		//System.out.println("\n***\n");
		//System.out.println( "THE MAX DEPTH OF THE CLASS HIERARCHY" );
		System.out.println( "MaxLevelOfClassHierarchy = [" + this.classHierarchy.getMaxLevel() + "]" );
		//System.out.println( "THE MAX DEPTH OF THE CLASS HIERARCHY" );
		//System.out.println("\n***\n");
		return this.classHierarchy.getMaxLevel();
			
	}
	
	//OntModel model, OntModel modifiedModel, Alignment alignment
	public void modifyOntology( Parameter p ) {
		this.parameter = p;                                     //set the parameter
		float value = 0.0f;
		String name, aux = "";
		
		if ( this.parameter == null ) {
			System.out.println( "No parameter" );		//no parameter as input
		}
		else {
			name = this.parameter.getName();		//the name of the parameter
					
			if ( this.parameter.getValue() != null )	//the value of the parameter
				value = Float.valueOf( this.parameter.getValue() ).floatValue();
			else 
				aux = this.parameter.getValue();
			
			if ( !this.isAlign ) {                          //if the initial hashtable for the alignment is not computed
				initializeProperties();                 //determine the elements from the initial reference alignment
			}
			
				//add percentage classes
			if ( name.equals( ParametersIds.ADD_SUBCLASS ) ) {
				System.out.println( "Add Class" + "[" + value + "]");
				addSubClasses( value );
			}	//remove percentage classes
			if ( name.equals( ParametersIds.REMOVE_SUBCLASS ) ) {
				System.out.println( "Remove Class" + "[" + value + "]");
				removeSubClasses( value );
			}	//remove percentage comments
			if ( name.equals( ParametersIds.REMOVE_COMMENT ) ) {
				System.out.println( "Remove Comments" + "[" + value + "]");
				removeComments ( value );
			}	//remove percentage properties
			if ( name.equals( ParametersIds.REMOVE_PROPERTY ) ) {
				System.out.println( "Remove Property" + "[" + value + "]");
				removeProperties ( value );
			}	//add percentage properties
			if ( name.equals( ParametersIds.ADD_PROPERTY ) ) {
				System.out.println( "Add Property" + "[" + value + "]");
				addProperties ( value );
			}	//recursive add nbClasses starting from level level
			if ( name.equals( ParametersIds.ADD_CLASSES ) ) {
				int index = aux.indexOf(".");
				int level = Integer.valueOf( aux.substring(0, index) );
				int nbClasses = Integer.valueOf( aux.substring(index+1, aux.length()) );
				System.out.println( "level " + level );
				System.out.println( "nbClasses " + nbClasses );
				addClasses ( level, nbClasses );
			}	//remove all the classes from the level level
			if ( name.equals( ParametersIds.REMOVE_CLASSES ) ) {
				System.out.println("Remove all classes from level" + (int)value );
				removeClassesFromLevel ( (int)value );
			}	//flatten a level
			if ( name.equals( ParametersIds.LEVEL_FLATTENED ) ) {
				//levelFlattened ( level );
				levelFlattened ( (int)value );
				System.out.println( "New class hierarchy level " + getMaxLevel() ) ;
				//this.classHierarchy.printClassHierarchy();
			}//rename classes
			if ( name.equals( ParametersIds.RENAME_CLASSES ) ) {
				System.out.println("Rename classes" + "[" + value + "]" );
				//System.out.println("\nValue = " + value + "\n");	//activeProperties, activeClasses, ..
				this.modifiedModel = renameResource ( false, true, value, true,false, false, 0);
			}//rename properties
			if ( name.equals( ParametersIds.RENAME_PROPERTIES ) ) {
				System.out.println("Rename properties " + "[" + value + "]" );
				//System.out.println("\nValue = " + value + "\n");	//activeProperties, activeClasses, ..
				this.modifiedModel = renameResource ( true, false, value, true,false, false, 0);
			}//remove percentage restrictions
			if ( name.equals( ParametersIds.REMOVE_RESTRICTION ) ) {
				System.out.println("Remove restrictions" + "[" + value + "]");
				removeRestrictions( value );
			}
		}
		/*
		System.out.println("\n\n\n***\n");
		
		Set<Object> e1 = params.keySet();
		for ( Iterator it = e1.iterator(); it.hasNext(); ) {
			String key = (String)it.next();
			String val = (String)params.get( key );
			System.out.println( "key " + key ); //+ " value " + value );
		}
		
		System.out.println("\n\n\n***\n");
		*/
		//compute the alignment and print it to the file referenceAlignment.rdf
		//computeAlignment( fileName );
		
	}
	

}
