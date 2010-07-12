/*
 * $Id: DiffEvaluator.java 1425 2010-04-06 20:25:39Z euzenat $
 *
 * Copyright (C) INRIA, 2010
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

import fr.inrialpes.exmo.align.parser.SyntaxElement;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.BasicEvaluator;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.Annotations;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.io.PrintWriter;
import java.net.URI;


/**
 * Evaluate proximity between two alignments.
 * This function implements Precision/Recall/Fallout. The first alignment
 * is thus the expected one.
 *
 * @author Jerome Euzenat
 * @version $Id: PRecEvaluator.java 1425 2010-04-06 20:25:39Z euzenat $ 
 */

public class DiffEvaluator extends BasicEvaluator implements Evaluator {

    Set<Cell> truepositive;
    Set<Cell> falsenegative;
    Set<Cell> falsepositive;

    /** Creation
     * Initiate Evaluator for precision and recall
     * @param align1 : the reference alignment
     * @param align2 : the alignment to evaluate
     * The two parameters are transformed into URIAlignment before being processed
     * Hence, if one of them is modified after initialisation, this will not be taken into account.
     **/
    public DiffEvaluator(Alignment align1, Alignment align2) throws AlignmentException {
	super(((BasicAlignment)align1).toURIAlignment(), ((BasicAlignment)align2).toURIAlignment());
	truepositive = new HashSet<Cell>();
	falsenegative = new HashSet<Cell>();
	falsepositive = new HashSet<Cell>();
    }

    public void init(){
	truepositive = new HashSet<Cell>();
	falsenegative = new HashSet<Cell>();
	falsepositive = new HashSet<Cell>();
    }

    public void diff(){
	// Cassia: Here you can put your code
    }

    public double eval( Properties params ) throws AlignmentException {
	init();
	diff();
	return 1.0;
    }
    public double eval( Properties params, Object cache ) throws AlignmentException {
	return eval( params );
    }

    public String HTMLString (){
	// Cassia: here you can put your display as a string and return it
	String result = "";
	result += "  <div  xmlns:"+Namespace.ATLMAP.shortCut+"='"+Namespace.ATLMAP.prefix+"' typeof=\""+Namespace.ATLMAP.shortCut+":output\" href=''>";
	result += "    <dl>";
	result += "  </dl>\n  </div>\n";
return result;
    }

    public void write(PrintWriter writer) throws java.io.IOException {
	// Cassia: here you can put your display as XML But this is not compulsory
	writer.println("<?xml version='1.0' encoding='utf-8' standalone='yes'?>");
    }

    public Properties getResults() {
	Properties results = new Properties();
	results.setProperty( "true positive", Integer.toString( truepositive.size() ) );
	results.setProperty( "false negative", Integer.toString( falsenegative.size() ) );
	results.setProperty( "false positive", Integer.toString( falsepositive.size() ) );
	return results;
    }

    public Set<Cell> getTruePositive() { return truepositive; }
    public Set<Cell> getFalseNegative() { return falsenegative; }
    public Set<Cell> getFalsePositive() { return falsepositive; }
}

