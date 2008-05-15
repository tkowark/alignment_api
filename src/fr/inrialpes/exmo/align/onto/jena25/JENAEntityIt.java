package fr.inrialpes.exmo.align.onto.jena25;

import java.net.URI;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.ontology.OntResource;


public class JENAEntityIt implements Iterator<OntResource> {

	    private Iterator<OntResource> it;
	    private OntResource current;
	    private URI ontURI;

	    public JENAEntityIt(URI ontURI, Iterator<OntResource> entityIt) {
		this.ontURI = ontURI;
		this.it= entityIt;
	    }

	    private void setNext() {
		while (current==null) {
		    current = it.next();
		    if (current.getURI()==null || !current.getURI().startsWith(ontURI.toString())) {
			current=null;
		    }
		}
	    }
	    public boolean hasNext() {
		try {
		    setNext();
		    return current!=null;
		}
		catch (NoSuchElementException e) {
			return false;
		}
	    }

	    public OntResource next() {
		setNext();
		OntResource returnR = current;
		current=null;
		return returnR;
	    }

	    public void remove() {
		throw new UnsupportedOperationException();
	    }
}
