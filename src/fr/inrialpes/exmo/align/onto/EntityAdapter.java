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
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class EntityAdapter<E> implements Entity<E>{

	protected E entity;
	protected Ontology ontology;
	protected URI uri;

	public static enum LANGUAGES { en, fr  };
	public static enum TYPE {comment, label};
	protected Map<LANGUAGES,Set<String>> langToAnnot;
	protected Map<TYPE,Set<String>> typeToAnnot;
	protected Set<String> annotations;

	public EntityAdapter(E e, Ontology o, URI u, boolean init) {
	    entity = e;
	    ontology = o;
	    uri = u;
	    if (init) init();
	}

	protected EntityAdapter(boolean init) {
	    if (init) init();
	}

	protected void init() {
	    annotations = new HashSet<String>();
	    langToAnnot = new EnumMap<LANGUAGES, Set<String>>(LANGUAGES.class);
	    typeToAnnot = new EnumMap<TYPE,Set<String>>(TYPE.class);
	    for (LANGUAGES lang : LANGUAGES.values())
		langToAnnot.put(lang, new HashSet<String>());
	    for (TYPE type : TYPE.values())
			typeToAnnot.put(type, new HashSet<String>());
	}

	public Set<String> getAnnotations(){
	    return annotations;
	}
	public Set<String> getTypeToAnnot( TYPE type ){
	    return typeToAnnot.get( type );
	}

	public Set<String> getLangToAnnot( LANGUAGES lang ){
	    return langToAnnot.get( lang );
	}

	public Set<String> getAnnotations(String lang, TYPE type) {
		Set<String> annots;
		if (lang == null && type == null)
			return Collections.unmodifiableSet(annotations);
		else if (lang == null)
			return Collections.unmodifiableSet(typeToAnnot.get(type));
		else if (type == null)
			return Collections.unmodifiableSet(langToAnnot.get(LANGUAGES.valueOf(lang)));

		annots= new HashSet<String>(langToAnnot.get(lang));
		annots.retainAll(typeToAnnot.get(type));
		return Collections.unmodifiableSet(annots);
	}

	public Set<String> getAnnotations(String lang) {
		return getAnnotations(lang,null);
	}

	public Set<String> getComments(String lang) {
		return getAnnotations(lang,TYPE.comment);
	}

	public Set<String> getLabels(String lang) {
		return getAnnotations(lang,TYPE.label);
	}

	public E getObject() {
		return entity;
	}

	public Ontology getOntology() {
		return ontology;
	}

	public URI getURI() {
		return uri;
	}

	public String toString() {
		return uri.toString();
	}

}
