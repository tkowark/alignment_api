/*
 * $Id$
 *
 * Copyright (C) INRIA, 2009-2010
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

package fr.inrialpes.exmo.align.impl.eval;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.BasicEvaluator;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;

import fr.inrialpes.exmo.iddl.IDDLReasoner;
import fr.inrialpes.exmo.iddl.conf.Semantics;

import java.util.Properties;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.io.PrintWriter;
import java.net.URI;


/**
 * Evaluate proximity between two alignments.
 * This function implements Precision/Recall/Fallout. The first alignment
 * is thus the expected one.
 *
 * @author Jerome Euzenat
 * @version $Id$ 
 */

public class SemPRecEvaluator extends PRecEvaluator implements Evaluator {

    private int nbfoundentailed = 0; // nb of returned cells entailed by the reference alignment
    private int nbexpectedentailed = 0; // nb of reference cells entailed by returned alignment

    private Semantics semantics = Semantics.DL; // the semantics used for interpreting alignments

    /** Creation
     * Initiate Evaluator for precision and recall
     * @param align1 : the reference alignment
     * @param align2 : the alignment to evaluate
     **/
    public SemPRecEvaluator(Alignment align1, Alignment align2) throws AlignmentException {
	super(((BasicAlignment)align1).toURIAlignment(), ((BasicAlignment)align2).toURIAlignment());
    }

    public void init( Object sem ){
	super.init(); // ??
	nbexpectedentailed = 0;
	nbfoundentailed = 0;
	if ( sem instanceof Semantics ) {
	    semantics = (Semantics)sem;
	}
    }

    /**
     *
     * The formulas are standard:
     * given a reference alignment A
     * given an obtained alignment B
     * which are sets of cells (linking one entity of ontology O to another of ontolohy O').
     *
     * P = |A inter B| / |B|
     * R = |A inter B| / |A|
     * F = 2PR/(P+R)
     * with inter = set intersection and |.| cardinal.
     *
     * In the implementation |B|=nbfound, |A|=nbexpected and |A inter B|=nbcorrect.
     * 
     * This takes semantivs as a parameter which should be a litteral of fr.inrialpes.exmo.iddl.conf.Semantics
     */
    public double eval( Properties params, Object cache ) throws AlignmentException {
	init( params.getProperty( "semantics" ) );
	nbfound = align2.nbCells();
	nbexpected = align1.nbCells();

	IDDLReasoner reasoner = new IDDLReasoner( semantics );
	reasoner.addOntology( align1.getOntology1URI() );
	reasoner.addOntology( align1.getOntology2URI() );
	reasoner.addAlignment( align1 );
	// What to do if not consistent?
	reasoner.isConsistent();

	for ( Cell c2 : align2 ) {
	    // create alignment
	    Alignment al = new ObjectAlignment();
	    al.init( align2.getOntology1URI(), align2.getOntology2URI() );
	    // add the cell
	    al.addAlignCell( c2.getObject1(), c2.getObject2(), c2.getRelation().getRelation(), 1. );
	    if ( reasoner.isEntailed( al ) ) nbfoundentailed++;
	}

	reasoner = new IDDLReasoner( semantics );
	reasoner.addOntology( align2.getOntology1URI() );
	reasoner.addOntology( align2.getOntology2URI() );
	reasoner.addAlignment( align2 );
	// What to do if not consistent?
	reasoner.isConsistent();

	for ( Cell c1 : align1 ) {
	    // create alignment
	    Alignment al = new ObjectAlignment();
	    al.init( align2.getOntology1URI(), align2.getOntology2URI() );
	    // add the cell (too bad, addCell is not in the interface)
	    al.addAlignCell( c1.getObject1(), c1.getObject2(), c1.getRelation().getRelation(), 1. );
	    if ( reasoner.isEntailed( al ) ) nbexpectedentailed++;
	}

	precision = (double) nbfoundentailed / (double) nbfound;
	recall = (double) nbexpectedentailed / (double) nbexpected;
	return computeDerived();
    }

    public int getFoundEntailed() { return nbfoundentailed; }
    public int getExpectedEntailed() { return nbexpectedentailed; }

    public Properties getResults() {
	Properties results = super.getResults();
	results.setProperty( "nbexpectedentailed", Integer.toString( nbexpectedentailed ) );
	results.setProperty( "nbfoundentailed", Integer.toString( nbfoundentailed ) );
	return results;
    }


}

