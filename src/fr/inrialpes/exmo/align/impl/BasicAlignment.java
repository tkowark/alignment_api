/*
 * $Id$
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

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.PrintStream;
import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

/**
 * Represents an OWL ontology alignment. An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information
 * about those objects.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */


public class BasicAlignment implements Alignment
{
    protected String type = "11";
    protected OWLOntology onto1 = null;
    protected OWLOntology onto2 = null;
    protected Hashtable hash1 = null;
    protected Hashtable hash2 = null;

    public BasicAlignment() {
	hash1 = new Hashtable();
	hash2 = new Hashtable();
    }

    // Note: protected is a problem outside of package
    //  but everything else is public
    protected void init( OWLOntology onto1, OWLOntology onto2 ){
	this.onto1 = onto1;
	this.onto2 = onto2;
    }

    /** Alignment methods **/
    public OWLOntology getOntology1(){ return onto1; };
    public OWLOntology getOntology2(){ return onto2; };
    public void setOntology1(OWLOntology ontology)
      { onto1 = ontology; };
    public void setOntology2(OWLOntology ontology)
      { onto2 = ontology; };
    public void setType( String type ){ this.type = type; };
    public String getType(){ return type; };

    /* Please note that all the following methods must be changed because
	they consider that only ONE Entity can be aligned with another !! */
    /** Cell methods **/
    public void addAlignCell( OWLEntity ob1, OWLEntity ob2, String relation, double measure) throws OWLException {
	Cell cell = (Cell)new BasicCell( ob1, ob2, relation, measure );
	hash1.put((Object)(ob1.getURI()),cell);
	hash2.put((Object)(ob2.getURI()),cell);
    };
    public void addAlignCell( OWLEntity ob1, OWLEntity ob2) throws OWLException {
	addAlignCell( ob1, ob2, "=", 0);
    };

    public OWLEntity getAlignedObject1( OWLEntity ob ) throws OWLException{
	Cell c = ((Cell)hash1.get((Object)ob.getURI()));
	if ( c != null ) return c.getObject2();
	else return (OWLEntity)null;
    };
    public OWLEntity getAlignedObject2( OWLEntity ob ) throws OWLException{
	Cell c = ((Cell)hash2.get((Object)ob.getURI()));
	if ( c != null ) return c.getObject1();
	else return (OWLEntity)null;
    };
    public Relation getAlignedRelation1( OWLEntity ob ) throws OWLException{
	Cell c = ((Cell)hash1.get((Object)ob.getURI()));
	if ( c != null ) return c.getRelation();
	else return (Relation)null;
    };
    public Relation getAlignedRelation2( OWLEntity ob ) throws OWLException{
	Cell c = ((Cell)hash2.get((Object)ob.getURI()));
	if ( c != null ) return c.getRelation();
	else return (Relation)null;
    };
    public double getAlignedMeasure1( OWLEntity ob ) throws OWLException{
	Cell c = ((Cell)hash1.get((Object)ob.getURI()));
	if ( c != null ) return c.getMeasure();
	else return 0;
    };
    public double getAlignedMeasure2( OWLEntity ob ) throws OWLException{
	Cell c = ((Cell)hash2.get((Object)ob.getURI()));
	if ( c != null ) return c.getMeasure();
	else return 0;
    };

    /** Housekeeping **/
    public void dump( ContentHandler h ){};

    /** The cut function suppresses from an alignment all the cell
	over a particulat threshold **/
    public void cut( double threshold ) throws OWLException {
	for( Enumeration e = hash1.keys() ; e.hasMoreElements(); ){
	    Cell c = (Cell)hash1.get(e.nextElement());
	    if ( c.getMeasure() > threshold ) {
		// Beware, this suppresses all cells with these keys 
		// There is only one of them
		hash1.remove((Object)c.getObject1().getURI());
		hash2.remove((Object)c.getObject2().getURI());
	    }
	} //end for
	
    };

    /** New version corresponding to the RDF/XML/OWL DTD **/
   public void write( PrintStream writer ) throws IOException, OWLException {
	writer.print("<?xml version='1.0' encoding='utf-8");
	//	writer.print(writer.getEncoding().toString());
	writer.print("' standalone='no'?>\n");
	writer.print("<!DOCTYPE rdf:RDF SYSTEM \"align.dtd\">\n\n");
	// add date, etc.
	writer.print("<rdf:RDF xmlns='http://knowledgeweb.semanticweb.org/heterogeneity/alignment'\n         xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n         xmlns:xsd='http://www.w3.org/2001/XMLSchema#'>\n");
	writer.print("<Alignment>\n  <xml>yes</xml>\n");
	writer.print("  <type>");
	writer.print(type);
	writer.print("</type>\n  <onto1>");
	writer.print(onto1.getLogicalURI().toString());
	writer.print("</onto1>\n  <onto2>");
	writer.print(onto2.getLogicalURI().toString());
	writer.print("</onto2>\n  <map>\n");
	
	for( Enumeration e = hash1.keys() ; e.hasMoreElements(); ){
	    Cell c = (Cell)hash1.get(e.nextElement());
	    if ( c != null ) c.write( writer );
	} //end for
	writer.print("  </map>\n</Alignment>\n");
	writer.print("</rdf:RDF>\n");
    };

}
