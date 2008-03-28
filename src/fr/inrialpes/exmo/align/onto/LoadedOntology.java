/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2008
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
import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;

public interface LoadedOntology<O> extends Ontology<O> {

    public Object getEntity( URI u ) throws AlignmentException;

    public URI getEntityURI( Object o ) throws AlignmentException;
    public String getEntityName( Object o ) throws AlignmentException;

    public boolean isEntity( Object o );
    public boolean isClass( Object o );
    public boolean isProperty( Object o );
    public boolean isDataProperty( Object o );
    public boolean isObjectProperty( Object o );
    public boolean isIndividual( Object o );

    public Set<Object> getEntities();
    public Set<Object> getClasses();
    public Set<Object> getProperties();
    public Set<Object> getObjectProperties();
    public Set<Object> getDataProperties();
    public Set<Object> getIndividuals();

    public int nbClasses();
    public int nbProperties();
    public int nbDataProperties();
    public int nbObjectProperties();
    public int nbInstances();

    public void unload();
}
