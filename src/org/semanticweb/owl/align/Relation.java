/*
 * $Id$
 * Copyright (C) INRIA Rh�ne-Alpes, 2003-2004
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

import org.semanticweb.owl.model.OWLException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Represents an OWL ontology alignment. An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information
 * about those objects.
 *
 * @author J�r�me Euzenat
 * @version $Id$ 
 */


public interface Relation
{
    /** Creation **/
    //public Relation( OWLOntology onto1, OWLOntology onto2 );

    /** Housekeeping **/
    public void dump(ContentHandler h);
    public void write( PrintStream writer ) throws java.io.IOException ;
    /** Exporting */
    public void printAsAxiom( Cell c ) throws OWLException;

}


