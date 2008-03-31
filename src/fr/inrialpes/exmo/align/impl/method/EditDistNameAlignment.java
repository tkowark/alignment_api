/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2005, 2007-2008
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

public class EditDistNameAlignment extends DistanceAlignment implements AlignmentProcess {
    /** Creation **/
    public EditDistNameAlignment(){
	setSimilarity( new MatrixMeasure() {
		public double measure( Object o1, Object o2 ) throws Exception {
		    String s1 = ontology1().getEntityName( o1 );
		    String s2 = ontology2().getEntityName( o2 );
		    if ( s1 == null || s2 == null ) return 1.;
		    else return StringDistances.levenshteinDistance(
							s1.toLowerCase(),
							s2.toLowerCase()) / max(s1.length(),s2.length());
		}
		public double classMeasure( Object cl1, Object cl2 ) throws Exception {
		    return measure( cl1, cl2 );
		}
		public double propertyMeasure( Object pr1, Object pr2 ) throws Exception {
		    return measure( pr1, pr2 );
		}
		public double individualMeasure( Object id1, Object id2 ) throws Exception {
		    return measure( id1, id2 );
		}
	    } );
    };

    private double max( double i, double j) { if ( i>j ) return i; else return j; }

}

