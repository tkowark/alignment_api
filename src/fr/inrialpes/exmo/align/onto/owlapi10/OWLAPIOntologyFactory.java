/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2008
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.onto.owlapi10;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import org.xml.sax.SAXException;

import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.onto.OntologyCache;
import fr.inrialpes.exmo.align.onto.OntologyFactory;
import fr.inrialpes.exmo.align.onto.EntityAdapter;
import fr.inrialpes.exmo.align.onto.Entity;
import fr.inrialpes.exmo.align.onto.EntityAdapter.TYPE;
import fr.inrialpes.exmo.align.onto.Ontology;
import fr.inrialpes.exmo.align.onto.ConcreteOntology;
import fr.inrialpes.exmo.align.onto.LoadedOntology;

import org.semanticweb.owl.io.vocabulary.RDFSVocabularyAdapter;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.helper.OWLEntityCollector;
import org.semanticweb.owl.util.OWLConnection;
import org.semanticweb.owl.util.OWLManager;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.io.owl_rdf.OWLRDFErrorHandler;

public class OWLAPIOntologyFactory extends OntologyFactory {

    private URI formalismUri = null;
    private String formalismId = "OWL1.0";

    public OWLAPIOntologyFactory() {
	try {
	    formalismUri = new URI("http://www.w3.org/2002/07/owl#");
	} catch (URISyntaxException ex) { ex.printStackTrace(); } // should not happen
    };

    public static OntologyFactory getInstance() {
	if (instance == null || !(instance instanceof OWLAPIOntologyFactory))
	    instance = new OWLAPIOntologyFactory();
	return instance;
    }

    public LoadedOntology loadOntology( URI physicalURI ) {
	OWLConnection connection = null;
	Map parameters = new HashMap();
	parameters.put(OWLManager.OWL_CONNECTION,
		       "org.semanticweb.owl.impl.model.OWLConnectionImpl");
	try {
	    connection = OWLManager.getOWLConnection(parameters);
	    Level lev = Logger.getLogger("org.semanticweb.owl").getLevel();
	    Logger.getLogger("org.semanticweb.owl").setLevel(Level.ERROR);
	    OWLOntology ontology = connection.loadOntologyPhysical(physicalURI);
	    Logger.getLogger("org.semanticweb.owl").setLevel(lev);
	    OWLAPIOntology onto = new OWLAPIOntology();
	    // It may be possible to fill this as final in OWLAPIOntology...
	    onto.setFormalism( formalismId );
	    onto.setFormURI( formalismUri );
	    onto.setOntology( ontology );
	    onto.setFile( physicalURI );
	    try {
		onto.setURI( ontology.getLogicalURI() );//getURI();
	    } catch (OWLException e) {
		// Better put in the AlignmentException of loaded
		e.printStackTrace();
	    }
	    return onto;
	} catch (OWLException e) {
	    e.printStackTrace();
	}
	return null;
    }

    /** Can be used for loading the ontology if it is not available **/
    // JE: Onto: check that the structure is properly filled (see build...)
    public Ontology loadOntology( URI ref, OntologyCache ontologies ) throws SAXException, AlignmentException {
	Ontology onto = null;
	if ( (ontologies != null) && ( ontologies.getOntology( ref ) != null ) ) {
	} else if ( onto != null ) {
	} else {
	    OWLOntology parsedOnt = null;
	    try {
		OWLRDFParser parser = new OWLRDFParser();
		OWLRDFErrorHandler handler = new OWLRDFErrorHandler(){
			public void owlFullConstruct( int code, String message ) 
			    throws SAXException {
			}
			public void owlFullConstruct(int code, String message, Object o)
			    throws SAXException {
			}
			public void error( String message ) throws SAXException {
			    throw new SAXException( message.toString() );
			}
			public void warning( String message ) throws SAXException {
			    System.err.println("WARNING: " + message);
			}
		    };
		Level lev = Logger.getLogger("org.semanticweb.owl").getLevel();
		Logger.getLogger("org.semanticweb.owl").setLevel(Level.ERROR);
		parser.setOWLRDFErrorHandler( handler );
		parser.setConnection( OWLManager.getOWLConnection() );
		parsedOnt = parser.parseOntology( ref );
		Logger.getLogger("org.semanticweb.owl").setLevel(lev);
	    } catch (OWLException ex) {
		throw new AlignmentException( "Cannot load ontology "+ref, ex );
	    }
	    // THIS SHOULD NOW BE DONE WHEN CREATING THE ONTOLOGY
	    //setOntology( parsedOnt );
	    //if ( ontologies != null ) ontologies.recordOntology( ref, this );
	    //onto = parsedOnt;
	}
	return onto;
    }

    // JE: Onto: I am not sure of the meaning of this "get"
    /*
	private Ontology getOntology(OWLOntology ont) {
	    // JE: Onto
	    //OntologyAdapter<OWLOntology> oModel = new  OWLAPIOntology();
	    Ontology oModel = new  OWLAPIOntology();
	    oModel.ontology=ont;
	    try {
		oModel.uri=ont.getLogicalURI();//getURI();
	    } catch (OWLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    oModel.classes = getEntities(oModel,OWLClass.class);
	    oModel.properties = getEntities(oModel,OWLProperty.class);
	    //oModel.individuals = getEntities(oModel,OWLIndividual.class);
	    oModel.entities = new HashSet<Entity>(oModel.properties);
	    oModel.entities.addAll(oModel.classes);
	    //oModel.entities.addAll(oModel.individuals);

	    //oModel.classes = getEntities(oModel,OWLEntity.class);
	    return oModel;
	}
    */

    private Set<Entity> getEntities(LoadedOntology<OWLOntology> ont, Class<? extends OWLEntity> c){
	OWLEntityCollector ec = new OWLEntityCollector();
	try {
	    ont.getOntology().accept(ec);
	    Set<Entity> entities = new HashSet<Entity>();
	    for (Object obj : ec.entities()) {
		if (c.isInstance(obj) && (((OWLEntity) obj).getURI() != null) &&
		    ((OWLEntity) obj).getURI().getSchemeSpecificPart().equals(ont.getURI().getSchemeSpecificPart())){
		    //System.out.println(((OWLEntity) obj).getURI().getSchemeSpecificPart()+" - "+ont.getURI().getSchemeSpecificPart());
		    //System.out.println(((OWLEntity) obj).getURI()+" : "+ont.getOntology().contains(obj));
		    entities.add(new OWLAPIEntity((OWLEntity)obj, ont, ((OWLEntity)obj).getURI() ));
		}
	    }
	    return entities;
	} catch (OWLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return null;
    }

    private void addAnnotations(OWLAPIEntity ent) {
	for (String[] a : getAnnotations(ent.getObject(),(OWLOntology)ent.getOntology().getOntology())) {
	    ent.getAnnotations().add(a[0]);
	    if (a[1]!=null) {
		ent.getLangToAnnot(EntityAdapter.LANGUAGES.valueOf(a[1])).add(a[0]);
	    }
	    if (a[2].equals(RDFSVocabularyAdapter.INSTANCE.getComment())) {
		ent.getTypeToAnnot(EntityAdapter.TYPE.comment).add(a[0]);
	    }
	    else if (a[2].equals(RDFSVocabularyAdapter.INSTANCE.getLabel())) {
		ent.getTypeToAnnot(EntityAdapter.TYPE.comment).add(a[0]);
	    }
	}
    }


	private Set<String[]> getAnnotations(OWLEntity e, OWLOntology o)  {
		Set<String[]> res = new HashSet<String[]>();
		Set annots;
		try {
			annots = e.getAnnotations(o);
			for (Object annot : annots) {
				OWLAnnotationInstance annInst = ((OWLAnnotationInstance) annot);
				String val;
				String lang=null;
				String annotUri = annInst.getProperty().getURI().toString();
				if ( annInst.getContent() instanceof OWLDataValue ) {
				    OWLDataValue odv = (OWLDataValue) annInst.getContent();
				    val = odv.toString();
				    lang = odv.getLang();
				}
				else
					val= (String) annInst.getContent();
				res.add(new String[]{val,lang,annotUri});
			}
		}
		catch (OWLException oe) {
			oe.printStackTrace();
		}
		return Collections.unmodifiableSet(res);
	}


    private class OWLAPIEntity extends EntityAdapter<OWLEntity> {
	//private boolean toLoad=true;

	private OWLAPIEntity() { super(false); }
	private OWLAPIEntity(OWLEntity e, Ontology o, URI u ) {
	    super( e, o, u, false);
	}

	public Set<String> getAnnotations(String lang, TYPE type) {
	    if (this.annotations==null) {
		init();
		addAnnotations(this);
		//toLoad=false;
	    }
	    return super.getAnnotations(lang, type);
	}
    }

}
