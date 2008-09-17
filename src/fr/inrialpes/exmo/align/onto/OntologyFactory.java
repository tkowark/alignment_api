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

import java.net.URI;
import java.util.Hashtable;

import java.lang.reflect.InvocationTargetException;

import org.semanticweb.owl.align.AlignmentException;

public abstract class OntologyFactory {

    //protected static OntologyFactory instance = null;
    protected static Hashtable<String,OntologyFactory> instances = null;

    private static String API_NAME="fr.inrialpes.exmo.align.onto.owlapi10.OWLAPIOntologyFactory";

    public static String getDefaultFactory(){
	return API_NAME;
    }

    public static void setDefaultFactory( String className ){
	API_NAME = className;
    }

    public static OntologyFactory getFactory() {
	return newInstance(API_NAME);
    }

    // JE: The true question here is that instance being static,
    // I assume that it is shared by all subclasses! 
    private static OntologyFactory newInstance( String apiName ) {
	if ( instances == null ) instances = new Hashtable<String,OntologyFactory>();
	OntologyFactory of = instances.get( apiName );
	if ( of != null ) return of;
	try {
	    // This should also be a static getInstance!
	    Class ofClass = Class.forName(apiName);
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

    public abstract void clearCache();

    /**
     * Load an ontology, cache enabled
     */
    public abstract LoadedOntology loadOntology( URI uri ) throws AlignmentException;
    /**
     * Load an ontology, cache enabled if true, disabled otherwise
     * This will disappear: cache will be dispatched in implementations
     */
    public LoadedOntology loadOntology( URI uri, OntologyCache<LoadedOntology> ontologies ) throws AlignmentException {
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
}
