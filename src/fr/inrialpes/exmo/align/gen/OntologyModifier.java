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

/* This program receives as input two ontologies (the original ontology, the ontology that must be modified),
  an alignment and a parameter with the modification that must be applied to the input ontology.
  After the modification of the initial ontology the alignment must be computed
  The file in which we store the alignment is "referenceAlignment.rdf"
*/

/* This program receives as input two ontologies (the original ontology, the ontology that must be modified),
  an alignment and a parameter with the modification that must be applied to the input ontology.
  After the modification of the initial ontology the alignment must be computed
  The file in which we store the alignment is "referenceAlignment.rdf"
*/

package fr.inrialpes.exmo.align.gen;

//Java classes
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

//Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;

//Google API classes
import com.google.api.GoogleAPI;
import com.google.api.translate.Language;
import com.google.api.translate.Translate;

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
import fr.inrialpes.exmo.align.impl.URIAlignment;
import java.util.Enumeration;
/*
//WordNet API classes
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
*/

//activeRandomString is true -> we replace the label with a random string
//activeTranslateString is true -> we translate the label

public class OntologyModifier {
    private boolean debug = false;
    private ClassHierarchy classHierarchy;                                      //the class hierarchy
    // JE: In all this class, model is useless!
    private OntModel model;                                                     //the model - the original Ontology
    private OntModel modifiedModel;						//the modified 	Ontology
    private String namespace;							//the Namespace
    private String namespaceNew;
    private Alignment alignment;						//the alignment of the two Ontologies
    private Properties params;							//the alignment
    private boolean isBuild = false;							//keep track if the class hierarchy is build
    private boolean isAlign = false;							//keep track if the initial alignment has already been computed
    private boolean isChanged = false;                                                  //keep track if the namespace of the new ontology is changed
    private String base;                                                        //

    // -------------------------
    // Constructors
    //Ontology init, Ontology modified, Alignment align
    // JE: Here the alignment is useless... it is used only if nothing is computed!
    public OntologyModifier ( OntModel model, Alignment alignment ) {
        modifiedModel = model;
	this.alignment = alignment;
	namespace = model.getNsPrefixURI("");       // JE: ???
        namespaceNew = "";
	params = new Properties();
    }

    // -------------------------
    // Accessors

    public void setDebug( boolean d ) { 
	debug = d;
    }

    public void setNewNamespace(String newNamespace) {
        this.namespaceNew = newNamespace;
        //if ( debug ) System.err.println("New namespace [" + this.namespaceNew + "]");
    }

    //returns the modified ontology after changing the namespace
    public OntModel getModifiedOntology () {
        //if ( debug ) System.err.println( "->change namespace" );
        modifiedModel = changeNamespace();				//change the namespace of the modified ontology
        //if ( debug ) System.err.println( "->namespace changed" );
        return modifiedModel;
    }

    public void setModifiedModel(OntModel model) {
        modifiedModel = model;
    }

    public OntModel getModifiedModel() {
        return modifiedModel;
    }

    //get properties
    public Properties getProperties() {
        return this.params;
    }

    //returns the alignment
    public Alignment getAlignment() {
        return this.alignment;
    }

    // -------------------------
    // Utility (string) functions

    //generates a random string with the length "length"
    public String getRandomString() {
        Random generator = new Random();
        String characters = "abcdefghijklmnopqrstuvwxyz";
	int length = characters.length();
	char[] text = new char[length];
	for (int i = 0; i < length; i++) {
		text[i] = characters.charAt( generator.nextInt(length) );
	}
	return new String(text); // JE suppressed toUpperCase()
    }

    //removes spaces from a string
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

    //translates the string from English to French
    public String translateString( String source ) {
        String translatedText = "";
        GoogleAPI.setHttpReferrer("http://code.google.com/p/google-api-translate-java/");
        //Translate.setHttpReferrer("http://code.google.com/p/google-api-translate-java/");
        try {
            translatedText = Translate.execute(source, Language.ENGLISH, Language.FRENCH);
        } catch (Exception e) {
            System.err.println( "Exception " + e.getMessage() );
        }
        return removeSpaces ( translatedText );
    }

    //string to upperCase
    public String toUpperCase ( String source ) {
        return source.toUpperCase();
    }

    //string to lowerCase
    public String toLowerCase ( String source ) {
        return source.toLowerCase();
    }

    public String getSynonym( String source ) {
        return source;
    }
        /*
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
        */

    public String parseString (String str, boolean activeTranslateString, boolean activeSynonym) {
        // if ( debug ) System.err.println ( "str = [" + str + "]" );
        char [] parsed = str.toCharArray();
        String newString = "";

        for ( int i=1; i<parsed.length; i++ ) {
            if( Character.isUpperCase( parsed[i] ) ) {
                String aux = str.substring(0, i);

                if ( activeTranslateString )
                    newString = newString.concat( translateString( str.substring(0, i) ) );
                if ( activeSynonym )
                    newString = newString.concat( getSynonym( str.substring(0, i) ) );

                str = str.substring(i);
            }
        }

        if ( activeTranslateString )
            newString = newString.concat( translateString(str.substring(0)) );
        if ( activeSynonym )
            newString = newString.concat( getSynonym(str.substring(0)) );
        return newString;
    }
    
    // -------------------------
    // Utility (randomizing) functions

    //count - the number of elements from the vector
    //the random numElems that must be selected
    //uses the Fisher and Yates method to shuffle integers from an array
    public int [] randNumbers (int count, int numElems) {
        int [] vect = new int[count];
        int [] n    = new int[numElems];
        int aux, rand;
        Random generator = new Random();

        for ( int i=0; i<count; i++ )                                           //fill the array with sorted elements
            vect[i] = i;
        for ( int j=0; j<numElems; j++ ) {
            rand = generator.nextInt( count-j );                                //choose a random number from the interval
            n[j] = vect[rand];                                                  //build the new vector
            aux = vect[rand];                                                   //swap
            vect[rand] = vect[count-j-1];
            vect[count-j-1] = aux;
        }
        return n;
    }

    // -------------------------
    // Label replacement

    //replaces the label of the property
    public void replacePropertyLabel( String uri, String newLabel, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation ) {
        OntProperty prop = modifiedModel.getOntProperty( uri );
        if ( prop.getLabel( "en" ) != null ) {
            if ( activeTranslateString ) {
                prop.setLabel( newLabel, "fr" );
            } else {
                prop.setLabel( newLabel, "en" );
            }
        }
    }

    //gets the URIs of the properties and their translation
    public HashMap<String, String> getPropertiesIdentifiers( float percentage, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation) {
        HashMap<String, String> propertiesIdentifiers = new HashMap<String, String>();	//the HashMap of the properties identifiers
	List<String> propertiesName = new ArrayList<String>();                  //the properties identifiers

        List<OntProperty> propertiesTo = new ArrayList<OntProperty>();          //the list of properties to be renamed
	List<OntProperty> notRenamedProperties = new ArrayList<OntProperty>();  //the list of not renamed properties
        List<OntProperty> properties = getOntologyProperties();                 //the list of all the properties

        int nbProperties, toBeRenamed, renamedProperties;

        //builds the list of all unrenamed properties from the model
        for ( OntProperty p : properties ) {
            String uri = base + p.getLocalName();
	    // JE: I am affraid that this.params is a terible thing here
            if ( this.params.containsKey( uri ) ) {
                String key = uri;
                String value = this.params.getProperty( key );
                if ( key.equals( value ) ) 
                    notRenamedProperties.add( p );      //add the property to not renamed properties  
            }
        }


        nbProperties = properties.size();                                       //the number of renamed properties
        renamedProperties = nbProperties - notRenamedProperties.size();
        toBeRenamed = (int)(percentage*nbProperties) - renamedProperties;       // -renamedProperties -> for Benchmark

	// JE: same thing as for classes... but strange
	// JE: should be the symptom of a bug
	if ( toBeRenamed < 0 ) toBeRenamed = 0;
        //builds the list of properties to be renamed
	int [] n = randNumbers(notRenamedProperties.size(), toBeRenamed);
	for ( int i=0; i<toBeRenamed; i++ ) {
		OntProperty p = notRenamedProperties.get(n[i]);
		propertiesTo.add(p);
                if ( p.getNameSpace().equals( namespace ) ) 
                    propertiesName.add( p.getLocalName() );
	}

	for ( OntProperty prop : propertiesTo ) {
		String nameSpace = prop.getNameSpace();
		String localName = prop.getLocalName();
		//has the same Namespace as the Ontology Namespace
                if ( nameSpace.equals( namespace ) ) {
                    if ( !propertiesIdentifiers.containsKey( localName ) ) {
                        if ( activeTranslateString ) {                          //replace the URI with the translated one
                            String translateStrg = parseString ( localName, true, false);
                            propertiesIdentifiers.put( localName , translateStrg );
                            replacePropertyLabel( prop.getURI(), translateStrg, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );

                            if ( params.containsKey( base + prop.getLocalName() ) ) {        //this.params.remove( prop.getURI() );
                                this.params.put( base + prop.getLocalName() , base + translateStrg );//the reference alignment
                            }
                        }
                        else if ( activeRandomString ) {                        //replace the URI with a random string
                            String newStrg = getRandomString();
                            propertiesIdentifiers.put( localName , newStrg );
                            replacePropertyLabel( prop.getURI(), newStrg, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                            if ( params.containsKey( base + prop.getLocalName() ) ) {        //this.params.remove( prop.getURI() );
                                this.params.put( base + prop.getLocalName() , base + newStrg);//the reference alignment
                            }
                        }
                        else if ( activeSynonym ) {
                            String synonym = parseString (localName, false, true);
                            if ( propertiesName.contains( synonym ) )
                                propertiesIdentifiers.put( localName, localName );
                            else  {
                                propertiesIdentifiers.put( localName, synonym );
                                replacePropertyLabel( prop.getURI(), synonym, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                                if ( params.containsKey( this.base + prop.getLocalName() ) ) {    //this.params.remove( prop.getURI() );
                                    this.params.put( this.base + prop.getLocalName() , this.base + synonym );	//the reference alignment
                                }
                            }
                        }
                        else if ( activeStringOperation == 1 ) {                //replace the URI with the UpperCase URI
                            propertiesIdentifiers.put( localName , toUpperCase( localName ) );
                            replacePropertyLabel( prop.getURI(), toUpperCase( localName ), activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                            if ( params.containsKey( this.base + prop.getLocalName() ) ) {        //this.params.remove( prop.getURI() );
                                this.params.put( this.base + prop.getLocalName() , this.base + toUpperCase( localName ) ); //the reference alignment
                            }
                        }
                        else if ( activeStringOperation == 2 ) {
                            propertiesIdentifiers.put( localName , toLowerCase( localName ) );
                            replacePropertyLabel( prop.getURI(), toLowerCase( localName ), activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                            if ( params.containsKey( this.base + prop.getLocalName() ) ) {        // this.params.remove( prop.getURI() );
                                this.params.put( this.base + prop.getLocalName() , this.base + toLowerCase( localName ) ); //the reference alignment
                            }
                        }
                        else {
                            propertiesIdentifiers.put( localName,  localName + "PROPERTY" );
                            replacePropertyLabel( prop.getURI(), localName + "PROPERTY", activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                            if ( params.containsKey( this.base + prop.getLocalName() ) ) {        //this.params.remove( prop.getURI() );
                                this.params.put( this.base + prop.getLocalName() , this.base + localName + "PROPERTY" );
                            }
                        }
                    }
                }
        }
        return propertiesIdentifiers;
    }

    //replaces the label of the class
    public void replaceClassLabel( String uri, String newLabel, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation ) {
        OntClass c = modifiedModel.getOntClass( uri );

        if ( c.getLabel( "en" ) != null ) {
            if ( activeTranslateString ) {
                c.setLabel( newLabel, "fr" );
            } else
                c.setLabel( newLabel, "en" );
        }
    }

    //gets the URIs of the classes and their translation
    public HashMap<String, String> getClassesIdentifiers ( float percentage, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation ) {
        HashMap<String, String> classesIdentifiers = new HashMap<String, String>(); //the HashMap of classes identifiers

        int nbClasses, toBeRenamed, renamedClasses;

        List<OntClass> notRenamedClasses = new ArrayList<OntClass>();           //the list of not renamed classes
        List<OntClass> classes = getOntologyClasses();                     //the list of ontology classes
        List<OntClass> classesTo = new ArrayList<OntClass>();                   //the list of classes to be renamed

	// params contains those classes which have already been renamed
        //builds the list of all unrenamed classes from the model
        for ( OntClass c : classes ) {
            String uri = base + c.getLocalName();
            //gets the pair <key, value>
            if ( this.params.containsKey( uri ) ) {
                String key = uri;
                String value = this.params.getProperty( uri );
                //they didnt change
                if ( key.equals( value ) ) 
                    notRenamedClasses.add( c ); //add the class to not renamed classes
            }
        }
        
        nbClasses = classes.size();                           
        renamedClasses = nbClasses - notRenamedClasses.size();                  //the number of renamed classes
        toBeRenamed = (int)(percentage*nbClasses) - renamedClasses;             // -renamedClasses -> for Benchmark
	// JE: toBeRenamed is negative when classes have been added to the model!
	// JE TOCHECK IF THIS IS STILL CORRECT
	if ( toBeRenamed < 0 ) toBeRenamed = 0;
        //build the list of classes to be renamed
        int[] n = randNumbers( notRenamedClasses.size(), toBeRenamed );
        for ( int i=0; i<toBeRenamed; i++ ) {
            OntClass cls = notRenamedClasses.get(n[i]);
            classesTo.add(cls);
        }

        for ( OntClass cls : classesTo ) {
            if ( !cls.isRestriction() ) {
                if ( !cls.isAnon() ) {
                    String nameSpace = cls.getNameSpace();
                    String localName = cls.getLocalName();

                    //has the same Namespace as the Ontology Namespace
                    if ( nameSpace.equals( namespace ) ) {
                        if ( !classesIdentifiers.containsKey( localName ) ) {
                            if ( activeTranslateString ) {			//replace the URI with the translated one
                                String translateStrg = parseString (localName, true, false);
                                classesIdentifiers.put( localName , translateStrg );
                                replaceClassLabel( cls.getURI(), translateStrg, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                                if ( params.containsKey( this.base + cls.getLocalName() ) ) {     //this.params.remove( cls.getURI() );
                                    this.params.put( this.base + cls.getLocalName() , this.base + translateStrg);	//the reference alignment
                                }
                            }
                            else if ( activeRandomString )	{		//replace the URI with a random string
                                String newStrg = getRandomString();
                                classesIdentifiers.put( localName , newStrg );
                                replaceClassLabel( cls.getURI(), newStrg, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                                if ( params.containsKey( this.base + cls.getLocalName() ) ) {     //this.params.remove( cls.getURI() );
                                    this.params.put( this.base + cls.getLocalName() , this.base + newStrg );	//the reference alignment
                                }
                            }
                            else if ( activeSynonym ) {                         //replace the URI with a synonym
                                String synonym = parseString (localName, false, true);
				classesIdentifiers.put( localName, synonym );
				replaceClassLabel( cls.getURI(), synonym, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
				if ( params.containsKey( this.base + cls.getLocalName() ) ) {     //this.params.remove( cls.getURI() );
                                    this.params.put( this.base + cls.getLocalName() , this.base + synonym );//the reference alignment
                                }
                            }
                            else if ( activeStringOperation == 1 ){             //replace the URI with the UpperCase URI
                                classesIdentifiers.put( localName , toUpperCase( localName ) );
                                replaceClassLabel( cls.getURI(), toUpperCase(localName), activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                                if ( params.containsKey( this.base + cls.getLocalName() ) ) {     //this.params.remove( cls.getURI() );
                                    this.params.put( this.base + cls.getLocalName() , this.base + toUpperCase( localName ) ); //the reference alignment
                                }
                            }
                            else if ( activeStringOperation == 2 ){             //replace the URI with the LowerCase URI
                                classesIdentifiers.put( localName , toLowerCase( localName ) );
                                replaceClassLabel( cls.getURI(), toLowerCase(localName), activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                                if ( params.containsKey( this.base + cls.getLocalName() ) ) {     //this.params.remove( cls.getURI() );
                                    this.params.put( this.base + cls.getLocalName() , this.base + toLowerCase( localName ) );     //the reference alignment
                                }
                            }
                            else {
                                classesIdentifiers.put( localName, localName + "CLASS" );
                                replaceClassLabel( cls.getURI(), localName + "CLASS", activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                                if ( params.containsKey( this.base + cls.getLocalName() ) ) {     //this.params.remove( cls.getURI() );
                                    this.params.put( this.base + cls.getLocalName() , this.base + localName + "CLASS" );
                                }
                            }
                        }
                    }
                }
            }
        }
        return classesIdentifiers;
    }

    //renames percentage properties and classes
    //activeProperties -> if true, then rename properties
    //activeClasses -> if true, then rename classes
    public OntModel renameResource ( boolean activeProperties, boolean activeClasses, float percentage, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation) {
        List<Statement> statements = null;                                      //the list of all statements
        HashMap<String, String> propertiesIdentifiers = null;                   //the HashMap of the properties identifiers
        HashMap<String, String> classesIdentifiers = null;                      //the HashMap of the classes identifiers

        OntModel newModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );//create new Model
        //get properties and classes identifiers
        if ( activeProperties )
            propertiesIdentifiers = getPropertiesIdentifiers( percentage, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation);
        if ( activeClasses )
            classesIdentifiers = getClassesIdentifiers( percentage, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
      
        //iterate and modify the identifiers
        for ( Statement stm : modifiedModel.listStatements().toList() ) {

            Resource subject   = stm.getSubject();                              //the subject
            Property predicate = stm.getPredicate();                            //the predicate
            RDFNode object     = stm.getObject();                               //the object

	    String subjectLocalName,   subjectNameSpace;
	    String predicateLocalName, predicateNameSpace;
	    String objectLocalName,    objectNameSpace;
	    
	    boolean isPred, isSubj, isObj;

            Resource subj = null;
            Property pred = null;
            Resource obj  = null;
            isPred = isSubj = isObj = false;

            //if it is the subject of the statement
            if ( subject.getLocalName() != null ) {
                if ( activeProperties ) {
                    if ( propertiesIdentifiers.containsKey( subject.getLocalName() ) ) {
                        //if the namespace of the subject is the same as the namespace of the property identifier
                        if ( subject.getNameSpace().equals( namespace ) ) {//that we want to remove
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
                        if (subject.getNameSpace().equals( namespace ) ) {
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
                            //if the namespace of the object is the same as the namespace of the property identifier that we want to remove
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
        if ( activeClasses ) {
            checkClassHierarchy();
            //we update the class hierarchy according to the new modifications
            this.classHierarchy.updateClassHierarchy( params );     //this.classHierarchy.printClassHierarchy();
        }
        return newModel;
    }

    // -------------------------
    // Class hierarchy utilities

    //must have the max level of the class hierarchy
    public int getMaxLevel() {
        checkClassHierarchy();                                                  //check if the class hierarchy is built
        return this.classHierarchy.getMaxLevel();
    }

    //if we add / remove a class we need to keep track of the class hierarchy
    public void buildClassHierarchy () {
        classHierarchy = new ClassHierarchy();
        classHierarchy.buildClassHierarchy( modifiedModel );
        //classHierarchy.printClassHierarchy();
    }

    //check if the class hierarchy is build
    public void checkClassHierarchy() {
        if ( !this.isBuild ) {
            buildClassHierarchy();
            this.isBuild = true;
        }
    }

    //gets the Ontology classes
    @SuppressWarnings("unchecked")
    public List<OntClass> getOntologyClasses () {
        List<OntClass> classes = new ArrayList<OntClass>();
        for ( Iterator it = modifiedModel.listNamedClasses(); it.hasNext(); ) {
            OntClass aux = (OntClass)it.next();
            if ( ( aux ).getNameSpace().equals( this.namespace ) ) {
                classes.add( aux );
            }
        }
        return classes;
    }

    //gets the Ontology properties
    @SuppressWarnings("unchecked")
    public List<OntProperty> getOntologyProperties () {
        List<OntProperty> properties = new ArrayList<OntProperty>();
        for ( Iterator it = modifiedModel.listAllOntProperties(); it.hasNext(); ) {
            OntProperty prop = (OntProperty)it.next();
            if ( prop.getNameSpace().equals( this.namespace ) )
                properties.add( prop );
        }
        return properties;
    }

    //adds a class with a random URI to the parent class parentURI
    public OntClass addClass ( OntClass parentClass, String name ) {
	String childURI = namespace+name;
        OntClass childClass = modifiedModel.createClass( childURI );//create a new class to the model

        classHierarchy.addClass( childURI, parentClass.getURI() );//add the node in the hierarchy of classes
        parentClass.addSubClass( childClass );                                  //add the childClass as subclass of parentClass
        modifiedModel.add( childClass, RDFS.subClassOf, parentClass );     //add the class to the model
        return childClass;
    }

    //add the percentage of the specified subclasses
    public void addSubClasses ( float percentage ) {
        List<OntClass> classes = getOntologyClasses();                          //get the list of classes from the Ontology
        int nbClasses = classes.size();                                         //number of classes from the Ontology
        int toAdd = (int)(percentage*nbClasses);
		
        checkClassHierarchy();                                                  //check if the classHierarchy is built
        //build the list of properties to be renamed
        int[] n = this.randNumbers( nbClasses, toAdd );
        for ( int i=0; i<toAdd; i++ ) {
            addClass( classes.get(n[i]), getRandomString() ); //give a random URI to the new class
        }
    }

    //add nbClasses beginning from level
    public void addClasses ( int level, int nbClasses, float percentage ) {
        String classURI;
        //the parent class -> if level is 1 then we create a new class
        //else we get a random class from the level : level-1 to be the parent of the class
        OntClass parentClass;
        OntClass childClass;
        List<OntClass> parentClasses = new ArrayList<OntClass>();
        List<OntClass> childClasses = new ArrayList<OntClass>();

        checkClassHierarchy();                                                  //check if the class hierarchy is built
        if ( level == 1 ) {                                                     //the parent of the class is Thing, we add the class and then the rest of the classes
           classURI = this.getRandomString();
           parentClass = modifiedModel.createClass( this.namespace + classURI );//create a new class to the model
           this.classHierarchy.addClass( this.namespace + classURI, "Thing" );  //add the node in the hierarchy of classes
           childClasses.add(parentClass);
        }
        else {
            parentClasses = this.classHierarchy.getClassesFromLevel(modifiedModel, level);
            int nbParentClasses = parentClasses.size();                         //number of classes from the Ontology
            int toAdd = (int) ( percentage * nbClasses );                       // 1 can be replaced by percentage

            for ( OntClass pClass : parentClasses ) {
                classURI = this.getRandomString();
                childClass = addClass (pClass, classURI );
                pClass = childClass;
                childClasses.add( childClass );
            }
        }

        for ( OntClass pClass : childClasses ) {
            classURI = pClass.getLocalName();
            for ( int i=level+1; i<level + nbClasses; i++ ) {
                classURI = "IS_" + classURI;
                childClass = addClass (pClass, classURI);
                pClass = childClass;
            }	//this.classHierarchy.printClassHierarchy();
        }
    }

    //changes the unionOf, intersectionOf
    public OntModel changeDomainRange ( HashMap<String, String> uris ) {
        OntModel newModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);//create new Model
        boolean isSubj, isObj, isPred;
        String subjectNameSpace, subjectLocalName;
        String objectNameSpace, objectLocalName;
        String predicateNameSpace, predicateLocalName;
        
        //iterate and modify the identifiers
        for ( Statement stm : modifiedModel.listStatements().toList() ) {
            Resource subject   = stm.getSubject();                              //the subject
            Property predicate = stm.getPredicate();                            //the predicate
            RDFNode object     = stm.getObject();                               //the object
            Resource subj = null;
            Property pred = null;
            Resource obj  = null;
            isSubj = isObj = isPred = false;

            //if the class appears as the subject of a proposition
            if ( subject.getURI() != null ) 
            if ( uris.containsKey( subject.getURI() ) ) {
                isSubj = true;
                subj = newModel.createResource( uris.get( subject.getURI() ) );
            }
            
            //if appears as the predicate - never
            if ( predicate.getURI() != null )
            if ( uris.containsKey( predicate.getURI() ) ) {
                isPred = true;
                pred = newModel.createProperty( uris.get( predicate.getURI() ) );
            }

            //if appears as the object of the statement
             if ( object.canAs( Resource.class ) )
                if ( object.isURIResource() ) {
                    if ( object.asResource().getURI() != null )
                        if ( uris.containsKey( object.asResource().getURI() ) ) {
                            isObj = true;
                            obj = newModel.createResource( uris.get( object.asResource().getURI() ) );
                        }
                    }

            if ( isSubj ) {
                if ( isPred ) {
                    if ( isObj )    newModel.add( subj, pred, obj );
                    else            newModel.add( subj, pred, object );
                }
                else {
                    if ( isObj )    newModel.add( subj, predicate, obj );
                    else            newModel.add( subj, predicate, object );
                }
            } else {
                if ( isPred ) {
                    if ( isObj )    newModel.add( subject, pred, obj );
                    else            newModel.add( subject, pred, object );
                }
                else {
                    if ( isObj )    newModel.add( subject, predicate, obj );
                    else            newModel.add( subject, predicate, object );
                }
            }
        }       
        return newModel;
    }

    //check if the removed class appears as AllValueFrom or SomeValueFrom in a restriction
    @SuppressWarnings("unchecked")
    public void checkClassesRestrictions ( OntClass childClass, OntClass parentClass )  {
        Restriction restr = null;
        for ( Iterator it = modifiedModel.listRestrictions(); it.hasNext(); ) {
            restr = (Restriction)it.next();					//get the restriction
            /* isAllValuesFromRestriction */
            if ( restr.isAllValuesFromRestriction() )  {
                AllValuesFromRestriction av = restr.asAllValuesFromRestriction();
                if ( av.getAllValuesFrom().getURI() != null )                   //if points to the childClass
                    //if ( av.getNameSpace().equals( this.namespace ) )
                        if ( av.getAllValuesFrom().getURI().equals( childClass.getURI() ) )
                            av.setAllValuesFrom( parentClass );                     //change to point to the parentClass
				 }
            /* isHasValueRestriction */
            if ( restr.isSomeValuesFromRestriction() ) {
                SomeValuesFromRestriction sv = restr.asSomeValuesFromRestriction();
                if ( sv.getSomeValuesFrom().getURI() != null )                  //if points to the childClass
                    //if ( sv.getNameSpace().equals( this.namespace ) )
                        if ( sv.getSomeValuesFrom().getURI().equals( childClass.getURI() ) )
                            sv.setSomeValuesFrom( parentClass );                    //change to point to the parentClass
            }

        }
    }

    //removes a class, returns the uri of his parent
    @SuppressWarnings("unchecked")
    public String removeClass ( OntClass cls ) {
        OntClass parentClass;
        ArrayList<OntClass> subClasses = new ArrayList<OntClass>();		//the list of all the subclasses of the class
        OntClass thing = modifiedModel.createClass( OWL.Thing.getURI() );	//Thing class
        checkClassHierarchy();							//check if the class hierarchy is built
        parentClass = this.classHierarchy.removeClass( modifiedModel, cls );//get the parent of the class

        for (Iterator it1 = cls.listSubClasses(); it1.hasNext(); ) {            //build the list of subclasses
            OntClass subCls = (OntClass)it1.next();				//because we can't change the
            subClasses.add( subCls );						//model while we are iterating
        }

        if ( parentClass != thing )						//now we change the superclass of classes
            for (OntClass clss : subClasses) 					//new superclass =>
                clss.setSuperClass( parentClass );				//=>the superclass of the node

	    checkClassesRestrictions( cls, parentClass );
            cls.remove();							//remove the class from the Ontology
            return parentClass.getURI();
    }

    //remove the subClasses from the list
    public void removeSubClasses ( float percentage ) {
        List<OntClass> classes = this.getOntologyClasses();			//the list of classes from Ontologu
        List<OntClass> removedClasses = new ArrayList<OntClass>();
        List<String> cl = new ArrayList<String>();
        HashMap<String, String> uris = new HashMap<String, String>();           //the HashMap of strings
        String parentURI = "";
        int nbClasses = classes.size();						//number of classes
        checkClassHierarchy();							//check if the class hierarchy is built
        int toBeRemoved =  (int) (percentage*nbClasses);			//the number of classes to be removed

        //build the list of classes to be removed
        int [] n = this.randNumbers(nbClasses, toBeRemoved);
        for ( int i=0; i<toBeRemoved; i++ ) {
            OntClass cls = classes.get(n[i]);
            removedClasses.add( cls );
            cl.add( cls.getURI() );                                             //builds the list of labels of classes to be removed
        }

        for ( OntClass cls : removedClasses ) {					//remove the classes from the list
            parentURI = removeClass (cls);
            uris.put(cls.getURI(), parentURI);
        }

        //checks if the class appears like unionOf.. and replaces its appearence with the superclass
        modifiedModel = changeDomainRange( uris );
        
        //remove the URI of the class from the reference alignment
        for ( String key : this.params.stringPropertyNames() ) {
            String value = this.params.getProperty( key );
            if ( cl.contains( this.namespace + key.substring(key.indexOf(this.base) + this.base.length() )) )
                this.params.remove( key );
            if ( cl.contains( this.namespace + value.substring(value.indexOf(this.base) + this.base.length() )) )                                          //we iterate to check if the class appears only like value
                this.params.remove( key );
        }
    }

    //remove all the classes from a specific level
    public void removeClassesFromLevel ( int level ) {
        HashMap<String, String> uris = new HashMap<String, String>();
        String parentURI = "";
        //if ( debug ) System.err.println( "Level " + level );
        /*	if ( level == 1 )						//except classes from level 1
         return;	*/
        List<OntClass> classes = new ArrayList<OntClass>();
        checkClassHierarchy();							//check if the class hierarchy is built
        classes = this.classHierarchy.getClassesFromLevel(modifiedModel, level);
        for ( int i=0; i<classes.size(); i++ ) {                                //remove the classes from the hierarchy
            parentURI = removeClass ( classes.get(i) );
            uris.put(classes.get(i).getURI(), parentURI);
        }
        //checks if the class appears like unionOf .. and replaces its appearence with the superclass
        modifiedModel = changeDomainRange( uris );
    }

    //remove percentage individuals from Ontology
    public void removeIndividuals( float percentage ) {
        boolean isSubj, isObj;							//the individual can appear as subject or object
        List<Individual> individuals = modifiedModel.listIndividuals().toList();
        List<Individual> individualsTo = new ArrayList<Individual>();           //the list of individuals to be removed
        int nbIndividuals = individuals.size();					//the number of individuals
        int toBeRemoved = (int)( percentage*nbIndividuals );                    //the number of individuals to be removed
        
        int [] n = this.randNumbers(nbIndividuals, toBeRemoved);                //build the list of individuals to be removed
        for ( int i=0; i<toBeRemoved; i++ ) {
            Individual indiv = individuals.get(n[i]);				//remove the individual from the reference alignment
            individualsTo.add( indiv );
            //this.params.remove( indiv.getURI() );
        }

        for ( Statement st : modifiedModel.listStatements().toList() ) {
            Resource subject   = st.getSubject();
            RDFNode object     = st.getObject();
            isSubj = isObj = false;

            if ( individualsTo.contains( subject ) )
                isSubj = true;
            if ( object.canAs( Resource.class ) )
                if ( individualsTo.contains( object.asResource() ) )
                   isObj = true;
            if ( isSubj )	//the individual appears as subject in the statement
                modifiedModel.remove( st );
            if ( isObj )	//the individual appears as object in the statement
                modifiedModel.remove( st );
        }
    }

    //remove properties from the model
    @SuppressWarnings("unchecked")
    public void removeProperties ( float percentage ) {
        List <OntProperty> properties = this.getOntologyProperties();           //the list of all properties from the model
        ArrayList <OntProperty> propertiesToBeRemoved = new ArrayList<OntProperty>();
        ArrayList<Restriction> restrictions = new ArrayList<Restriction>();
        ArrayList<OntClass> resources = new ArrayList<OntClass>();
        List<String> pr = new ArrayList<String>();
        boolean isObj, isSubj, isPred;

        int nbProperties = properties.size();					//the number of properties
        int toBeRemoved = (int)( percentage*nbProperties );			//the number of properties to be removed

        //build the list of classes to be removed
        int [] n = this.randNumbers(nbProperties, toBeRemoved);
        for ( int i=0; i<toBeRemoved; i++ ) {					//build the list of properties to be removed
            OntProperty p = properties.get(n[i]);
            propertiesToBeRemoved.add( p );
            pr.add( p.getURI() );


            //this.params.remove( p.getURI() );
            for ( Iterator it = p.listReferringRestrictions(); it.hasNext();  ) {//get the restrictions of that property
                restrictions.add( (Restriction)it.next() );
            }
            for ( Restriction r : restrictions )				//delete all the restrictions
                r.remove();

            //the domain of the property is a unionOf class
            if ( p.hasDomain(null) ) {
                if ( p.getDomain().canAs( OntResource.class  ) ) {
                    OntResource res = p.getDomain();
                    if ( res.canAs( UnionClass.class ) ) {
                        OntClass cls = res.asClass();
                        resources.add(cls);
                    }
                }
            }
            //the range of the property is a unionOf class
            if ( p.hasRange(null) ) {
                if ( p.getRange().canAs( OntResource.class ) ) {
                    OntResource res = p.getRange();
                    if ( res.canAs( UnionClass.class ) ) {
                        OntClass cls = res.asClass();
                        resources.add(cls);
                    }
                }
            }
        }

        for ( OntClass c : resources )
            c.remove();

        //remove that property from params
        //since we don't respect any order the value can appear as a value, thus we must iterate among all params to delete it
        for ( String key : this.params.stringPropertyNames() ) {
            String value = this.params.getProperty(key);
            if ( pr.contains( this.namespace + key.substring(key.indexOf(this.base) + this.base.length()) ) ) {     //if ( debug ) System.err.println( "Elimin " + key );
                this.params.remove( key );
            }
            if ( pr.contains( this.namespace + value.substring(key.indexOf(this.base) + this.base.length()) ) ) {   //if ( debug ) System.err.println( "Elimin " + key );
                this.params.remove( key );
            }
        }

        for ( Statement st : modifiedModel.listStatements().toList() ) {					//remove the declaration of properties from the model
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

            if ( object.canAs( Resource.class ) )				//if appears as object
                if ( propertiesToBeRemoved.contains( object ) )
                    if ( object.asResource().getNameSpace().equals( this.namespace ) )
                        isObj = true;

            if ( isSubj || isPred || isObj )					//remove the statement in which the prop
                modifiedModel.remove( st );				//appears as subject, predicate or object
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
        int nbClasses = classes.size();                                         //the number of classes
        int nbProperties = properties.size();                                   //the number of properties
        int toBeAdd = (int)( percentage*nbProperties );                         //the number of properties to be add

        for ( int i=0; i<toBeAdd/2; i++ ) {                                     //add object properties
            //p = modifiedModel.createObjectProperty( this.namespace + "OBJECT_PROPERTY_" + getRandomString() );
            p = modifiedModel.createObjectProperty( this.namespace + getRandomString() );
            index = classRand.nextInt( nbClasses );                             //pick random domain
            p.addDomain( classes.get( index ) );
            index = classRand.nextInt( nbClasses );                             //pick random range
            p.addRange( classes.get( index ) );
        }

        for ( int i=toBeAdd/2; i<toBeAdd; i++ ) {                               //add datatype properties
            //d = modifiedModel.createDatatypeProperty( this.namespace + "DATATYPE_PROPERTY_" + getRandomString() );
            d = modifiedModel.createDatatypeProperty( this.namespace +  getRandomString() );
            index = classRand.nextInt( nbClasses );                             //add domain
            d.addDomain( classes.get( index ) );
            d.addRange( XSD.xstring );						//add range -> string
        }
    }

    //remove classes comments
    @SuppressWarnings("unchecked")
    public void removeClassesComments ( float percentage ) {
        ArrayList<Literal> comments = new ArrayList<Literal>();
        List<OntClass> classes = modifiedModel.listNamedClasses().toList();
        ArrayList<OntClass> classesTo = new ArrayList<OntClass>();
        int nbClasses = classes.size();
        int toBeRemoved = (int)( percentage * nbClasses );                      //number of classes comments to be removed

        int [] n = this.randNumbers(nbClasses, toBeRemoved);
        for ( int i=0; i<toBeRemoved; i++ ) {
            OntClass cls = classes.get(n[i]);
            classesTo.add( cls );
        }

        for ( OntClass c : classesTo ) {
            for (Iterator it2 = c.listComments(null); it2.hasNext();)
                comments.add(((Literal) it2.next()));
            for (Literal lit : comments)                                        // remove comments
                c.removeComment( lit );
            comments.clear();
        }
    }

    //remove properties comments
    @SuppressWarnings("unchecked")
    public void removePropertiesComments ( float percentage ) {
        ArrayList<Literal> comments = new ArrayList<Literal>();                 // an array list to hold all the comments
        List<OntProperty> properties = modifiedModel.listAllOntProperties().toList();
        ArrayList<OntProperty> propertiesTo = new ArrayList<OntProperty>();
        int nbProperties = properties.size();
        int toBeRemoved = (int)( percentage * nbProperties );                   //the number of properties comments to be removed

        int [] n = this.randNumbers(nbProperties, toBeRemoved);
        for ( int i=0; i<toBeRemoved; i++ ) {
            OntProperty p = properties.get(n[i]);
            propertiesTo.add( p );
        }

        for ( OntProperty prop : propertiesTo ) {
            for (Iterator it2 = prop.listComments(null); it2.hasNext();)        // get all comments
                comments.add(((Literal) it2.next()));
            for (Literal lit : comments) 					//remove comments
                prop.removeComment( lit );
            comments.clear();
        }
    }

    //remove individuals comments
    @SuppressWarnings("unchecked")
    public void removeIndividualsComments ( float percentage ) {
        ArrayList<Literal> comments = new ArrayList<Literal>();                 // an array list to hold all the comments
        List<Individual> individuals = modifiedModel.listIndividuals().toList();
        ArrayList<Individual> individualsTo = new ArrayList<Individual>();
        int nbIndividuals = individuals.size();
        int toBeRemoved = (int)( percentage * nbIndividuals );                  //number of classes to be removed

        int [] n = this.randNumbers(nbIndividuals, toBeRemoved);
        for ( int i=0; i<toBeRemoved; i++ ) {
            Individual indiv = individuals.get(n[i]);
            individualsTo.add( indiv );
        }
        for ( Individual indiv : individuals ) {
            for (Iterator it2 = indiv.listComments(null); it2.hasNext(); )      //get all comments
                comments.add( ((Literal) it2.next()) );
            for (Literal lit : comments )					//remove comments
                indiv.removeComment( lit );
            comments.clear();
        }
    }

    //remove Ontologies comments
    @SuppressWarnings("unchecked")
    public void removeOntologiesComments ( float percentage ) {
        ArrayList<Literal> comments = new ArrayList<Literal>();                 // an array list to hold all the comments
        List<Ontology> ontologies = modifiedModel.listOntologies().toList();
        ArrayList<Ontology> ontologiesTo = new ArrayList<Ontology>();
        int nbOntologies = ontologies.size();
        int toBeRemoved = (int)( percentage * nbOntologies );                   //the number of Ontologies comments to be removed

        int [] n = this.randNumbers(nbOntologies, toBeRemoved);
        for ( int i=0; i<toBeRemoved; i++ ) {
            Ontology onto = ontologies.get(n[i]);
            ontologiesTo.add( onto );
        }

        for ( Ontology onto : ontologies ) {
            for (Iterator it2 = onto.listComments(null); it2.hasNext(); )       // get all comments
                comments.add(((Literal) it2.next()));
            for ( Literal lit : comments )					//remove all comments
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
        if ( level == 1 )                                                       //no change
            return;
        checkClassHierarchy();                                                  //check if the class hierarchy is built
        active = this.classHierarchy.flattenClassHierarchy( modifiedModel, level, levelClasses, parentLevelClasses, superLevelClasses);
        size = levelClasses.size();

        /* remove duplicates from list */
        HashMap<String, ArrayList<Restriction>> restrictions = new HashMap<String, ArrayList<Restriction>>();
        List<String> parentURI = new ArrayList<String>();
        HashMap<String, String> unionOf = new HashMap<String, String>();

        for ( int i=0; i<size; i++ ) {
            OntClass childClass = levelClasses.get( i );			//child class
            OntClass parentClass = parentLevelClasses.get( i );                 //parent class

            //build the list of restrictions of the parent class
            ArrayList<Restriction> restr = new ArrayList<Restriction>();
            List<OntClass> supCls = parentClass.listSuperClasses().toList();
            for ( OntClass cls : supCls ) {
                if ( cls.isRestriction() ) {
                    Restriction r = cls.asRestriction();
                    if ( r.isAllValuesFromRestriction() )
                        restr.add(r);
                    if ( r.isCardinalityRestriction() )  
                        restr.add(r);
                    if ( r.isHasValueRestriction() )
                        restr.add(r);
                    if ( r.isMaxCardinalityRestriction() )
                        restr.add(r);
                    if ( r.isMinCardinalityRestriction() )
                        restr.add(r);
                    if ( r.isSomeValuesFromRestriction() )
                        restr.add(r);
                    //if ( debug ) System.err.println( cls.getURI() + cls.getLocalName() );
                }
            }
            //if ( debug ) System.err.println( restr.size() );

            if ( !restrictions.containsKey( parentClass.getURI() ) ) {
                restrictions.put( parentClass.getURI(), restr );
            }
            parentURI.add( parentClass.getURI() );

            //all the classes are subclasses of owl: Thing
            if (  active ) {                                                    //if ( !parentClass.getURI().equals( "Thing" ) ) {
               OntClass superClass = superLevelClasses.get( i );                //parent class of the child class parents

               //if ( debug ) System.err.println("SuperClass class [" + superClass.getURI() + "]");
               //if ( debug ) System.err.println("Parent class [" + parentClass.getURI() + "]");
               //if ( debug ) System.err.println("Child class [" + childClass.getURI() + "]");
               
               if ( modifiedModel.containsResource(parentClass) ) {
                   //to check if the class appears as unionOf, someValuesFrom, allValuesFrom ..
                   unionOf.put(parentClass.getURI(), superClass.getURI());
                   checkClassesRestrictions ( parentClass, superClass );
                   parentClass.remove();
               }
               childClass.addSuperClass( superClass );
               parentClass.removeSubClass( childClass );
            } else {
                OntClass superClass = modifiedModel.createClass( OWL.Thing.getURI() );	//Thing class

                if ( modifiedModel.containsResource(parentClass) ) {
                   //to check if the class appears as unionOf..
                   unionOf.put(parentClass.getURI(), superClass.getURI());
                   checkClassesRestrictions ( parentClass, superClass );
                   parentClass.remove();
               }

                parentClass.removeSubClass( childClass );
            }            
        }

        int i = 0;
        for ( String uri : parentURI ) {
            OntClass childClass = levelClasses.get( i );
            List<Restriction> restr = restrictions.get( uri );
            for ( Restriction r : restr ) {
                childClass.addSuperClass(r);
            }
            i++;
        }

        //checks if the class appears like unionOf, someValuesFrom, allValuesFrom .. and replaces its appearence with the superclass
        modifiedModel = changeDomainRange(unionOf);

        //remove the parentClass from the alignment
        for ( String key : this.params.stringPropertyNames() ) {
            String value = this.params.getProperty(key);
            if ( parentURI.contains( this.namespace + key.substring(key.indexOf(this.base) + this.base.length()) ) ) {        //this.classHierarchy.removeUri("Thing", key);
                this.params.remove(key);
            }
            if ( parentURI.contains( this.namespace + value.substring(key.indexOf(this.base) + this.base.length() ))) {    //this.classHierarchy.removeUri("Thing", value);
                this.params.remove(key);
            }
        }

    }

    //flatten level - for noHierarchy
    public void _noHierarchy ( int level ) {
        if ( level == 1 ) return;
        int size;
        boolean active = false;
        ArrayList<OntClass> levelClasses = new ArrayList<OntClass>();		//the list of classes from that level
        ArrayList<OntClass> parentLevelClasses = new ArrayList<OntClass>();	//the list of parent of the child classes from that level
        ArrayList<OntClass> superLevelClasses = new ArrayList<OntClass>();	//the list of parent of the parent classes from that level
        checkClassHierarchy();                                                  //check if the class hierarchy is built
        active = this.classHierarchy.flattenClassHierarchy( modifiedModel, level, levelClasses, parentLevelClasses, superLevelClasses);
        size = levelClasses.size();

        for ( int i=0; i<size; i++ ) {
            OntClass childClass = levelClasses.get( i );			//child class
            OntClass parentClass = parentLevelClasses.get( i );                 //parent class
            //all the classes are subclasses of owl: Thing
            if (  active ) {                                                    //if ( !parentClass.getURI().equals( "Thing" ) ) {
               OntClass superClass = superLevelClasses.get( i );                //parent class of the child class parents
               childClass.addSuperClass( superClass );
               parentClass.removeSubClass( childClass );
            } else {
                parentClass.removeSubClass( childClass );
            }
        }
    }

    public void noHierarchy () {
        int level = this.getMaxLevel();  //this.classHierarchy.printClassHierarchy();
        while ( this.getMaxLevel() != 1 ) {
            //this.classHierarchy.printClassHierarchy();
            _noHierarchy ( level );
            level--;
            
        }
    }

    //remove percentage restrictions from the model
    public void removeRestrictions( float percentage )  {
        List<Restriction> restrictions   = new ArrayList<Restriction>();
        List<Restriction> restrictionsTo = new ArrayList<Restriction>();	//the array list of restrictions to be removed
        restrictions = modifiedModel.listRestrictions().toList();
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

    // -------------------------
    // Alignment management

    // isAlign is a not so good way to test initialisation
    // (initialisation with an empty Properties is done in the initialiser)
    // but the "params" is used everywhere...

    //the initial reference alignment
    // JE: !!!: no order guarantee in properties !!!
    public void initializeAlignment( Properties params ) {
        this.params = params;

        Enumeration e = this.params.propertyNames();
        String aux = (String)e.nextElement();
        base = aux.substring(0, aux.lastIndexOf("#")+1);

        this.isAlign = true;
    }

    public void initializeAlignment() {
        List<OntClass> classes       = modifiedModel.listNamedClasses().toList();//all classes
        List<OntProperty> properties = modifiedModel.listAllOntProperties().toList();//all properties
        List<Individual> individuals = modifiedModel.listIndividuals().toList();//all individuals
        List<Ontology> ontologies    = modifiedModel.listOntologies().toList();//all Ontologies

        this.params = new Properties();

        this.isAlign = true;							//the alignment has been computed for the first time
        this.base = this.namespace;
        
        for ( OntClass cls : classes )                                          //list all classes
            if ( cls.getNameSpace().equals( this.namespace ) )
                this.params.put( cls.getURI() , cls.getURI() );                 //add them to the initial alignment

        for ( OntProperty prop : properties )                                   //list all properties
            if ( prop.getNameSpace().equals( this.namespace ) )
                this.params.put( prop.getURI() , prop.getURI() );               //add them to the initial alignment
    }

    //compute the alignment after the modifications
    @SuppressWarnings("unchecked")
    public void computeAlignment( String fileName ) {
        URI onto1 = null;
        URI onto2 = null;

        this.alignment  = new URIAlignment();
        
        try {
            onto1 = new URI ( this.base.substring(0, this.base.lastIndexOf("#")) );
            onto2 = new URI( this.namespaceNew.substring(0, this.namespaceNew.lastIndexOf("#")) );

            this.alignment.init(onto1, onto2);
            this.alignment.setFile1(onto1);
            this.alignment.setFile2(onto2);
            
            //Cell addAlignCell(Object ob1, Object ob2, String relation, double measure) throws AlignmentException {
            for ( String key : this.params.stringPropertyNames() ) {
                String value = this.params.getProperty(key);
                URI uri1 = URI.create(key);
                URI uri2 = URI.create(this.namespaceNew + value.substring(value.indexOf(this.base) + this.base.length()));
                //if ( debug ) System.err.println( "[" + key + "][" + value + "]" );
                this.alignment.addAlignCell( uri1, uri2, "=", 1 );
            }
        } catch (AlignmentException aex)  { System.err.println( "Exception " + aex.getMessage() );
        } catch (Exception ex) {  System.err.println("Exception " + ex.getMessage());
        }
    }

    // -------------------------
    // Namespace management and change

    //change the namespace of the modified ontology
    // JE: ??
    public OntModel changeNamespace () {
        this.isChanged = true;
        boolean isSubj, isPred, isObj;;

        OntModel newModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );//create new Model

        //iterate through all the statements and change the namespace
        for ( Statement stm : modifiedModel.listStatements().toList() ) {
            Resource subject   = stm.getSubject();                              //the subject
            Property predicate = stm.getPredicate();                            //the predicate
            RDFNode object     = stm.getObject();                               //the object
            
            Resource subj = null;
            Property pred = null;
            Resource obj  = null;

            isPred = isSubj = isObj = false;

            if ( subject.getLocalName() != null )
                if ( !subject.isLiteral() )
                    if ( subject.getNameSpace().equals( this.namespace ) ) {
                        subj = newModel.createResource( this.namespaceNew + subject.getLocalName() );
                        isSubj = true;
                    }

            if ( !object.isLiteral() )
                if ( object.canAs( Resource.class ) )
                    if ( object.isURIResource() )
                        if ( object.asResource().getNameSpace().equals( this.namespace ) ) {
                            obj = newModel.createResource( this.namespaceNew + object.asResource().getLocalName() );
                            isObj = true;
                        }

            if ( !predicate.isLiteral() )
                if ( predicate.getNameSpace().equals( this.namespace ) ) {
                    pred = newModel.createProperty( this.namespaceNew + predicate.getLocalName() );
                    isPred = true;
                }

            if ( isSubj ) {
                if ( isPred ) {
                    if ( isObj )
                        newModel.add( subj, pred, obj );
                    else
                        newModel.add(subj, pred, object);
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
            ResourceUtils.renameResource( o, this.namespaceNew );
        }

        return newModel;
    }

    // -------------------------
    // Main entry point

    //OntModel model, OntModel modifiedModel, Alignment alignment
    public void modifyOntology( String name, String param ) {
	if ( param == null ) return;
        if ( name == null || name.equals("") ) {
            System.err.println( "No parameter" );                               //no parameter as input
	    return;
        };
        float value = 0.0f;
	String aux = "";

	if ( param.equals( ParametersIds.NO_HIERARCHY ) )
	    ;
	else if( param != null)                          //the value of the parameter
	    value = Float.valueOf( param ).floatValue(); // ??
	else
	    aux = param;

	if ( !this.isAlign ) {
	    initializeAlignment();                                          //determine the elements from the initial reference alignment
	}

	if ( name.equals( ParametersIds.ADD_CLASSES ) ) { //add percentage classes
	    if ( debug ) System.err.println( "Add Class" + "[" + value + "]");
	    addSubClasses( value );
	} else if ( name.equals( ParametersIds.REMOVE_CLASSES ) ) { //remove percentage classes
	    if ( debug ) System.err.println( "Remove Class" + "[" + value + "]");
	    removeSubClasses( value );
	} else if ( name.equals( ParametersIds.REMOVE_COMMENTS ) ) { //remove percentage comments
	    if ( debug ) System.err.println( "Remove Comments" + "[" + value + "]");
	    removeComments ( value );
	} else if ( name.equals( ParametersIds.REMOVE_PROPERTIES ) ) { //remove percentage properties
	    if ( debug ) System.err.println( "Remove Property" + "[" + value + "]");
	    removeProperties ( value );
	} else if ( name.equals( ParametersIds.ADD_PROPERTIES ) ) { //add percentage properties
	    if ( debug ) System.err.println( "Add Property" + "[" + value + "]");
	    addProperties ( value );
	} else if ( name.equals( ParametersIds.ADD_CLASSESLEVEL ) ) { //recursive add nbClasses starting from level level
	    aux = ((Float)value).toString();
	    int index = aux.indexOf(".");
	    int level = Integer.valueOf( aux.substring(0, index) );
	    int nbClasses = Integer.valueOf( aux.substring(index+1, aux.length()) );
	    if ( debug ) System.err.println( "level " + level );
	    if ( debug ) System.err.println( "nbClasses " + nbClasses );
	    float percentage = 1.00f;
	    addClasses ( level, nbClasses, percentage );
	} else if ( name.equals( ParametersIds.REMOVE_CLASSESLEVEL ) ) { //remove all the classes from the level level
	    if ( debug ) System.err.println("Remove all classes from level" + (int)value );
	    removeClassesFromLevel ( (int)value );
	} else if ( name.equals( ParametersIds.LEVEL_FLATTENED ) ) { //flatten a level
	    //levelFlattened ( level );
	    levelFlattened ( (int)value );
	    if ( debug ) System.err.println( "New class hierarchy level " + getMaxLevel() ) ;
	} else if ( name.equals( ParametersIds.RENAME_CLASSES ) ) { //rename classes
	    if ( debug ) System.err.println("Rename classes" + "[" + value + "]" );
	    //if ( debug ) System.err.println("\nValue = " + value + "\n");	//activeProperties, activeClasses, ..
	    modifiedModel = renameResource ( false, true, value, true, false, false, 0);
	} else if ( name.equals( ParametersIds.RENAME_PROPERTIES ) ) { //rename properties
	    if ( debug ) System.err.println("Rename properties " + "[" + value + "]" );
	    //if ( debug ) System.err.println("\nValue = " + value + "\n");	//activeProperties, activeClasses, ..
	    modifiedModel = renameResource ( true, false, value, true, false, false, 0);
	} else if ( name.equals( ParametersIds.REMOVE_RESTRICTIONS ) ) { //remove percentage restrictions
	    if ( debug ) System.err.println("Remove restrictions" + "[" + value + "]");
	    removeRestrictions( value );
	} else if ( name.equals( ParametersIds.REMOVE_INDIVIDUALS ) ) { //remove percentage individuals
	    if ( debug ) System.err.println("Remove individuals" + "[" + value + "]");
	    removeIndividuals( value );
	} else if ( name.equals( ParametersIds.NO_HIERARCHY ) ) { //no hierarchy
	    if ( debug ) System.err.println( "NoHierarchy" );
	    noHierarchy();
	}

	//rebuild the class hierarchy every time
	this.isBuild = false;
	this.isChanged = false;
    }


}
