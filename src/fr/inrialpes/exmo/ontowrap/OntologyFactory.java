/*
 * $Id$
 *
 * Copyright (C) INRIA, 2008, 2010
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

package fr.inrialpes.exmo.ontowrap;

import java.net.URI;
import java.util.Hashtable;

import java.lang.reflect.InvocationTargetException;

public abstract class OntologyFactory {

    public static int ANY = 0;
    public static int DIRECT = 1;
    public static int INDIRECT = 2;
    public static int ASSERTED = 3;
    public static int UNASSERTED = 4;
    public static int INHERITED = 5;
    public static int FULL = 6;
    public static int NAMED = 7;
    public static int LOCAL = 8;
    public static int GLOBAL = 9;
    public static int UNNAMED = 10;
    public static int MENTIONNED = 11;
    public static int ALL = 12;

    protected static Hashtable<String,OntologyFactory> instances = null;

    private static String API_NAME="fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3OntologyFactory";

    public static String getDefaultFactory(){
	return API_NAME;
    }

    public static void setDefaultFactory( String className ){
	API_NAME = className;
    }

    public static OntologyFactory getFactory() {
	return newInstance(API_NAME);
    }

    private static OntologyFactory newInstance( String apiName ) {
	if ( instances == null ) instances = new Hashtable<String,OntologyFactory>();
	OntologyFactory of = instances.get( apiName );
	if ( of != null ) return of;
	try {
	    // This should also be a static getInstance!
	    Class<?> ofClass = Class.forName(apiName);
	    Class[] cparams = {};
	    java.lang.reflect.Constructor ofConstructor = ofClass.getConstructor(cparams);
	    Object[] mparams = {};
	    of = (OntologyFactory)ofConstructor.newInstance(mparams);
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
	instances.put( apiName, of );
	return of;
    }

    public static void clear() {
	if ( instances != null ) {
	    for ( OntologyFactory of : instances.values() ){
		of.clearCache();
	    }
	}
    }

    /**
     * All Ontologies must implement clearCache()
     * which unload their ontologies if any cache is enabled.
     */
    public abstract void clearCache();

    /**
     * Encapsulate an ontology already in the environment
     * These methods should rather be in a LoadableOntologyFactory
     */
    public abstract LoadedOntology newOntology( Object onto ) throws OntowrapException;

    /**
     * Load an ontology, cache enabled
     * These methods should rather be in a LoadableOntologyFactory
     */
    public abstract LoadedOntology loadOntology( URI uri ) throws OntowrapException;

    /**
     * Load an ontology, cache enabled if true, disabled otherwise
     * This will disappear: cache will be dispatched in implementations
    public LoadedOntology loadOntology( URI uri, OntologyCache<LoadedOntology> ontologies ) throws OntowrapException {
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
     */
}
