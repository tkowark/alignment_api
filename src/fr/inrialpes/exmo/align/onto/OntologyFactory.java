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
import java.util.HashMap;
import java.util.Map;

import java.lang.reflect.InvocationTargetException;

import org.semanticweb.owl.align.AlignmentException;

public abstract class OntologyFactory {

    protected static OntologyFactory instance;
    
    private static String API_NAME="fr.inrialpes.exmo.align.onto.owlapi10.OWLAPIOntologyFactory";

    public static String getDefaultFactory(){
	return API_NAME;
    }

    public void setDefaultFactory( String className ){
	API_NAME = className;
    }

    public static OntologyFactory newInstance() {
	return newInstance(API_NAME);
    }

    public static OntologyFactory newInstance(String apiName) {
	try {
	    Class ofClass = Class.forName(apiName);
	    Class[] cparams = {};
	    java.lang.reflect.Constructor ofConstructor = ofClass.getConstructor(cparams);
	    Object[] mparams = {};
	    instance = (OntologyFactory)ofConstructor.newInstance(mparams);
	} catch (ClassNotFoundException cnfex ) {
	    cnfex.printStackTrace(); // better raise errors
	} catch (NoSuchMethodException nsmex) {
	    nsmex.printStackTrace();
	} catch (InstantiationException ieex) {
	    ieex.printStackTrace();
	} catch (IllegalAccessException iaex) {
	    iaex.printStackTrace();
	} catch (InvocationTargetException itex) {
	    itex.printStackTrace();
	}
	return instance;
    }

    /**
     * Load an ontology, cache enabled
     */
    public abstract LoadedOntology loadOntology( URI uri ) throws AlignmentException;
    /**
     * Load an ontology, cache enabled if true, disabled otherwise
     */
    public LoadedOntology loadOntology( URI uri, OntologyCache ontologies ) throws AlignmentException {
	LoadedOntology onto = null;
	if ( ontologies != null ) {
	    onto = ontologies.getOntologyFromURI( uri );
	    if ( onto != null ) return onto;
	    onto = ontologies.getOntology( uri );
	    if ( onto != null ) return onto;
	};
	onto = loadOntology( uri );
	if ( ontologies != null ) ontologies.recordOntology( uri, onto );
	return onto;
    };

    /* JE: this is a reimplementation of OntologyCache
    // JE: This is not really useful since it doubles OntologyCache...
    // This may be included as well...
    protected static Map<URI,LoadedOntology> loadedOntos = new HashMap<URI,LoadedOntology>();
    protected static Map<URI,LoadedOntology> loadedOntosLogical = new HashMap<URI,LoadedOntology>();
    public LoadedOntology getOntologyFromCache( URI uri ) {
	LoadedOntology onto;
	if ( loadedOntos.containsKey( uri ) ) {
	    onto = loadedOntos.get( uri );
	} else {
	    onto = loadOntology( uri );
	    loadedOntos.put( uri, onto );
	    loadedOntosLogical.put( onto.getURI(), onto);
	}
	return onto;
    }

    public static Ontology getOntologyFromURI(URI logicalURI) {
	return loadedOntosLogical.get(logicalURI);
    }
    */
}
