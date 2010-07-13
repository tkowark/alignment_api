package fr.inrialpes.exmo.ontowrap.skosapi;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class JenaSKOSThesaurus implements HeavyLoadedOntology<Model> {

    protected final static String SKOS_NS="http://www.w3.org/2004/02/skos/core#";
    protected final static String SKOS_CONCEPT=SKOS_NS+"Concept";
    protected final static String SKOS_PREFLABEL=SKOS_NS+"prefLabel";
    protected final static String SKOS_NOTE=SKOS_NS+"note";
    protected final static String SKOS_NOTATION=SKOS_NS+"notation";
    protected final static String SKOS_BROADERTRANSITIVE=SKOS_NS+"broaderTransitive";
    protected final static String SKOS_NARROWERTRANSITIVE=SKOS_NS+"narrowerTransitive";
    
    
    
   
    protected final InfModel ontoInf;
    protected final Model onto;
    
    
    public JenaSKOSThesaurus(URI file) {
	onto=ModelFactory.createDefaultModel();
	onto.read(file.toString());
	onto.read((new File("/Users/jerome/Recherche/tae/skos.rdf")).toURI().toString());
	this.ontoInf=ModelFactory.createRDFSModel(onto);
	
    }

    @Override
    public boolean getCapabilities(int Direct, int Asserted, int Named) throws OntowrapException {
	// TODO Auto-generated method stub
	return true;
    }

    @Override
    public Set<? extends Object> getClasses(Object i, int local, int asserted, int named) throws OntowrapException {
	return Collections.emptySet();
    }

    @Override
    public Set<? extends Object> getDataProperties(Object c, int local, int asserted, int named) throws OntowrapException {
	return Collections.emptySet();
    }

    @Override
    public Set<? extends Object> getDomain(Object p, int asserted) throws OntowrapException {
	return Collections.emptySet();
    }

    @Override
    public Set<? extends Object> getInstances(Object c, int local, int asserted, int named) throws OntowrapException {
	return Collections.emptySet();
    }

    @Override
    public Set<? extends Object> getObjectProperties(Object c, int local, int asserted, int named) throws OntowrapException {
	return Collections.emptySet();
    }

    @Override
    public Set<? extends Object> getProperties(Object c, int local, int asserted, int named) throws OntowrapException {
	return Collections.emptySet();
    }

    @Override
    public Set<? extends Object> getRange(Object p, int asserted) throws OntowrapException {
	return Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    public <E> Set<E> getSubClasses(E c, int local, int asserted, int named) {
	HashSet<E> sub = new HashSet<E>(); 
	//System.out.println(c);
	StmtIterator it =ontoInf.listStatements(null,ontoInf.getProperty(SKOS_BROADERTRANSITIVE),(Resource) c);
	while (it.hasNext()) {
	    Statement st = it.next();
	    //System.out.println("\t"+st.getSubject());
	    sub.add((E)st.getSubject());
	}
	//System.out.println("\tSUITTE");
	it =ontoInf.listStatements((Resource) c,ontoInf.getProperty(SKOS_NARROWERTRANSITIVE),(RDFNode)null);
	while (it.hasNext()) {
	    Statement st = it.next();
	    //System.out.println("\t"+st.getObject());
	    sub.add((E)st.getObject());
	}
	return sub;
    }

    @Override
    public Set<? extends Object> getSubProperties(Object p, int local, int asserted, int named) throws OntowrapException {
	return Collections.emptySet();
    }

    @Override
    public Set<? extends Object> getSuperClasses(Object c, int local, int asserted, int named) throws OntowrapException {
	HashSet<Object> sub = new HashSet<Object>(); 
	//System.out.println(c);
	StmtIterator it =ontoInf.listStatements(null,ontoInf.getProperty(SKOS_NARROWERTRANSITIVE),(Resource) c);
	while (it.hasNext()) {
	    Statement st = it.next();
	    //System.out.println("\t"+st.getSubject());
	    sub.add(st.getSubject());
	}
	//System.out.println("\tSUITTE");
	it =ontoInf.listStatements((Resource) c,ontoInf.getProperty(SKOS_BROADERTRANSITIVE),(RDFNode)null);
	while (it.hasNext()) {
	    Statement st = it.next();
	    //System.out.println("\t"+st.getObject());
	    sub.add(st.getObject());
	}
	return sub;
    }

    @Override
    public Set<? extends Object> getSuperProperties(Object p, int local, int asserted, int named) throws OntowrapException {
	return Collections.emptySet();
    }

    
    public Set<? extends Object> getClasses() throws OntowrapException {
	ResIterator resIt = ontoInf.listSubjectsWithProperty(RDF.type, ontoInf.getResource(SKOS_CONCEPT));
	return resIt.toSet();
    }

    @Override
    public Set<? extends Object> getDataProperties() throws OntowrapException {
	return Collections.emptySet();
    }

    @Override
    public Set<? extends Object> getEntities() throws OntowrapException {
	return this.getClasses();
    }

    @Override
    public Object getEntity(URI u) throws OntowrapException {
	try {
	    return ontoInf.getResource(u.toString());
	} catch (Exception e) {
	    throw new OntowrapException("No Object for URI "+u);
	}
    }

    public Set<String> getEntityAnnotations(Object o, String lang, String[] types) throws OntowrapException {
	HashSet<String> annots=new HashSet<String>();
	ExtendedIterator<RDFNode> it=null;
	for (String t : types) {
	    if (it==null) 
		it=ontoInf.listObjectsOfProperty((Resource) o,ontoInf.getProperty(t));
	    else 
		it.andThen(ontoInf.listObjectsOfProperty((Resource) o,ontoInf.getProperty(t)));
	}
	while (it.hasNext()) {
	    Node n = it.next().asNode();
	    if (n.isLiteral() && (lang==null || lang.equals(n.getLiteralLanguage()))) {
		//System.out.println(n.getLiteralLexicalForm());
		annots.add(n.getLiteralLexicalForm());
	    }
	}
	return annots;
    }
    
    public Set<String> getEntityAnnotations(Object o) throws OntowrapException {
	return getEntityAnnotations(o,null);
    }

    public Set<String> getEntityAnnotations(Object o, String lang) throws OntowrapException {
	return getEntityAnnotations(o,lang,new String[]{RDFS.label.toString(),SKOS_NOTE,SKOS_NOTATION});
    }

    public Set<String> getEntityComments(Object o, String lang) throws OntowrapException {
	return getEntityAnnotations(o,lang,new String[]{SKOS_NOTE});
    }

    public Set<String> getEntityComments(Object o) throws OntowrapException {
	return getEntityComments(o,null);
    }

    @Override
    public String getEntityName(Object o) throws OntowrapException {
	return getEntityName(o,null);
    }

    @Override
    public String getEntityName(Object o, String lang) throws OntowrapException {
	try {
	    return getEntityAnnotations(o,lang,new String[]{SKOS_PREFLABEL}).iterator().next();
	}
	catch (Exception e) {
	    throw new OntowrapException("No skos:prefLabel for "+o);
	}
    }

    @Override
    public Set<String> getEntityNames(Object o, String lang) throws OntowrapException {
	return getEntityAnnotations(o,lang,new String[]{RDFS.label.toString()});
    }

    @Override
    public Set<String> getEntityNames(Object o) throws OntowrapException {
	return getEntityNames(o,null);
    }

    @Override
    public URI getEntityURI(Object o) throws OntowrapException {
	try {
	    return URI.create(((Resource) o).getURI());
	}
	catch (Exception e) {
	    throw new OntowrapException("No URI for "+o);
	}
    }

    @Override
    public Set<? extends Object> getIndividuals() throws OntowrapException {
	return Collections.emptySet();
    }

    @Override
    public Set<? extends Object> getObjectProperties() throws OntowrapException {
	return Collections.emptySet();
    }

    @Override
    public Set<? extends Object> getProperties() throws OntowrapException {
	return Collections.emptySet();
    }

    @Override
    public boolean isClass(Object o) throws OntowrapException {
	return ontoInf.contains((Resource) o, RDF.type, ontoInf.getResource(SKOS_CONCEPT));
    }

    @Override
    public boolean isDataProperty(Object o) throws OntowrapException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean isEntity(Object o) throws OntowrapException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean isIndividual(Object o) throws OntowrapException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean isObjectProperty(Object o) throws OntowrapException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean isProperty(Object o) throws OntowrapException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public int nbClasses() throws OntowrapException {
	return this.getClasses().size();
    }

    @Override
    public int nbDataProperties() throws OntowrapException {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public int nbEntities() throws OntowrapException {
	return this.getClasses().size();
    }

    @Override
    public int nbIndividuals() throws OntowrapException {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public int nbObjectProperties() throws OntowrapException {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public int nbProperties() throws OntowrapException {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public void unload() throws OntowrapException {
	// TODO Auto-generated method stub
	
    }

    @Override
    public URI getFile() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public URI getFormURI() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getFormalism() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Model getOntology() {
	return ontoInf;
    }

    @Override
    public URI getURI() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setFile(URI file) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void setFormURI(URI u) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void setFormalism(String name) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void setOntology(Model o) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void setURI(URI uri) {
	// TODO Auto-generated method stub
	
    }
    
   
    

}
