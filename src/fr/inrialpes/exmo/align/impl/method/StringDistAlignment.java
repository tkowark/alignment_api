/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2007
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

package fr.inrialpes.exmo.align.impl.method; 

import java.net.URI;
import java.lang.reflect.Method;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.align.impl.MatrixMeasure;

/**
 * Represents an OWL ontology alignment. An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information
 * about those objects.
 *
 * An improvement of that class is that, since it is based on names only,
 * it can match freely property names with class names...
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class StringDistAlignment extends DistanceAlignment implements AlignmentProcess {
    
    Method dissimilarity = null;

    /** Creation **/
    public StringDistAlignment() {
	setSimilarity( new MatrixMeasure() {
		public double measure( OWLClass cl1, OWLClass cl2 ) throws Exception{
		    String[] params = { cl1.getURI().getFragment(), cl2.getURI().getFragment() };
		    if ( debug > 4 ) 
			System.err.println( "CL:"+cl1.getURI().getFragment()+" ++ "+cl2.getURI().getFragment());
		    return ((Double)dissimilarity.invoke( null, params )).doubleValue();
		}
		public double measure( OWLProperty pr1, OWLProperty pr2 ) throws Exception{
		    String[] params = { pr1.getURI().getFragment(), pr2.getURI().getFragment() };
		    if ( debug > 4 ) 
			System.err.println( "PR:"+pr1.getURI().getFragment()+" ++ "+pr2.getURI().getFragment());
		    return ((Double)dissimilarity.invoke( null, params )).doubleValue();
		}
		public double measure( OWLIndividual id1, OWLIndividual id2 ) throws Exception{
		    if ( debug > 4 ) 
			System.err.println( "ID:"+id1+" -- "+id2);
		    URI URI1 = id1.getURI();
		    String name1;
		    if ( URI1 != null ) name1 = URI1.getFragment();
		    else name1 = "";
		    URI URI2 = id2.getURI();
		    String name2;
		    if ( URI2 != null ) name2 = URI2.getFragment();
		    else name2 = "";
		    String[] params = { name1, name2 };
		    return ((Double)dissimilarity.invoke( null, params )).doubleValue();
		}
	    } );
	setType("**");
    }

    /* Processing */
    public void align( Alignment alignment, Parameters params ) throws AlignmentException {
	loadInit( alignment );
	//ignore alignment;
	//double threshold = 1.; // threshold above which distances are to high

	// Get function from params
	String f = (String)params.getParameter("stringFunction");
	try {
	    String fname = "equalDistance";
	    if ( f != null ) fname = f.trim();
	    Class sClass = Class.forName("java.lang.String");
	    Class[] mParams = { sClass, sClass };
	    dissimilarity = Class.forName("fr.inrialpes.exmo.align.impl.method.StringDistances").getMethod( fname, mParams );
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	}

	// Initialize matrix
	getSimilarity().initialize( (OWLOntology)getOntology1(), (OWLOntology)getOntology2(), alignment );

	// Compute similarity/dissimilarity
	getSimilarity().compute( params );

	// Print matrix if asked
	if ( params.getParameter("printMatrix") != null ) printDistanceMatrix( params );

	// Extract alignment
	extract( type, params );
    }
}
