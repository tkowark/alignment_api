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

package org.semanticweb.owl.align; 

import java.io.PrintStream;
import java.io.IOException;
import java.util.Enumeration;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;

/**
 * Represents an OWL ontology alignment.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */


public interface Alignment
{

    /** Alignment methods **/

    /**
     * The alignment has reference to the two aligned ontology.
     * All Alignment cells contain firts the entity from the first ontology
     * The alignment is from the first ontology to the second.
     */
    public OWLOntology getOntology1();
    public OWLOntology getOntology2();
    public void setOntology1(OWLOntology ontology);
    public void setOntology2(OWLOntology ontology);
    /**
     * Alignment type:
     * Currently defined a sa String.
     * This string is supposed to contain two characters: among ?, 1, *, +
     * Can be implemented otherwise
     */
    public void setLevel( String level );
    public String getLevel();
    /**
     * Alignment type:
     * Currently defined a sa String.
     * This string is supposed to contain two characters: among ?, 1, *, +
     * Can be implemented otherwise
     */
    public void setType( String type );
    public String getType();

    /** Cell methods **/
    /**
     * The alignment itself is a set of Alignment cells relating one
     * entity of the firt ontology to an entity of the second one.
     * These cells are indexed within the Alingnment by the URI of the
     * entities in each ontology (with one hashtable for each).
     * In addition to the coupe of entities, the cells contains a
     * qualification of the relation between them (a Relation object)
     * and a quantification of the confidence in the relation (an int).
     */

    /**
     * Cells are created and indexed at once
     */
    public void addAlignCell( OWLEntity ob1, OWLEntity ob, String relation, double measure) throws OWLException;
    public void addAlignCell( OWLEntity ob1, OWLEntity ob2) throws OWLException;
    public Cell getAlignCell1( OWLEntity ob ) throws OWLException;
    public Cell getAlignCell2( OWLEntity ob ) throws OWLException;
    /**
     * Each part of the cell can be queried independently.
     * There is not cell access out of the alignment objects.
     */
    public OWLEntity getAlignedObject1( OWLEntity ob ) throws OWLException;
    public OWLEntity getAlignedObject2( OWLEntity ob ) throws OWLException;
    public Relation getAlignedRelation1( OWLEntity ob ) throws OWLException;
    public Relation getAlignedRelation2( OWLEntity ob ) throws OWLException;
    public double getAlignedMeasure1( OWLEntity ob ) throws OWLException;
    public double getAlignedMeasure2( OWLEntity ob ) throws OWLException;

    public Enumeration getElements();
    public int nbCells();

    /** Housekeeping **/
    /**
     * The methods for outputing and dispalying alignments are common to
     * all alignment. They depend on the implementation of the similar
     * methods in Cell and Relation.
     */
    /**
     * Dump should be implemented as a method generating SAX events
     * for a SAXHandler provided as input 
     */
    public void dump(ContentHandler h);
    public void write( PrintStream writer ) throws java.io.IOException, org.semanticweb.owl.model.OWLException;

    /** Exporting
	The alignments are exported for other purposes.
    */
    public void printAsAxiom( PrintStream writer ) throws OWLException;

}

