package fr.inrialpes.exmo.align.onto.jena25;

import java.net.URI;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.LiteralImpl;

import fr.inrialpes.exmo.align.onto.BasicOntology;
import fr.inrialpes.exmo.align.onto.LoadedOntology;

public class JENAOntology extends BasicOntology<OntModel> implements LoadedOntology<OntModel>{




    public Object getEntity(URI u) throws AlignmentException {
	return onto.getOntResource(u.toString());
    }

    public Set<String> getEntityAnnotations(Object o) throws AlignmentException {
	Set<String> annots = new HashSet<String>();
	OntResource or = (OntResource) o;
	Iterator i = or.listComments(null);
	while (i.hasNext()) {
	    annots.add(((LiteralImpl) i.next()).getLexicalForm());
	}
	i = or.listLabels(null);
	while (i.hasNext()) {
	    annots.add(((LiteralImpl) i.next()).getLexicalForm());
	}
	return annots;
    }

    public Set<String> getEntityComments(Object o, String lang) throws AlignmentException {
	Set<String> comments = new HashSet<String>();
	OntResource or = (OntResource) o;
	Iterator i = or.listComments(lang);
	while (i.hasNext()) {
	    String comment = ((LiteralImpl) i.next()).getLexicalForm();
	    comments.add(comment);
	}
	return comments;
    }

    public Set<String> getEntityComments(Object o) throws AlignmentException {
	return getEntityComments(o,null);
    }


    public String getEntityName(Object o) throws AlignmentException {
	// TODO Auto-generated method stub
	return null;
    }

    public Set<String> getEntityNames(Object o, String lang) throws AlignmentException {
	Set<String> labels = new HashSet<String>();
	OntResource or = (OntResource) o;
	Iterator i = or.listLabels(lang);
	while (i.hasNext()) {
	    String label = ((LiteralImpl) i.next()).getLexicalForm();
	    labels.add(label);
	}
	return labels;
    }

    public Set<String> getEntityNames(Object o) throws AlignmentException {
	return getEntityNames(o,null);
    }

    public URI getEntityURI(Object o) throws AlignmentException {
	try {
	    OntResource or = (OntResource) o;
	    return new URI(or.getURI());
	} catch (Exception e) {
	    throw new AlignmentException(o.toString()+" do not have uri", e );
	}
    }

    protected Set<OntResource> getEntitySet(final Iterator<OntResource> i) {
	return  new AbstractSet<OntResource>() {
	    private int size=-1;
	    public Iterator<OntResource> iterator() {
		return new JENAEntityIt(getURI(),i);
	    }
	    public int size() {
		if (size==-1) {
		    for (OntResource r : this)
			size++;
		    size++;
		}
		return size;
	    }
	};
    }

    @SuppressWarnings("unchecked")
    public Set<?> getClasses() {
	return getEntitySet(onto.listNamedClasses());
    }

    @SuppressWarnings("unchecked")
    public Set<?> getDataProperties() {
	return getEntitySet(onto.listDatatypeProperties());
    }

    @SuppressWarnings("unchecked")
    public Set<?> getEntities() {
	return getEntitySet(onto.listObjectProperties().andThen(onto.listDatatypeProperties()).
		andThen(onto.listIndividuals()).andThen(onto.listClasses()));
    }

    @SuppressWarnings("unchecked")
    public Set<?> getIndividuals() {
	return getEntitySet(onto.listIndividuals());
    }

    @SuppressWarnings("unchecked")
    public Set<?> getObjectProperties() {
	return getEntitySet(onto.listObjectProperties());
    }

    @SuppressWarnings("unchecked")
    public Set<?> getProperties() {
	return getEntitySet(onto.listObjectProperties().andThen(onto.listDatatypeProperties()));
    }

    public boolean isClass(Object o) {
	return o instanceof OntClass;
    }

    public boolean isDataProperty(Object o) {
	return o instanceof DatatypeProperty;
    }

    public boolean isEntity(Object o) {
	return isClass(o)||isProperty(o)||isIndividual(o);
    }

    public boolean isIndividual(Object o) {
	return o instanceof Individual;
    }

    public boolean isObjectProperty(Object o) {
	return o instanceof ObjectProperty;
    }

    public boolean isProperty(Object o) {
	return o instanceof OntProperty;
    }

    public int nbClasses() {
	return this.getClasses().size();
    }

    public int nbDataProperties() {
	return this.getDataProperties().size();
    }

    public int nbInstances() {
	return this.getIndividuals().size();
    }

    public int nbObjectProperties() {
	return this.getObjectProperties().size();
    }

    public int nbProperties() {
	return this.getProperties().size();
    }

    public void unload() {

    }


}
