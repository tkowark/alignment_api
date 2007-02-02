/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2005, 2007
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
 * Matches two oontologies based on the equality of the name of their entities.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class NameEqAlignment extends DistanceAlignment implements AlignmentProcess {
	
    /** Creation **/
    public NameEqAlignment(){
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
    public void align( Alignment alignment, Parameters params ) throws AlignmentException {
	loadInit( alignment );
	getSimilarity().initialize( (OWLOntology)getOntology1(), (OWLOntology)getOntology2(), alignment );
	getSimilarity().compute( params );
	extract( type, params );
    }
}
