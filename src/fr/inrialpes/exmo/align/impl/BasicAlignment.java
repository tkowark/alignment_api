/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2007
 * Copyright (C) CNR Pisa, 2005
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

import java.util.Hashtable;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.net.URI;

import org.xml.sax.ContentHandler;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.Parameters;

/**
 * Represents a basic ontology alignment, i.e., a fully functionnal alignment
 * for wich the type of aligned objects is not known.
 *
 * NOTE(JE): hashtabale are indexed by URI.
 * This is strange, but there might be a reason
 * NOTE(JE): I do not think that this is the case anymore
 * 
 * In version 3.0 this class is virtually abstract.
 * But it cannot be declared abstract because it uses its own constructor.
 * 
 * @author Jérôme Euzenat, David Loup, Raphaël Troncy
 * @version $Id$
 */

public class BasicAlignment implements Alignment {
    public void accept(AlignmentVisitor visitor) throws AlignmentException {
	visitor.visit(this);
    }

    protected Object onto1 = null;

    protected Object onto2 = null;

    /* Set to true for rejecting the use of deprecated (non deterministic) primitives */
    protected static boolean STRICT_IMPLEMENTATION = false;

    protected int debug = 0;

    protected String level = "0";

    protected String type = "**";

    protected Hashtable hash1 = null;

    protected Hashtable hash2 = null;

    protected long time = 0;

    protected Parameters extensions = null;

    /**
     * This is the URI of the place from which the ontology has been loaded!
     * This is NOT the Ontology URI which can be obtained by
     * onto1.getLogicalURI();
     */
    protected URI uri1 = null;

    protected URI uri2 = null;

    public BasicAlignment() {
	hash1 = new Hashtable();
	hash2 = new Hashtable();
	extensions = new BasicParameters();
	setExtension( "method", getClass().getName() );
    }

    public void init( Object onto1, Object onto2, Object cache ) throws AlignmentException {
	this.onto1 = onto1;
	this.onto2 = onto2;
    }

    public void init( Object onto1, Object onto2 ) throws AlignmentException {
	init( onto1, onto2, null );
    }

    public int nbCells() {
	int sum = 0;
	for ( Enumeration e = hash1.elements(); e.hasMoreElements(); ) {
	    sum += ((HashSet)e.nextElement()).size();
	}
	return sum;
    }

    /** Alignment methods * */
    public Object getOntology1() {
	return onto1;
    };

    public Object getOntology2() {
	return onto2;
    };

    public URI getOntology1URI() throws AlignmentException {
	if ( onto1 instanceof URI ) {
	    return (URI)onto1;
	} else {
	    throw new AlignmentException( "Cannot find URI for "+onto1 );
	}
    };

    public URI getOntology2URI() throws AlignmentException {
	if ( onto2 instanceof URI ) {
	    return (URI)onto2;
	} else {
	    throw new AlignmentException( "Cannot find URI for "+onto2 );
	}
    };

    public void setOntology1(Object ontology) throws AlignmentException {
	//*/onto1 = (OWLOntology)ontology;
	onto1 = ontology;
    };

    public void setOntology2(Object ontology) throws AlignmentException {
	//*/onto2 = (OWLOntology)ontology;
	onto2 = ontology;
    };

    public void setType(String type) { this.type = type; };

    public String getType() { return type; };

    public void setLevel(String level) { this.level = level; };

    public String getLevel() { return level; };

    public URI getFile1() { return uri1; };

    public void setFile1(URI u) { uri1 = u; };

    public URI getFile2() { return uri2; };

    public void setFile2(URI u) { uri2 = u; };

    public Parameters getExtensions(){ return extensions; }

    public void setExtension( String label, String value ) {
	extensions.setParameter( label, value );
    };

    public String getExtension( String label ) {
	return (String)extensions.getParameter( label );
    };

    public Enumeration getElements() { 
	return new MEnumeration( hash1 );
    }

    public ArrayList getArrayElements() {
	ArrayList array = new ArrayList();
	for (Enumeration e = getElements(); e.hasMoreElements(); ) {
	    array.add( (BasicCell)e.nextElement() );
	}
	return array;
    }

    /** Cell methods **/
    public Cell addAlignCell( String id, Object ob1, Object ob2, Relation relation, double measure ) throws AlignmentException {
	Cell cell = createCell( id, ob1, ob2, relation, measure);
	addCell( cell );
	return cell;
    }

    // JE: Why does this not allow to create cells with ids preserved?
    // This would be useful when the Alignements are cloned to preserve them
    public Cell addAlignCell(Object ob1, Object ob2, String relation, double measure) throws AlignmentException {
	return addAlignCell( (String)null, ob1, ob2, BasicRelation.createRelation("="), measure );
    };

    public Cell addAlignCell(Object ob1, Object ob2) throws AlignmentException {
	return addAlignCell( (String)null, ob1, ob2, BasicRelation.createRelation("="), 1. );
    }

    public Cell createCell( String id, Object ob1, Object ob2, Relation relation, double measure) throws AlignmentException {
	return (Cell)new BasicCell( id, ob1, ob2, relation, measure);
    }

    protected void addCell( Cell c ) throws AlignmentException {
	boolean found = false;
	//*/ HashSet s1 = (HashSet)hash1.get((OWLEntity)c.getObject1());
	HashSet s1 = (HashSet)hash1.get(c.getObject1());
	if ( s1 != null ) {
	    // I must check that there is no one here
	    for (Iterator i = s1.iterator(); !found && i.hasNext(); ) {
		if ( c.equals((BasicCell)i.next()) ) found = true;
	    }
	    if (!found) s1.add( c );
	} else {
	    s1 = new HashSet();
	    s1.add( c );
	    //*/hash1.put((OWLEntity)c.getObject1(),s1);
	    hash1.put(c.getObject1(),s1);
	}
	found = false;
	//*/HashSet s2 = (HashSet)hash2.get((OWLEntity)c.getObject2());
	HashSet s2 = (HashSet)hash2.get(c.getObject2());
	if( s2 != null ){
	    // I must check that there is no one here
	    for (Iterator i=s2.iterator(); !found && i.hasNext(); ) {
		if ( c.equals((BasicCell)i.next()) ) found = true;
	    }
	    if (!found)	s2.add( c );
	} else {
	    s2 = new HashSet();
	    s2.add( c );
	    //*/hash2.put((OWLEntity)c.getObject2(),s2);
	    hash2.put(c.getObject2(),s2);
	}
    }

    public Set getAlignCells1(Object ob) throws AlignmentException {
	//*/return (HashSet)hash1.get((OWLEntity)ob);
	return (HashSet)hash1.get( ob );
    }
    public Set getAlignCells2(Object ob) throws AlignmentException {
	//*/return (HashSet)hash2.get((OWLEntity)ob);
	return (HashSet)hash2.get( ob );
    }

    /*
     * Deprecated: implement as the one retrieving the highest strength correspondence
     * @deprecated
     */
    public Cell getAlignCell1(Object ob) throws AlignmentException {
	if ( STRICT_IMPLEMENTATION == true ){
	    throw new AlignmentException("getAlignCell1: deprecated (use getAlignCells1 instead)");
	} else {
	    //*/Set s2 = (Set)hash1.get((OWLEntity)ob);
	    Set s2 = (Set)hash1.get(ob);
	    Cell bestCell = null;
	    double bestStrength = 0.;
	    if ( s2 != null ) {
		for( Iterator it2 = s2.iterator(); it2.hasNext(); ){
		    Cell c = (Cell)it2.next();
		    double val = c.getStrength();
		    if ( val > bestStrength ) {
			bestStrength = val;
			bestCell = c;
		    }
		}
	    }
	    return bestCell;
	}
    }

    public Cell getAlignCell2(Object ob) throws AlignmentException {
	if ( STRICT_IMPLEMENTATION == true ){
	    throw new AlignmentException("getAlignCell2: deprecated (use getAlignCells2 instead)");
	} else {
	    //*/Set s1 = (Set)hash2.get((OWLEntity) ob);
	    Set s1 = (Set)hash2.get(ob);
	    Cell bestCell = null;
	    double bestStrength = 0.;
	    if ( s1 != null ){
		for( Iterator it1 = s1.iterator(); it1.hasNext(); ){
		    Cell c = (Cell)it1.next();
		    double val = c.getStrength();
		    if ( val > bestStrength ) {
			bestStrength = val;
			bestCell = c;
		    }
		}
	    }
	    return bestCell;
	}
    }

    /*
     * @deprecated
     */
    public Object getAlignedObject1(Object ob) throws AlignmentException {
	Cell c = getAlignCell1(ob);
	if (c != null) return c.getObject2();
	else return null;
    };

    /*
     * @deprecated
     */
    public Object getAlignedObject2(Object ob) throws AlignmentException {
	Cell c = getAlignCell2(ob);
	if (c != null) return c.getObject1();
	else return null;
    };

    /*
     * @deprecated
     */
    public Relation getAlignedRelation1(Object ob) throws AlignmentException {
	Cell c = getAlignCell1(ob);
	if (c != null) return c.getRelation();
	else return (Relation) null;
    };

    /*
     * @deprecated
     */
    public Relation getAlignedRelation2(Object ob) throws AlignmentException {
	Cell c = getAlignCell2(ob);
	if (c != null) return c.getRelation();
	else return (Relation) null;
    };

    /*
     * @deprecated
     */
    public double getAlignedStrength1(Object ob) throws AlignmentException {
	Cell c = getAlignCell1(ob);
	if (c != null) return c.getStrength();
	else return 0;
    };

    /*
     * @deprecated
     */
    public double getAlignedStrength2(Object ob) throws AlignmentException {
	Cell c = getAlignCell2(ob);
	if (c != null) return c.getStrength();
	else return 0;
    };

    // JE: beware this does only remove the exact equal cell
    // not those with same value
    public void removeAlignCell(Cell c) throws AlignmentException {
	//*/HashSet s1 = (HashSet)hash1.get((OWLEntity)c.getObject1());
	HashSet s1 = (HashSet)hash1.get(c.getObject1());
	//*/HashSet s2 = (HashSet)hash2.get((OWLEntity)c.getObject2());
	HashSet s2 = (HashSet)hash2.get(c.getObject2());
	s1.remove(c);
	s2.remove(c);
	if (s1.isEmpty())
	    //*/hash1.remove((OWLEntity)c.getObject1());
	    hash1.remove(c.getObject1());
	if (s2.isEmpty())
	    //*/hash2.remove((OWLEntity)c.getObject2());
	    hash2.remove(c.getObject2());
    }

    /***************************************************************************
     * The cut function suppresses from an alignment all the cell over a
     * particulat threshold
     **************************************************************************/
    public void cut2(double threshold) throws AlignmentException {
	for (Enumeration e = getElements(); e.hasMoreElements(); ) {
	    Cell c = (Cell)e.nextElement();
	    if ( c.getStrength() < threshold )
		removeAlignCell( c );
	}
    }

    /***************************************************************************
     * Default cut implementation
     * For compatibility with API until version 1.1
     **************************************************************************/
    public void cut( double threshold ) throws AlignmentException {
	cut( "hard", threshold );
    }

    /***************************************************************************
     * Cut refinement :
     * - getting those cells with strength above n (hard)
     * - getting the n best cells (best)
     * - getting those cells with strength at worse n under the best (span)
     * - getting the n% best cells (perc)
     * - getting those cells with strength at worse n% of the best (prop)
     * Rule:
     * threshold is betweew 1 and 0
     **************************************************************************/
    public void cut( String method, double threshold ) throws AlignmentException
    {
	// Check that threshold is a percent
	if ( threshold > 1. || threshold < 0. )
	    throw new AlignmentException( "Not a percentage or threshold : "+threshold );
	// Create a sorted list of cells
	// For sure with sorted lists, we could certainly do far better
	List buffer = getArrayElements();
	Collections.sort( buffer );
	int size = buffer.size();
	boolean found = false;
	int i = 0; // the number of cells to keep
	// Depending on the method, find the limit
	if ( method.equals("perc") ){
	    i = (new Double(size*threshold)).intValue();
	} else if ( method.equals("best") ){
	    i = new Double(threshold*100).intValue();
	} else {
	    double max;
	    if ( method.equals("hard") ) max = threshold;
	    else if ( method.equals("span") ) max = ((Cell)buffer.get(0)).getStrength() - threshold;
	    else if ( method.equals("prop") ) max = ((Cell)buffer.get(0)).getStrength() * threshold;
	    else throw new AlignmentException( "Not a cut specification : "+method );
	    for( i=0; i < size && !found ;) {
		if ( ((Cell)buffer.get(i)).getStrength() < max ) found = true;
		else i++;
	    }
	}
	// Introduce the result back in the structure
	size = i;
	hash1.clear();
	hash2.clear();
	for( i = 0; i < size; i++ ) {
	    addCell( (Cell)buffer.get(i) );
	}
    };

    /***************************************************************************
     * The harden function acts like threshold but put all weights to 1.
     **************************************************************************/
    public void harden(double threshold) throws AlignmentException {
	for (Enumeration e = getElements(); e.hasMoreElements();) {
	    Cell c = (Cell)e.nextElement();
	    if (c.getStrength() < threshold) removeAlignCell( c );
	    else c.setStrength(1.);
	}
    }

    // JE: BEWARE, THESE METHODS RETURNS BASIC-ALIGNMENTS! ONCE IMPLEMENTED
    // THEY MAY BENEFIT AT BEING OVERRIDDEN WHICH THEY ARE NOT
   /**
     * The second alignment is meet with the first one meaning that for
     * any pair (o, o', n, r) in O and (o, o', n', r) in O' the resulting
     * alignment will contain:
     * ( o, o', meet(n,n'), r)
     * any pair which is in only one alignment is preserved.
     */
    public Alignment meet(Alignment align) throws AlignmentException {
	BasicAlignment result = new BasicAlignment();
	result.init(onto1,onto2);
	return result;
    }

   /**
     * The second alignment is join with the first one meaning that for
     * any pair (o, o', n, r) in O and (o, o', n', r) in O' the resulting
     * alignment will contain:
     * ( o, o", join(n,n'), r)
     * any pair which is in only one alignment is discarded.
     */
    public Alignment join(Alignment align) throws AlignmentException {
	BasicAlignment result = new BasicAlignment();
	result.init(onto1,onto2);
	return result;
    }

    /**
     * The second alignment is composed with the first one meaning that for
     * any pair (o, o', n, r) in O and (o',o", n', r') in O' the resulting
     * alignment will contain:
     * ( o, o", join(n,n'), compose(r, r')) iff compose(r,r') exists.
     */
    public Alignment compose(Alignment align) throws AlignmentException {
	BasicAlignment result = new BasicAlignment();
	result.init(onto1,onto2);
	return result;
    }

    /**
     * A new alignment is created such that for
     * any pair (o, o', n, r) in O the resulting alignment will contain:
     * ( o', o, n, inverse(r)) iff compose(r) exists.
     */

    public Alignment inverse () throws AlignmentException {
	BasicAlignment result = new BasicAlignment();
	result.init( onto2, onto1 );
	result.setFile1( getFile2() );
	result.setFile2( getFile1() );
	// We must inverse getType
	result.setType( getType() );
	result.setLevel( getLevel() );
	// Must add an inverse to the method extension
	for ( Enumeration e = extensions.getNames() ; e.hasMoreElements(); ){
	    String label = (String)e.nextElement();
	    result.setExtension( label, getExtension( label ) );
	}
	result.setExtension( "id", (String)null );
	for ( Enumeration e = getElements() ; e.hasMoreElements(); ){
	    result.addCell(((Cell)e.nextElement()).inverse());
	}
	return (Alignment)result;
    };

    /** Housekeeping **/
    public void dump(ContentHandler h) {
    };

    /**
     * Incorporate the cells of the alignment into its own alignment. Note: for
     * the moment, this does not copy but really incorporates. So, if hardening
     * is applied, then the ingested alignmment will be modified as well.
     * JE: May be a "force" boolean for really ingesting or copying may be
     *     useful
     */
    public void ingest(Alignment alignment) throws AlignmentException {
	for (Enumeration e = alignment.getElements(); e.hasMoreElements();) {
	    addCell((Cell)e.nextElement());
	};
    }

    /**
     * Generate a copy of this alignment object
     * It has the same content but a different id (no id indeed)
     */
    public Object clone() {
	BasicAlignment align = new BasicAlignment();
	try {
	    align.init( getOntology1(), getOntology2() );
	    // This method is never launched by the present class
	} catch ( AlignmentException e ) {};
	align.setType( getType() );
	align.setLevel( getLevel() );
	align.setFile1( getFile1() );
	align.setFile2( getFile2() );
	for ( Enumeration e = extensions.getNames() ; e.hasMoreElements(); ){
	    String label = (String)e.nextElement();
	    align.setExtension( label, getExtension( label ) );
	}
	align.getExtensions().unsetParameter( "id" );
	try { align.ingest( this ); }
	catch (AlignmentException ex) { ex.printStackTrace(); }
	return align;
    }

    /**
     * This should be rewritten in order to generate the axiom ontology instead
     * of printing it! And then use ontology serialization for getting it
     * printed.
     */
    public void render( AlignmentVisitor renderer ) throws AlignmentException {
	accept(renderer);
    }
}

class MEnumeration implements Enumeration {
    private Enumeration set = null; // The enumeration of sets
    private Iterator current = null; // The current set's enumeration

    MEnumeration( Hashtable s ){
	set = s.elements();
	while( set.hasMoreElements() && current == null ){
	    current = ((Set)set.nextElement()).iterator();
	    if( !current.hasNext() ) current = null;
	}
    }
    public boolean hasMoreElements(){
	return ( current != null);
    }
    public Object nextElement(){
	Object val = current.next();
	if( !current.hasNext() ){
	    current = null;
	    while( set.hasMoreElements() && current == null ){
		current = ((Set)set.nextElement()).iterator();
		if( !current.hasNext() ) current = null;
	    }
	}
	return val;
    }
}
