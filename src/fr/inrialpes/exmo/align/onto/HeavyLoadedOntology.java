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

import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;

/**
 * Encapsulate deep access to an ontology through some Ontology API
 *
 * Asserted methods corresponds to the information explicitely given or stored about the entity.
 * Non-asserted corresponds to the information that can be deduced from it.
 * So asserted methods are related to a syntactic view while the others are related to the semantics
 */
public interface HeavyLoadedOntology<O> extends LoadedOntology<O> {

    /* Class methods */
    public Set<Object> getSubClasses( Object c, boolean local, boolean asserted, boolean named );
    public Set<Object> getSuperClasses( Object c, boolean local, boolean asserted, boolean named );
    public Set<Object> getProperties( Object c, boolean local, boolean asserted, boolean named );
    public Set<Object> getDataProperties( Object c, boolean local, boolean asserted, boolean named );
    public Set<Object> getObjectProperties( Object c, boolean local, boolean asserted, boolean named );
    public Set<Object> getInstances( Object c, boolean local, boolean asserted, boolean named  );

    /* Property methods */
    public Set<Object> getSubProperties( Object p, boolean local, boolean asserted, boolean named );
    public Set<Object> getSuperProperties( Object p, boolean local, boolean asserted, boolean named );
    public Set<Object> getRange( Object p, boolean asserted );
    public Set<Object> getDomain( Object p, boolean asserted );

    /* Individual methods */
    public Set<Object> getClasses( Object i, boolean local, boolean asserted, boolean named );

}
