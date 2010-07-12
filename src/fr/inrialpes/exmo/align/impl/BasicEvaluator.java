/*
 * $Id$
 *
 * Copyright (C) INRIA, 2004, 2007-2008, 2010
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

package fr.inrialpes.exmo.align.impl; 

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Evaluator;

import java.io.PrintWriter;

/**
 * Evaluate proximity between two alignments.
 * This function implements a simple weighted symetric difference.
 * There are many different things to compute in such a function...
 * Add classification per type of objects (Ind, Class, Prop...)
 */

public abstract class BasicEvaluator implements Evaluator {
    protected double result = 1.;
    protected Alignment align1;
    protected Alignment align2;

    /** Creation **/
    public BasicEvaluator( Alignment align1, Alignment align2 ) throws AlignmentException {
	if ( !align1.getOntology1URI().equals( align2.getOntology1URI() )
	     || !align1.getOntology2URI().equals( align2.getOntology2URI() ) )
	    throw new AlignmentException( "The alignments must align the same ontologies\n" );
	this.align1 = align1;
	this.align2 = align2;
    }

    public void write( PrintWriter writer ) throws java.io.IOException {
	writer.print("<rdf:RDF>\n  <Evaluation class=\"BasicEvaluator\">\n    <result>");
 	writer.print(result);
 	writer.print("</result>\n  </Evaluation>\n</rdf:RDF>\n");
    }

}


