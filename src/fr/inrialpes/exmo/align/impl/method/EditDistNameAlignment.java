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
import java.util.Vector;
import java.net.URI;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.align.impl.MatrixMeasure;
import fr.inrialpes.exmo.align.impl.Similarity;

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
    protected final class EditDistName extends MatrixMeasure {
	public EditDistName(){
	}
	public void compute( Parameters params ){
	    try {
		// Compute distances on classes
		for ( Iterator it2 = onto2.getClasses().iterator(); it2.hasNext(); ){
		    OWLClass cl2 = (OWLClass)it2.next();
		    int l2 = cl2.getURI().getFragment().length();
		    for ( Iterator it1 = onto1.getClasses().iterator(); it1.hasNext(); ){
			OWLClass cl1 = (OWLClass)it1.next();
			int l1 = cl1.getURI().getFragment().length();
			clmatrix[((Integer)classlist1.get(cl1)).intValue()][((Integer)classlist2.get(cl2)).intValue()] =
			    StringDistances.levenshteinDistance(
								cl1.getURI().getFragment().toLowerCase(),
								cl2.getURI().getFragment().toLowerCase()) / max(l1,l2);
		    }
		}
		// Compute distances on properties
		for ( Iterator it2 = onto2.getObjectProperties().iterator(); it2.hasNext(); ){
		    OWLProperty pr2 = (OWLProperty)it2.next();
		    int l2 = pr2.getURI().getFragment().length();
		    for ( Iterator it1 = onto1.getObjectProperties().iterator(); it1.hasNext(); ){
			OWLProperty pr1 = (OWLProperty)it1.next();
			int l1 = pr1.getURI().getFragment().length();
			prmatrix[((Integer)proplist1.get(pr1)).intValue()][((Integer)proplist2.get(pr2)).intValue()] =
			    StringDistances.levenshteinDistance(
								pr1.getURI().getFragment().toLowerCase(),
								pr2.getURI().getFragment().toLowerCase()) / max(l1,l2);
		    }
		    for ( Iterator it1 = onto1.getDataProperties().iterator(); it1.hasNext(); ){
			OWLProperty pr1 = (OWLProperty)it1.next();
			int l1 = pr1.getURI().getFragment().length();
			prmatrix[((Integer)proplist1.get(pr1)).intValue()][((Integer)proplist2.get(pr2)).intValue()] =
			    StringDistances.levenshteinDistance(
								pr1.getURI().getFragment().toLowerCase(),
								pr2.getURI().getFragment().toLowerCase()) / max(l1,l2);
		    }
		}
		for ( Iterator it2 = onto2.getDataProperties().iterator(); it2.hasNext(); ){
		    OWLProperty pr2 = (OWLProperty)it2.next();
		    int l2 = pr2.getURI().getFragment().length();
		    for ( Iterator it1 = onto1.getObjectProperties().iterator(); it1.hasNext(); ){
			OWLProperty pr1 = (OWLProperty)it1.next();
			int l1 = pr1.getURI().getFragment().length();
			prmatrix[((Integer)proplist1.get(pr1)).intValue()][((Integer)proplist2.get(pr2)).intValue()] =
			    StringDistances.levenshteinDistance(
								pr1.getURI().getFragment().toLowerCase(),
								pr2.getURI().getFragment().toLowerCase()) / max(l1,l2);
		    }
		    for ( Iterator it1 = onto1.getDataProperties().iterator(); it1.hasNext(); ){
			OWLProperty pr1 = (OWLProperty)it1.next();
			int l1 = pr1.getURI().getFragment().length();
			prmatrix[((Integer)proplist1.get(pr1)).intValue()][((Integer)proplist2.get(pr2)).intValue()] =
			    StringDistances.levenshteinDistance(
								pr1.getURI().getFragment().toLowerCase(),
								pr2.getURI().getFragment().toLowerCase()) / max(l1,l2);
		    }
		}


	    } catch (OWLException e) { e.printStackTrace(); }
	}
    }
 
    /** Creation **/
    public EditDistNameAlignment( OWLOntology onto1, OWLOntology onto2 ){
	super( onto1, onto2 );
	setSimilarity( new EditDistName() );
	setType("**");
    };

    private double max( double i, double j) { if ( i>j ) return i; else return j; }

    /** Processing **/
    /** This is not exactly equal, this uses toLowerCase() */
    public void align( Alignment alignment, Parameters params ) throws AlignmentException, OWLException {
	//ignore alignment;
	double threshold = 1.; // threshold above which distances are to high

	getSimilarity().initialize( (OWLOntology)getOntology1(), (OWLOntology)getOntology2(), alignment );
	getSimilarity().compute( params );
	extract( type, params );
    }
}

