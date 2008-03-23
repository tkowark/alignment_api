/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2008
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

package fr.inrialpes.exmo.align.onto;

import java.net.URI;
import java.util.Iterator;
import org.xml.sax.SAXException;

import org.semanticweb.owl.align.AlignmentException;

/**
 * Store the information regarding ontologies in a specific structure
 */

public abstract class ConcreteOntology<O> extends BasicOntology<O> implements LoadedOntology<O> {

    public abstract Iterator<Object> getObjectProperties() throws AlignmentException;
    public abstract Iterator<URI> getClassNames() throws AlignmentException;
    public abstract Iterator<URI> getObjectPropertyNames() throws AlignmentException;
    public abstract Iterator<URI> getDataPropertyNames() throws AlignmentException;
    public abstract Iterator<URI> getInstanceNames() throws AlignmentException;
    public abstract int nbClasses() throws AlignmentException;
    public abstract int nbDataProperties() throws AlignmentException;
    public abstract int nbObjectProperties() throws AlignmentException;
    public abstract int nbInstances() throws AlignmentException;
    public abstract Object getEntity( URI uri ) throws AlignmentException;
    //public abstract void loadOntology( URI ref, OntologyCache ontologies ) throws SAXException, AlignmentException;
}
