/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2007-2008
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package fr.inrialpes.exmo.align.impl; 

// import java classes
import java.util.Hashtable;
import java.util.Enumeration;
import java.net.URI;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLException;

/**
 * This caches the loaded ontologies so that it is possible to share them between alignments
 * as well as to unload them if necessary.
 * This class is currently tied to the OWL API. It will be possible to extend it.
 * 
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class OntologyCache {
 
  /** The list of currently loaded ontologies as a function:
   * URI --> Ontology
   * This is the ontology URI, NOT its filename
   */
    Hashtable<URI,Object> ontologies = null;
    
    public OntologyCache() {
	ontologies = new Hashtable<URI,Object>();
    }
  
    public void recordOntology( URI uri, Object ontology ){
	ontologies.put(uri,ontology);
    }

    public OWLOntology getOntology( URI uri ){
	return (OWLOntology)ontologies.get( uri );
    }

    public void unloadOntology( URI uri, Object ontology ){
	// used to be uri.toString();
	Object o = ontologies.get(uri);
	try {
	    if ( o instanceof OWLOntology && o == ontology )
		((OWLOntology)o).getOWLConnection().notifyOntologyDeleted( ((OWLOntology)o) );
	} catch (OWLException ex) { System.err.println(ex); };
	ontologies.remove( uri );
    }

    public void clear(){
	try {
	    for ( Enumeration e = ontologies.elements() ; e.hasMoreElements();  ){
		Object o = e.nextElement();
		if ( o instanceof OWLOntology )
		    ((OWLOntology)o).getOWLConnection().notifyOntologyDeleted( (OWLOntology)o );
	    }
	} catch (OWLException ex) { System.err.println(ex); };
	ontologies.clear();
    }

}
