/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2005, 2007
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

import org.semanticweb.owl.align.AlignmentProcess;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.align.impl.MatrixMeasure;

/**
 * This class aligns ontology with regard to the editing distance between 
 * class names.
 * TODO: It does not normalize the results...
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class EditDistNameAlignment extends DistanceAlignment implements AlignmentProcess
{
    /** Creation **/
    public EditDistNameAlignment(){
	setSimilarity( new MatrixMeasure() {
		public double measure( OWLClass cl1, OWLClass cl2 ) throws OWLException{
		    String s1 = cl1.getURI().getFragment();
		    String s2 = cl2.getURI().getFragment();
		    if ( s1 == null || s2 == null ) return 1.;
		    else return StringDistances.levenshteinDistance(
							s1.toLowerCase(),
							s2.toLowerCase()) / max(s1.length(),s2.length());
		}
		public double measure( OWLProperty pr1, OWLProperty pr2 ) throws OWLException{
		    String s1 = pr1.getURI().getFragment();
		    String s2 = pr2.getURI().getFragment();
		    if ( s1 == null || s2 == null ) return 1.;
		    else return StringDistances.levenshteinDistance(
							s1.toLowerCase(),
							s2.toLowerCase()) / max(s1.length(),s2.length());
		}
		public double measure( OWLIndividual id1, OWLIndividual id2 ) throws OWLException{
		    String s1 = id1.getURI().getFragment();
		    String s2 = id2.getURI().getFragment();
		    if ( s1 == null || s2 == null ) return 1.;
		    else return StringDistances.levenshteinDistance(
							s1.toLowerCase(),
							s2.toLowerCase()) / max(s1.length(),s2.length());
		}
	    } );
    };

    private double max( double i, double j) { if ( i>j ) return i; else return j; }

}

