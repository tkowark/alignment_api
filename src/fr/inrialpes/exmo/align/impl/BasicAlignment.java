/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2004
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

package fr.inrialpes.exmo.align.impl;

import java.lang.ClassNotFoundException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.PrintStream;
import java.io.IOException;
import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

/**
 * Represents an OWL ontology alignment. An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information about those
 * objects.
 * 
 * @author Jérôme Euzenat, David Loup
 * @version $Id$
 */

public class BasicAlignment implements Alignment {
	public void accept(AlignmentVisitor visitor) throws AlignmentException {
		visitor.visit(this);
	}

	protected int debug = 0;

	protected String level = "0";

	protected String type = "**";

	protected OWLOntology onto1 = null;

	protected OWLOntology onto2 = null;

	protected Hashtable hash1 = null;

	protected Hashtable hash2 = null;

	protected URI uri1 = null;

	protected URI uri2 = null;

	public BasicAlignment() {
		hash1 = new Hashtable();
		hash2 = new Hashtable();
	}

	// Note: protected is a problem outside of package
	//  but everything else is public
	protected void init(OWLOntology onto1, OWLOntology onto2) {
		this.onto1 = onto1;
		this.onto2 = onto2;
	}

	public int nbCells() {
		return hash1.size();
	}

	/** Alignment methods * */
	public Object getOntology1() {
		return onto1;
	};

	public Object getOntology2() {
		return onto2;
	};

	public void setOntology1(Object ontology) throws AlignmentException {
		try {
			if (!Class.forName("org.semanticweb.owl.model.OWLOntology")
					.isInstance(ontology))
				throw new AlignmentException(
						"setOntollogy1: arguments must be OWLEntities");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		onto1 = (OWLOntology) ontology;
	};

	public void setOntology2(Object ontology) throws AlignmentException {
		try {
			if (!Class.forName("org.semanticweb.owl.model.OWLOntology")
					.isInstance(ontology))
				throw new AlignmentException(
						"setOntollogy2: arguments must be OWLEntities");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		onto2 = (OWLOntology) ontology;
	};

	public void setType(String type) {
		this.type = type;
	};

	public String getType() {
		return type;
	};

	public void setLevel(String level) {
		this.level = level;
	};

	public String getLevel() {
		return level;
	};

	public URI getFile1() {
		return uri1;
	};

	public void setFile1(URI u) {
		uri1 = u;
	};

	public URI getFile2() {
		return uri2;
	};

	public void setFile2(URI u) {
		uri2 = u;
	};

	public Enumeration getElements() {
		return hash1.elements();
	}

	/*
	 * Please note that all the following methods must be changed because they
	 * consider that only ONE Entity can be aligned with another !!
	 */
	/** Cell methods * */
	public Cell addAlignCell(Object ob1, Object ob2, String relation,
			double measure) throws AlignmentException {
		try {
			if (!Class.forName("org.semanticweb.owl.model.OWLEntity")
					.isInstance(ob1)
					|| !Class.forName("org.semanticweb.owl.model.OWLEntity")
							.isInstance(ob2))
				throw new AlignmentException(
						"addAlignCell: arguments must be OWLEntities");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Cell cell = (Cell) new BasicCell((OWLEntity) ob1, (OWLEntity) ob2,
					relation, measure);
			hash1.put((Object) (((OWLEntity) ob1).getURI()), cell);
			hash2.put((Object) (((OWLEntity) ob2).getURI()), cell);
			return cell;
		} catch (OWLException e) {
			throw new AlignmentException("getURI problem", e);
		}
	};

	public Cell addAlignCell(Object ob1, Object ob2) throws AlignmentException {
		return addAlignCell(ob1, ob2, "=", 1.);
	};

	public Cell getAlignCell1(Object ob) throws AlignmentException {
		try {
			if (!Class.forName("org.semanticweb.owl.model.OWLEntity")
					.isInstance(ob))
				throw new AlignmentException(
						"getAlignCell1: arguments must be OWLEntities");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			return (Cell) hash1.get(((OWLEntity) ob).getURI());
		} catch (OWLException e) {
			throw new AlignmentException("getURI problem", e);
		}
	}

	public Cell getAlignCell2(Object ob) throws AlignmentException {
		try {
			if (!Class.forName("org.semanticweb.owl.model.OWLEntity")
					.isInstance(ob))
				throw new AlignmentException(
						"getAlignCell2: arguments must be OWLEntities");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			return (Cell) hash2.get(((OWLEntity) ob).getURI());
		} catch (OWLException e) {
			throw new AlignmentException("getURI problem", e);
		}
	}

	public Object getAlignedObject1(Object ob) throws AlignmentException {
		try {
			if (!Class.forName("org.semanticweb.owl.model.OWLEntity")
					.isInstance(ob))
				throw new AlignmentException(
						"getAlignedObject1: arguments must be OWLEntities");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Cell c = getAlignCell1(ob);
		if (c != null)
			return c.getObject2();
		else
			return null;
	};

	public Object getAlignedObject2(Object ob) throws AlignmentException {
		try {
			if (!Class.forName("org.semanticweb.owl.model.OWLEntity")
					.isInstance(ob))
				throw new AlignmentException(
						"getAlignedObject2: arguments must be OWLEntities");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Cell c = getAlignCell2(ob);
		if (c != null)
			return c.getObject1();
		else
			return null;
	};

	public Relation getAlignedRelation1(Object ob) throws AlignmentException {
		try {
			if (!Class.forName("org.semanticweb.owl.model.OWLEntity")
					.isInstance(ob))
				throw new AlignmentException(
						"getAlignedRelation1: argument must be OWLEntity");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Cell c = getAlignCell1(ob);
		if (c != null)
			return c.getRelation();
		else
			return (Relation) null;
	};

	public Relation getAlignedRelation2(Object ob) throws AlignmentException {
		try {
			if (!Class.forName("org.semanticweb.owl.model.OWLEntity")
					.isInstance(ob))
				throw new AlignmentException(
						"getAlignedRelation2: argument must be OWLEntity");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Cell c = getAlignCell2(ob);
		if (c != null)
			return c.getRelation();
		else
			return (Relation) null;
	};

	public double getAlignedStrength1(Object ob) throws AlignmentException {
		try {
			if (!Class.forName("org.semanticweb.owl.model.OWLEntity")
					.isInstance(ob))
				throw new AlignmentException(
						"getAlignedStrength1: arguments must be OWLEntities");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Cell c = getAlignCell1(ob);
		if (c != null)
			return c.getStrength();
		else
			return 0;
	};

	public double getAlignedStrength2(Object ob) throws AlignmentException {
		try {
			if (!Class.forName("org.semanticweb.owl.model.OWLEntity")
					.isInstance(ob))
				throw new AlignmentException(
						"getAlignedStrength2: arguments must be OWLEntities");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Cell c = getAlignCell2(ob);
		if (c != null)
			return c.getStrength();
		else
			return 0;
	};

    public void inverse () {
	OWLOntology o = onto1;
	onto1 = onto2;
	onto2 = o;
	// We must inverse getType
	URI u = uri1;
	uri1 = uri2;
	uri2 = u;
	Hashtable h = hash1;
	hash1 = hash2;
	hash2 = h;
	for ( Enumeration e = getElements() ; e.hasMoreElements(); ){
	    ((Cell)e.nextElement()).inverse();
	}
    };

    /** Housekeeping * */
    public void dump(ContentHandler h) {
    };

    /***************************************************************************
     * The cut function suppresses from an alignment all the cell over a
     * particulat threshold
     **************************************************************************/
    public void cut(double threshold) throws AlignmentException {
	for (Enumeration e = hash1.keys(); e.hasMoreElements();) {
	    Cell c = (Cell) hash1.get(e.nextElement());
	    if (c.getStrength() < threshold) {
		// Beware, this suppresses all cells with these keys
		// There is only one of them
		try {
		    hash1.remove(((OWLEntity) c.getObject1()).getURI());
		    hash2.remove(((OWLEntity) c.getObject2()).getURI());
		} catch (OWLException ex) {
		    throw new AlignmentException("getURI problem", ex);
		}
	    }
	} //end for
    };

    /***************************************************************************
     * The harden function acts like threshold but put all weights at 1.
     **************************************************************************/
    public void harden(double threshold) throws AlignmentException {
	for (Enumeration e = hash1.keys(); e.hasMoreElements();) {
	    Cell c = (Cell) hash1.get(e.nextElement());
	    if (c.getStrength() < threshold) {
		// Beware, this suppresses all cells with these keys
		// There is only one of them
		try {
		    hash1.remove(((OWLEntity) c.getObject1()).getURI());
		    hash2.remove(((OWLEntity) c.getObject2()).getURI());
		} catch (OWLException ex) {
		    throw new AlignmentException("getURI problem", ex);
		}
	    } else {
		c.setStrength(1.);
	    }
	} //end for
    };

    /**
     * Incorporate the cell of the alignment into it own alignment. Note: for
     * the moment, this does not copy but really incorporates. So, if hardening
     * or cutting, are applied, then the ingested alignmment will be modified as
     * well.
     */
    protected void ingest(Alignment alignment) throws AlignmentException {
	for (Enumeration e = alignment.getElements(); e.hasMoreElements();) {
	    Cell c = (Cell) e.nextElement();
	    try {
		hash1.put((Object) ((OWLEntity) c.getObject1()).getURI(), c);
		hash2.put((Object) ((OWLEntity) c.getObject2()).getURI(), c);
	    } catch (OWLException ex) {
		throw new AlignmentException("getURI problem", ex);
	    }
	}
    };

    /**
     * This should be rewritten in order to generate the axiom ontology instead
     * of printing it! And then use ontology serialization for getting it
     * printed.
     */
    public void render(PrintStream writer, AlignmentVisitor renderer)
	throws AlignmentException {
	accept(renderer);
    }
}
