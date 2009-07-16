/*
 * $Id$
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2008
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
    
    /**
     * return one of the "rdfs:label" property values for a given entity.
     * @param o the entity
     * @return a label
     * @throws AlignmentException
     */
    public String getEntityName( Object o ) throws AlignmentException;

    /**
     * Returns the values of the "rdfs:label" property for a given entity and for a given natural language (attribute xml:lang).
     * @param o the entity
     * @param lang the code of the language ("en", "fr", "es", etc.) 
     * @return the set of labels
     * @throws AlignmentException
     */
    public Set<String> getEntityNames( Object o , String lang ) throws AlignmentException;
    /**
     * Returns all the values of the "rdfs:label" property for a given entity.
     * @param o the entity
     * @return the set of labels
     * @throws AlignmentException
     */
    public Set<String> getEntityNames( Object o ) throws AlignmentException;
    
    /**
     * Returns the values of the "rdfs:comment" property for a given entity and for a given natural language (attribute xml:lang).
     * @param o the entity
     * @param lang the code of the language ("en", "fr", "es", etc.) 
     * @return the set of comments
     * @throws AlignmentException
     */
    public Set<String> getEntityComments( Object o , String lang ) throws AlignmentException;
    
    /**
     * Returns all the values of the "rdfs:comment" property for a given entity
     * @param o the entity
     * @return the set of comments
     * @throws AlignmentException
     */
    public Set<String> getEntityComments( Object o ) throws AlignmentException;
    
    /**
     * Returns all the values of the "owl:AnnotationProperty" property for a given entity. 
     * These annotations are those predefined in owl (owl:versionInfo, rdfs:label, rdfs:comment, rdfs:seeAlso and rdfs:isDefinedBy)
     * but also all other defined annotation properties which are subClass of "owl:AnnotationProperty"
     * @param o the entity
     * @return the set of annotation values
     * @throws AlignmentException
     */
    public Set<String> getEntityAnnotations( Object o ) throws AlignmentException;

    public boolean isEntity( Object o );
    public boolean isClass( Object o );
    public boolean isProperty( Object o );
    public boolean isDataProperty( Object o );
    public boolean isObjectProperty( Object o );
    public boolean isIndividual( Object o );

    public Set<?> getEntities();
    public Set<?> getClasses();
    public Set<?> getProperties();
    public Set<?> getObjectProperties();
    public Set<?> getDataProperties();
    public Set<?> getIndividuals();

    public int nbEntities();
    public int nbClasses();
    public int nbProperties();
    public int nbDataProperties();
    public int nbObjectProperties();
    public int nbIndividuals();

    public void unload();
}
