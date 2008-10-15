/*
 * $Id$
 *
 * Copyright (C) INRIA, 2008
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

    /* Capability methods */
    public boolean getCapabilities( int Direct, int Asserted, int Named );

    /* Class methods */
    public Set<Object> getSubClasses( Object c, int local, int asserted, int named );
    public Set<Object> getSuperClasses( Object c, int local, int asserted, int named );
    public Set<Object> getProperties( Object c, int local, int asserted, int named );
    public Set<Object> getDataProperties( Object c, int local, int asserted, int named );
    public Set<Object> getObjectProperties( Object c, int local, int asserted, int named );
    public Set<Object> getInstances( Object c, int local, int asserted, int named  );

    /* Property methods */
    public Set<Object> getSubProperties( Object p, int local, int asserted, int named );
    public Set<Object> getSuperProperties( Object p, int local, int asserted, int named );
    public Set<Object> getRange( Object p, boolean asserted );
    public Set<Object> getDomain( Object p, boolean asserted );

    /* Individual methods */
    public Set<Object> getClasses( Object i, int local, int asserted, int named );

}
