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
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public abstract class OntologyAdapter<O> extends ConcreteOntology<O> {

	protected O ontology;
	protected URI uri;

	protected Set<Entity> classes;
	protected Set<Entity> properties;
	protected Set<Entity> entities;
	public Set<Entity> individuals;

	protected OntologyAdapter() {}

	public OntologyAdapter(O ont, Set<Entity> c, Set<Entity> p, URI uri) {
		ontology=ont;
		classes = c;
		properties = p;
		entities = new HashSet<Entity>(c);
		entities.addAll(p);
		this.uri = uri;
	}

	public boolean contains(Entity e) {
		return classes.contains(e) || properties.contains(e);
	}

	public Set<Entity> getClasses() {
		return Collections.unmodifiableSet(classes);
	}

	public Set<Entity> getEntities() {
		return Collections.unmodifiableSet(entities);
	}

	public O getOntology() {
		return ontology;
	}

	public void setOntology( O o ) {
		ontology = o;
	}

	public Set<Entity> getProperties() {
		return Collections.unmodifiableSet(properties);
	}

	public URI getURI() {
		return uri;
	}

	public String toString() {
		return uri.toString();
	}

}
