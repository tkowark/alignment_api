/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2007-2008
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

/*
 * This should be turned into an HeavyLoadedOntology.
 * Some primitives are already avalible below
 *
 *
 */

package fr.inrialpes.exmo.align.onto.owlapi10;

import java.net.URI;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.onto.LoadedOntology;
import fr.inrialpes.exmo.align.onto.BasicOntology;

import org.semanticweb.owl.io.vocabulary.RDFSVocabularyAdapter;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLNaryBooleanDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.helper.OWLEntityCollector;

/*
 * JE: BEWARE, THIS HAS NOT BEEN FULLY IMPLEMENTED SO FAR!!!!
 * TO BE CHECKED
 * getEntities() and getProperties() ARE INCORRECT
 * nbXxxx() ARE INNEFICIENT
 */

/**
 * Store the information regarding ontologies in a specific structure
 * Acts as an interface with regard to an ontology APY
 */

public class OWLAPIOntology extends BasicOntology<OWLOntology> implements LoadedOntology<OWLOntology> {

    public OWLAPIOntology() {
	setFormalism( "OWL1.0" );
	try {
	    setFormURI( new URI("http://www.w3.org/2002/07/owl#") );
	} catch (Exception e) {}; // does not happen
    };

    // Ontology interface
    public OWLOntology getOntology() { return onto; }

    public void setOntology( OWLOntology o ) { this.onto = o; }

    // LoadedOntology interface
    public Object getEntity( URI uri ) throws AlignmentException {
	try {
	    OWLEntity result = ((OWLOntology)onto).getClass( uri );
	    if ( result == null ) result = ((OWLOntology)onto).getDataProperty( uri );
	    if ( result == null ) result = ((OWLOntology)onto).getObjectProperty( uri );
	    if ( result == null ) result = ((OWLOntology)onto).getIndividual( uri );
	    return result;
	} catch (OWLException ex) {
	    throw new AlignmentException( "Cannot dereference URI : "+uri );
	}
    }

    public URI getEntityURI( Object o ) throws AlignmentException {
	try {
	    return ((OWLEntity)o).getURI();
	} catch (OWLException oex) {
	    throw new AlignmentException( "Cannot get URI ", oex );
	}
    }
    public String getEntityName( Object o ) throws AlignmentException {
	try {
	    // Try to get labels first...
	    URI u = ((OWLEntity)o).getURI();
	    if ( u != null ) return u.getFragment();
	    else return "";
	} catch (OWLException oex) {
	    return null;
	}
    };


    protected Set<String> getAnnotations(OWLEntity e , String lang , String typeAnnot ) throws OWLException {
	Set<String> annots = new HashSet<String>();
	for (Object objAnnot :  e.getAnnotations(onto)) {
	    OWLAnnotationInstance annot = (OWLAnnotationInstance) objAnnot;
		String annotUri = annot.getProperty().getURI().toString();
		if (annotUri.equals(typeAnnot) || typeAnnot==null) {
        		if ( annot.getContent() instanceof OWLDataValue &&
        			( lang==null || ((OWLDataValue) annot.getContent()).getLang().equals(lang)) ) {
        		    annots.add(((OWLDataValue) annot.getContent()).getValue().toString());
        		}
		}
	}
	return annots;
    }

    public Set<String> getEntityNames( Object o , String lang ) throws AlignmentException {
	try {
	    OWLEntity e = ((OWLEntity) o);
	    return getAnnotations(e,lang,RDFSVocabularyAdapter.INSTANCE.getLabel());
	} catch (OWLException oex) {
	    return null;
	}
    }
    public Set<String> getEntityNames( Object o ) throws AlignmentException {
	try {
	    OWLEntity e = ((OWLEntity) o);
	    return getAnnotations(e,null,RDFSVocabularyAdapter.INSTANCE.getLabel());
	} catch (OWLException oex) {
	    return null;
	}
    };


    public Set<String> getEntityComments( Object o , String lang ) throws AlignmentException {
	try {
	    OWLEntity e = ((OWLEntity) o);
	    return getAnnotations(e,lang,RDFSVocabularyAdapter.INSTANCE.getComment());
	} catch (OWLException oex) {
	    return null;
	}
    };

    public Set<String> getEntityComments( Object o ) throws AlignmentException {
	try {
	    OWLEntity e = ((OWLEntity) o);
	    return getAnnotations(e,null,RDFSVocabularyAdapter.INSTANCE.getComment());
	} catch (OWLException oex) {
	    return null;
	}
    };

    public Set<String> getEntityAnnotations( Object o ) throws AlignmentException {
	try {
	    return getAnnotations(((OWLEntity) o),null,null);
	} catch (OWLException oex) {
	    return null;
	}
    };

    public boolean isEntity( Object o ){
	if ( o instanceof OWLEntity ) return true;
	else return false;
    };
    public boolean isClass( Object o ){
	if ( o instanceof OWLClass ) return true;
	else return false;
    };
    public boolean isProperty( Object o ){
	if ( o instanceof OWLProperty ) return true;
	else return false;
    };
    public boolean isDataProperty( Object o ){
	if ( o instanceof OWLDataProperty ) return true;
	else return false;
    };
    public boolean isObjectProperty( Object o ){
	if ( o instanceof OWLObjectProperty ) return true;
	else return false;
    };
    public boolean isIndividual( Object o ){
	if ( o instanceof OWLIndividual ) return true;
	else return false;
    };

    // JD: allows to retrieve some specific entities by giving their class
    protected Set<?> getEntities(Class<? extends OWLEntity> c) throws OWLException{
        OWLEntityCollector ec = new OWLEntityCollector();
    	onto.accept(ec);
    	Set<Object> entities = new HashSet<Object>();
	for (Object obj : ec.entities()) {
	    // JD: OWLEntitytCollector seems to return anonymous entities :&& ((OWLEntity)obj).getURI()!=null
	    if (c.isInstance(obj)  ){
		entities.add(obj);
	    }
	}
	return entities;
    }

    // Here it shoud be better to report exception
    // JE: Onto this does not work at all, of course...!!!!
    public Set<?> getEntities() {
	try {
	    return getEntities(OWLEntity.class);
	} catch (OWLException ex) {
	    return null;
	}
    }

    public Set<?> getClasses() {
	try {
	    return ((OWLOntology)onto).getClasses(); // [W:unchecked]
	} catch (OWLException ex) {
	    return null;
	}
    }

    public Set<?> getProperties() {
	try {
	    //return ((OWLOntology)onto).getProperties(); // [W:unchecked]
	    return getEntities(OWLProperty.class);
	} catch (OWLException ex) {
	    return null;
	}
    }

    // The only point is if I should return OWLProperties or names...
    // I guess that I will return names (ns+name) because otherwise I will need a full
    // Abstract ontology interface and this is not the rule...
    public Set<?> getObjectProperties() {
	try {
	    // [Warning:unchecked] due to OWL API not serving generic types
	    return ((OWLOntology)onto).getObjectProperties(); // [W:unchecked]
	} catch (OWLException ex) {
	    return null;
	}
    }

    public Set<?> getDataProperties() {
	try {
	    return ((OWLOntology)onto).getDataProperties(); // [W:unchecked]
	} catch (OWLException ex) {
	    return null;
	}
    }

    public Set<?> getIndividuals() {
	try {
	    return ((OWLOntology)onto).getIndividuals(); // [W:unchecked]
	} catch (OWLException ex) {
	    return null;
	}
    }

    /*
    private Set<Object> getEntities( Class<? extends OWLEntity> c ){
	OWLEntityCollector ec = new OWLEntityCollector();
	try {
	    getOntology().accept(ec);
	    Set<Object> entities = new HashSet<Object>();
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
    */
    /*
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
    */
    /*
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
    */

    // JE: particularly inefficient
    public int nbClasses() {
	try {
	    return ((OWLOntology)onto).getClasses().size();
	} catch (OWLException oex) {
	    return 0;
	}
    }

    public int nbProperties() {
	return nbObjectProperties()+nbDataProperties();
    }

    public int nbObjectProperties() {
	try {
	    return ((OWLOntology)onto).getObjectProperties().size();
	} catch (OWLException oex) {
	    return 0;
	}
    }

    public int nbDataProperties() {
	try {
	    return ((OWLOntology)onto).getDataProperties().size();
	} catch (OWLException oex) {
	    return 0;
	}
    }

    public int nbInstances() {
	try {
	    return ((OWLOntology)onto).getIndividuals().size();
	} catch (OWLException oex) {
	    return 0;
	}
    }

    /* HeavyLoadedOntology specifics */

    public Set<?> getProperties( OWLDescription desc ) {
	Set<Object> list = new HashSet<Object>();
	try {
	    if ( desc instanceof OWLRestriction ){
		list.add( ((OWLRestriction)desc).getProperty() );
	    } else if ( desc instanceof OWLClass ) {
		// JE: I suspect that this can be a cause for looping!!
		for ( Object cl : ((OWLClass)desc).getEquivalentClasses((OWLOntology)onto) ){
		    // JE: strange casting
		    Set<?> res = getProperties( (OWLDescription)cl );
		    if ( res != null ) list.add( res );
		}
	    } else if ( desc instanceof OWLNaryBooleanDescription ) {
		for ( Object d : ((OWLNaryBooleanDescription)desc).getOperands() ){
		    // JE: strange casting
		    Set<?> res = getProperties( (OWLDescription)d );
		    if ( res != null ) list.add( res );
		}
	    }
	} catch (OWLException e) { e.printStackTrace();	}
	return list;
    }

    public void unload() {
	try {
	    ((OWLOntology)onto).getOWLConnection().notifyOntologyDeleted( ((OWLOntology)onto) );
	} catch (OWLException ex) { System.err.println(ex); };
    }

}
