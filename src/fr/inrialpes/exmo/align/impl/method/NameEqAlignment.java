/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2004
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.impl; 

import java.util.Iterator;
import java.util.Hashtable;

import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;

/**
 * Represents an OWL ontology alignment. An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information
 * about those objects.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */


public class NameEqAlignment extends BasicAlignment implements AlignmentProcess
{
	
    /** Creation **/
    public NameEqAlignment( OWLOntology onto1, OWLOntology onto2 ){
    	init( onto1, onto2 );
	setType("11");
    };

    /** Processing **/
    /** This is not exactly equal, this uses toLowerCase() */
    public void align( Alignment alignment, Parameters params ) throws AlignmentException, OWLException {
	Hashtable table = new Hashtable();
	OWLClass cl = null;
	OWLProperty pr = null;
	OWLIndividual id = null;
	//ignore alignment;
	// This is a stupid O(2n) algorithm:
	// Put each class of onto1 in a hashtable indexed by its name (not qualified)
	// For each class of onto2 whose name is found in the hash table
	for ( Iterator it = onto1.getClasses().iterator(); it.hasNext(); ){
	    cl = (OWLClass)it.next();
	    if ( cl.getURI().getFragment() != null )
		table.put((Object)cl.getURI().getFragment().toLowerCase(), cl);
	}
	for ( Iterator it = onto2.getClasses().iterator(); it.hasNext(); ){
	    OWLClass cl2 = (OWLClass)it.next();
	    if ( cl2.getURI().getFragment() != null ) {
		cl = (OWLClass)table.get((Object)cl2.getURI().getFragment().toLowerCase());
		if( cl != null ){ addAlignCell( cl, cl2 ); }
	    }
	}
	for ( Iterator it = onto1.getObjectProperties().iterator(); it.hasNext(); ){
	    pr = (OWLProperty)it.next();
	    if ( pr.getURI().getFragment() != null )
		table.put((Object)pr.getURI().getFragment().toLowerCase(), pr);
	}
	for ( Iterator it = onto2.getObjectProperties().iterator(); it.hasNext(); ){
	    OWLProperty pr2 = (OWLProperty)it.next();
	    if ( pr2.getURI().getFragment() != null ){
		pr = (OWLProperty)table.get((Object)pr2.getURI().getFragment().toLowerCase());
		if( pr != null ){ addAlignCell( pr, pr2 ); }
	    }
	}
	for ( Iterator it = onto1.getDataProperties().iterator(); it.hasNext(); ){
	    pr = (OWLProperty)it.next();
	    if ( pr.getURI().getFragment() != null )
		table.put((Object)pr.getURI().getFragment().toLowerCase(), pr);
	}
	for ( Iterator it = onto2.getDataProperties().iterator(); it.hasNext(); ){
	    OWLProperty pr2 = (OWLProperty)it.next();
	    if ( pr2.getURI().getFragment() != null ){
		pr = (OWLProperty)table.get((Object)pr2.getURI().getFragment().toLowerCase());
		if( pr != null ){ addAlignCell( pr, pr2 ); }
	    }
	}
	//for ( Iterator it = onto1.getIndividuals().iterator(); it.hasNext(); ){
	//	id = (OWLIndividual)it.next();
	//	table.put((Object)pr.getURI().getFragment().toLowerCase(), id);
	//}
	//for ( Iterator it = onto2.getIndividuals().iterator(); it.hasNext(); ){
	//	OWLIndividual id2 = (OWLIndividual)it.next();
	//	id = (OWLIndividual)table.get((Object)id2.getURI().getFragment().toLowerCase());
	//	if( id != null ){ addAlignCell( id, id2 ); }
	//  }
    }

}
