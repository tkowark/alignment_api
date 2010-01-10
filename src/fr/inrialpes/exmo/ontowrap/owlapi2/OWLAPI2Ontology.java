/*
 * $Id$
 *
 * Copyright (C) INRIA, 2008-2009
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

package fr.inrialpes.exmo.align.onto.owlapi2;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLNaryBooleanDescription;
import org.semanticweb.owl.model.OWLCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.vocab.OWLRDFVocabulary;

import fr.inrialpes.exmo.align.onto.BasicOntology;
import fr.inrialpes.exmo.align.onto.OntologyFactory;
import fr.inrialpes.exmo.align.onto.HeavyLoadedOntology;

public class OWLAPI2Ontology extends BasicOntology<OWLOntology> implements
	HeavyLoadedOntology<OWLOntology> {

    public OWLAPI2Ontology() {
    }

    public Set<? extends Object> getClasses() {
	return onto.getReferencedClasses();
    }

    public Set<? extends Object> getDataProperties() {
	return onto.getReferencedDataProperties();
    }

    public Set<? extends Object> getEntities() {
	return onto.getReferencedEntities();
    }

    public Object getEntity(URI u) throws AlignmentException {
	for (OWLEntity e : onto.getReferencedEntities()) {
	    if (e.getURI().equals(u))
		return e;
	}
	return null;
    }

    protected Set<String> getEntityAnnotations(Object o, URI type, String lang) {
	OWLEntity ent = (OWLEntity) o;
	Set<String> annots = new HashSet<String>();
	Set<OWLAnnotation> owlAnnots;
	if (type==null)
	    owlAnnots = ent.getAnnotations(onto);
	else
	    owlAnnots = ent.getAnnotations(onto, type);
	for (OWLAnnotation annot : owlAnnots) {
	    OWLConstant c = annot.getAnnotationValueAsConstant();
	    if (lang == null || c.asOWLUntypedConstant().hasLang(lang))
		annots.add(c.getLiteral());
	}
	return annots;
    }

    public Set<String> getEntityAnnotations(Object o) throws AlignmentException {
	return getEntityAnnotations(o, null, null);
    }

    public Set<String> getEntityComments(Object o, String lang)
	    throws AlignmentException {
	return getEntityAnnotations(o, OWLRDFVocabulary.RDFS_COMMENT.getURI(),
		lang);
    }

    public Set<String> getEntityComments(Object o) throws AlignmentException {
	return getEntityAnnotations(o, OWLRDFVocabulary.RDFS_COMMENT.getURI(),
		null);
    }

    public String getEntityName(Object o) throws AlignmentException {
	try {
	    // Should try to get the label first
	    return getFragmentAsLabel( ((OWLEntity) o).getURI() );
	} catch (ClassCastException e) {
	    throw new AlignmentException(o+" is not an entity for "+ onto.getURI());
	}
    }

    public String getEntityName( Object o, String lang ) throws AlignmentException {
	// Should first get the label in the language
	return getEntityName( o );
    }

    public Set<String> getEntityNames(Object o, String lang) throws AlignmentException {
	return getEntityAnnotations(o, OWLRDFVocabulary.RDFS_LABEL.getURI(), lang);
    }

    public Set<String> getEntityNames(Object o) throws AlignmentException {
	Set<String> result = getEntityAnnotations(o, OWLRDFVocabulary.RDFS_LABEL.getURI(), null);
	if ( result == null ) {
	    String label = getEntityName( o );
	    if ( label != null ) {
		result = new HashSet();
		result.add( label );
	    }
	}
	return result;
    }

    public URI getEntityURI(Object o) throws AlignmentException {
	try {
	    return ((OWLEntity) o).getURI();
	} catch (ClassCastException e) {
	    throw new AlignmentException(o + " is not an entity for "
		    + onto.getURI());
	}
    }

    public Set<? extends Object> getIndividuals() {
	return onto.getReferencedIndividuals();
    }

    public Set<? extends Object> getObjectProperties() {
	//System.err.println ( "ONTO: "+onto );
	return onto.getReferencedObjectProperties();
    }

    public Set<? extends Object> getProperties() {
	Set<OWLDataProperty> dtProp = onto.getReferencedDataProperties();
	Set<OWLObjectProperty> oProp = onto.getReferencedObjectProperties();
	Set<Object> prop = new HashSet<Object>(dtProp.size() + oProp.size());
	prop.addAll(dtProp);
	prop.addAll(oProp);
	return prop;
    }

    public boolean isClass(Object o) {
	return o instanceof OWLClass;
    }

    public boolean isDataProperty(Object o) {
	return o instanceof OWLDataProperty;
    }

    public boolean isEntity(Object o) {
	return o instanceof OWLEntity;
    }

    public boolean isIndividual(Object o) {
	return o instanceof OWLIndividual;
    }

    public boolean isObjectProperty(Object o) {
	return o instanceof OWLObjectProperty;
    }

    public boolean isProperty(Object o) {
	return o instanceof OWLProperty;
    }

    public int nbClasses() {
	return onto.getReferencedClasses().size();
    }

    public int nbDataProperties() {
	return onto.getReferencedDataProperties().size();
    }

    public int nbIndividuals() {
	return onto.getReferencedIndividuals().size();
    }

    public int nbObjectProperties() {
	return onto.getReferencedObjectProperties().size();
    }

    public int nbProperties() {
	return onto.getReferencedDataProperties().size()
		+ onto.getReferencedObjectProperties().size();
    }

    int nbentities = -1;

    public int nbEntities() {
	if ( nbentities != -1 ) return nbentities;
	nbentities = nbClasses()+nbProperties()+nbIndividuals();
	return nbentities;
    }

    // This is the HeavyLoadedOntology interface
    // JE NOT FULLY IMPLEMENTED YET...

    /* Capability methods */
    // [//TODO]
    public boolean getCapabilities( int Direct, int Asserted, int Named ){
	return true;
    };

    /* Class methods */
    // Pretty inefficient but nothing seems to be stored
    public Set<Object> getSubClasses( Object cl, int local, int asserted, int named ){
	Set<Object> sbcl = new HashSet<Object>();
	for( Object c : getClasses() ) {
	    if ( getSuperClasses( (OWLClass)c, local, asserted, named ).contains( cl ) ) sbcl.add( c );
	}
	return sbcl;
    };
    public Set<Object> getSuperClasses( Object cl, int local, int asserted, int named ){
	Set<Object> spcl = new HashSet<Object>();
	if ( asserted == OntologyFactory.ASSERTED ){
	    for( Object rest : ((OWLClass)cl).getSuperClasses( getOntology() ) ){
		if (rest instanceof OWLClass) spcl.add( rest );
	    }
	} else {
	    // JE: I do not feel that this is really correct
	    Set<Object> sup = new HashSet<Object>();
	    for( Object rest : ((OWLClass)cl).getSuperClasses( getOntology() ) ){
		if (rest instanceof OWLClass) {
		    spcl.add( rest );
		    sup.add( rest );
		}
	    }
	}
	return spcl;
    };
    /*
     * In the OWL API, there is no properties: there are SuperClasses which are restrictions
     */
    public Set<Object> getProperties( Object cl, int local, int asserted, int named ){
	Set<Object> prop = new HashSet<Object>();
	if ( asserted == OntologyFactory.ASSERTED && local == OntologyFactory.LOCAL ) {
	    for ( Object ent : ((OWLClass)cl).getSuperClasses( getOntology() ) ){
		if ( ent instanceof OWLRestriction ) 
		    prop.add( ((OWLRestriction)ent).getProperty() );
	    }
	} else {
	    prop = getInheritedProperties( (OWLClass)cl );
	}
	return prop;
    };
    // Not very efficient: 2n instead of n
    public Set<Object> getDataProperties( Object c, int local, int asserted, int named ){
	Set<Object> props = new HashSet<Object>();
	for ( Object p : getProperties( c, local, asserted, named )  ){
	    if ( p instanceof OWLDataProperty ) props.add( p );
	}
	return props;
    };
    // Not very efficient: 2n instead of n
    public Set<Object> getObjectProperties( Object c, int local, int asserted, int named ){
	Set<Object> props = new HashSet<Object>();
	for ( Object p : getProperties( c, local, asserted, named )  ){
	    if ( p instanceof OWLObjectProperty ) props.add( p );
	}
	return props;
    };
    // Pretty inefficient but nothing seems to be stored
    public Set<Object> getInstances( Object cl, int local, int asserted, int named  ){
	Set<Object> sbcl = new HashSet<Object>();
	for( Object i : getIndividuals() ) {
	    //if ( getClasses( (OWLIndividual)i, local, asserted, named ).contains( cl ) ) sbcl.add( i );
	    if ( ((OWLIndividual)i).getTypes( getOntology() ).contains( cl ) ) sbcl.add( i );
	}
	return sbcl;
    };

    /* Property methods */
    // Pretty inefficient but nothing seems to be stored
    public Set<Object> getSubProperties( Object pr, int local, int asserted, int named ){
	Set<Object> sbpr = new HashSet<Object>();
	for( Object p : getProperties() ) {
	    if ( getSuperProperties( (OWLProperty)p, local, asserted, named ).contains( pr ) ) sbpr.add( p );
	}
	return sbpr;
    };
    public Set<Object> getSuperProperties( Object pr, int local, int asserted, int named ){
	Set<Object> supers = new HashSet<Object>();
	if ( asserted == OntologyFactory.ASSERTED ){
	    for( Object rest : ((OWLProperty)pr).getSuperProperties( getOntology() ) ){
		if (rest instanceof OWLProperty) supers.add( rest );
	    }
	} else {
	    Set<Object> sup = new HashSet<Object>();
	    for( Object rest : ((OWLProperty)pr).getSuperProperties( getOntology() ) ){
		if (rest instanceof OWLProperty) {
		    sup.add( rest );
		    supers.add( rest );
		}
	    }
	}
	return supers;
    };
    public Set<Object> getRange( Object p, int asserted ){
	Set resultSet = new HashSet(); 
	for ( Object ent : ((OWLProperty)p).getRanges( getOntology() ) ){
	    // Not correct
	    // Could be something else than class
	    if ( ent instanceof OWLClass || ent instanceof OWLDataType ) {
		resultSet.add( ent );
	    }
	}
	return resultSet;
    };
    public Set<Object> getDomain( Object p, int asserted ){
	Set resultSet = new HashSet(); 
	for ( Object ent : ((OWLProperty)p).getDomains( getOntology() ) ){
	    // Not correct
	    // Could be something else than class
	    if ( ent instanceof OWLClass ) {
		resultSet.add( ent );
	    }
	}
	return resultSet;
    };

    /* Individual methods */
    public Set<Object> getClasses( Object i, int local, int asserted, int named ){
	Set<Object> supers = null;
	if ( i instanceof OWLIndividual )
	    for ( OWLDescription d : ((OWLIndividual)i).getTypes( getOntology() ) ){
		if ( d instanceof OWLClass ) supers.add( (OWLClass)d );
	    }
	if ( local == OntologyFactory.LOCAL ) {
	    return (Set<Object>)supers; 
	} else {
	    // inherits the superclasses (unless we have to reduce them...)
	    Set<Object> newsupers = new HashSet<Object>();
	    for ( Object cl : supers ){
		newsupers.addAll( getSuperClasses( cl, local, asserted, named ) );
	    }
	    newsupers.addAll( supers ); // maybe useful
	    return newsupers;
	}
    };

    /*
     * Inherits all properties of a class
     */
    private Set<Object> getInheritedProperties( OWLClass cl ) {
	Set resultSet = new HashSet(); 
	try { getProperties( cl, resultSet, new HashSet()); }
	catch (OWLException ex) {};
	return resultSet;
    }

    /* This traverse properties */
    public void getProperties( OWLDescription desc, Set<Object> list, Set<Object> visited ) throws OWLException {
	if ( desc instanceof OWLRestriction ){
	    //getProperties( (OWLRestriction)desc, list );
	    list.add( ((OWLRestriction)desc).getProperty() );
	} else if ( desc instanceof OWLClass ) {
	    getProperties( (OWLClass)desc, list, visited );
	} else if ( desc instanceof OWLNaryBooleanDescription ) {
	    for ( Object d : ((OWLNaryBooleanDescription)desc).getOperands() ){
		getProperties( (OWLDescription)d, list, visited );
	    }
	    //getProperties( (OWLNaryBooleanDescription)desc, list, visited );
	}
    }
    public void getProperties( OWLRestriction rest, Set<Object> list, Set<Object> visited ) throws OWLException {
	list.add( (Object)rest.getProperty() );
    }
    public void getProperties( OWLNaryBooleanDescription d, Set<Object> list, Set<Object> visited ) throws OWLException {
	for ( OWLDescription desc : d.getOperands() ){
	    getProperties( desc, list, visited );
	}
    }
    public void getProperties( OWLClass cl, Set<Object> list, Set<Object> visited ) throws OWLException {
	for ( Object desc : cl.getSuperClasses( getOntology() ) ){
	    getProperties( (OWLDescription)desc, list, visited );
	}
	for ( Object desc : cl.getEquivalentClasses( getOntology() ) ){
	    if ( !visited.contains( desc ) ) {
		visited.add( desc );
		getProperties( (OWLDescription)desc, list, visited );
	    }
	}
    }

    // JD: it is hazardous...
    public void unload() {
	onto = null;
    }


}
