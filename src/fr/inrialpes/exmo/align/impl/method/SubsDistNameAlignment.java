/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2005
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

package fr.inrialpes.exmo.align.impl.method; 

import java.util.Iterator;

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

import fr.inrialpes.exmo.align.impl.DistanceAlignment;

/**
 * This class implements alignment based on substring distance
 * of class and property labels
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class SubsDistNameAlignment extends DistanceAlignment implements AlignmentProcess
{
    /** Creation **/
    public SubsDistNameAlignment( OWLOntology onto1, OWLOntology onto2 ){
	super( onto1, onto2 );
	setSimilarity( new MatrixMeasure() {
		public double measure( OWLClass cl1, OWLClass cl2 ) throws OWLException{
		    String s1 = cl1.getURI().getFragment();
		    String s2 = cl2.getURI().getFragment();
		    return StringDistances.subStringDistance(s1.toLowerCase(),s2.toLowerCase());
		}
		public double measure( OWLProperty pr1, OWLProperty pr2 ) throws OWLException{
		    String s1 = pr1.getURI().getFragment();
		    String s2 = pr2.getURI().getFragment();
		    return StringDistances.subStringDistance(s1.toLowerCase(),s2.toLowerCase());
		}
		public double measure( OWLIndividual id1, OWLIndividual id2 ) throws OWLException{
		    String s1 = id1.getURI().getFragment();
		    String s2 = id2.getURI().getFragment();
		    return StringDistances.subStringDistance(s1.toLowerCase(),s2.toLowerCase());
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
