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
import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

//import org.semanticweb.owl.model.OWLOntology;
//import org.semanticweb.owl.model.OWLEntity;

/**
 * Represents an OWL ontology alignment.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */


public interface Alignment
{

    /** Alignment methods **/

    public void accept( AlignmentVisitor visitor ) throws AlignmentException;
    /**
     * The alignment has reference to the two aligned ontology.
     * All Alignment cells contain firts the entity from the first ontology
     * The alignment is from the first ontology to the second.
     */
    public Object getOntology1();
    public Object getOntology2();
    public void setOntology1(Object ontology) throws AlignmentException;
    public void setOntology2(Object ontology) throws AlignmentException;
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

    /**
     * Alignment type:
     * Currently defined a sa String.
     * This string is supposed to contain two characters: among ?, 1, *, +
     * Can be implemented otherwise
     */
    public void setFile1( URI type );
    public void setFile2( URI type );
    public URI getFile1();
    public URI getFile2();

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
    public Cell addAlignCell( Object ob1, Object ob, String relation, double measure) throws AlignmentException;
    public Cell addAlignCell( Object ob1, Object ob2) throws AlignmentException;
    public Cell getAlignCell1( Object ob ) throws AlignmentException;
    public Cell getAlignCell2( Object ob ) throws AlignmentException;
    /**
     * Each part of the cell can be queried independently.
     * There is not cell access out of the alignment objects.
     */
    public Object getAlignedObject1( Object ob ) throws AlignmentException;
    public Object getAlignedObject2( Object ob ) throws AlignmentException;
    public Relation getAlignedRelation1( Object ob ) throws AlignmentException;
    public Relation getAlignedRelation2( Object ob ) throws AlignmentException;
    public double getAlignedStrength1( Object ob ) throws AlignmentException;
    public double getAlignedStrength2( Object ob ) throws AlignmentException;

    public Enumeration getElements();
    public int nbCells();

    public void cut( double threshold ) throws AlignmentException;
    public void harden( double threshold ) throws AlignmentException;
    public void inverse() throws AlignmentException;

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
    //    public void write( PrintStream writer ) throws IOException, AlignmentException;

    /** Exporting
	The alignments are exported for other purposes.
    */
    public void render( PrintStream writer, AlignmentVisitor renderer ) throws AlignmentException;

}

