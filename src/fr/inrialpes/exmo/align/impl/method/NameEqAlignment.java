/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2005
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

import java.util.Iterator;
import java.util.Hashtable;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLException;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.align.impl.MatrixMeasure;
import fr.inrialpes.exmo.align.impl.Similarity;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;

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

public class NameEqAlignment extends DistanceAlignment implements AlignmentProcess {
	
    /** Creation **/
    public NameEqAlignment( OWLOntology onto1, OWLOntology onto2 ){
	super( onto1, onto2 );
	setSimilarity( new MatrixMeasure() {
		public double measure( OWLClass cl1, OWLClass cl2 ) throws OWLException{
		    String s1 = cl1.getURI().getFragment();
		    String s2 = cl2.getURI().getFragment();
		    if ( s1 != null && s2 != null && s1.toLowerCase().equals(s2.toLowerCase()) ) return 0.;
		    else return 1.;
		}
		public double measure( OWLProperty pr1, OWLProperty pr2 ) throws OWLException{
		    String s1 = pr1.getURI().getFragment();
		    String s2 = pr2.getURI().getFragment();
		    if ( s1 != null && s2 != null && s1.toLowerCase().equals(s2.toLowerCase()) ) return 0.;
		    else return 1.;
		}
		public double measure( OWLIndividual id1, OWLIndividual id2 ) throws OWLException{
		    String s1 = id1.getURI().getFragment();
		    String s2 = id2.getURI().getFragment();
		    if ( s1 != null && s2 != null && s1.toLowerCase().equals(s2.toLowerCase()) ) return 0.;
		    else return 1.;
		}
	    } );
	setType("**");
    };

    /** Processing **/
    public void align( Alignment alignment, Parameters params ) throws AlignmentException, OWLException {
	//ignore alignment;
	double threshold = 1.; // threshold above which distances are to high

	getSimilarity().initialize( (OWLOntology)getOntology1(), (OWLOntology)getOntology2(), alignment );
	getSimilarity().compute( params );
	extract( type, params );
    }
}
