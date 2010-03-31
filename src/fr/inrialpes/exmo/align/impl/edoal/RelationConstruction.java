/*
 * $Id$
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.5 - 2006 - was RelationExpr
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

package fr.inrialpes.exmo.align.impl.edoal;

import java.util.Collection;
import java.util.HashSet;

import fr.inrialpes.exmo.align.parser.SyntaxElement.Constructor;
import fr.inrialpes.exmo.align.parser.SyntaxElement;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Visitable;

/**
 * <p>
 * Represents a RelationExpression.
 * </p>
 * 
 * @author Francois Scharffe, Adrian Mocan
 * 
 * Created on 23-Mar-2005 Committed by $Author: adrianmocan $
 * 
 * $Source:
 * /cvsroot/mediation/mappingapi/src/fr.inrialpes.exmo.align.impl.edoal/RelationExpr.java,v $,
 * @version $Revision: 1.5 $ $Date: 2010-03-07 20:40:05 +0100 (Sun, 07 Mar 2010) $
 */

public class RelationConstruction extends RelationExpression {

    /** Holds all expressions. */
    private Collection<PathExpression> components;
    
    /** Operator of this complex expression. */
    private Constructor operator;
    
    public RelationConstruction() {
	super();
	components = new HashSet<PathExpression>();
    }

    public RelationConstruction( Constructor op, Collection<PathExpression> expressions ) {
	if ((expressions == null) || (op == null)) {
	    throw new NullPointerException("The subexpressions and the operator must not be null");
	}
	if (expressions.contains(null)) {
	    throw new IllegalArgumentException("The subexpressions must not contain null");
	}
	// The collection should have only relations
	// It should be ordered: implement List
	this.components = expressions;
	if ( op != SyntaxElement.AND.getOperator() &&
	     op != SyntaxElement.OR.getOperator() &&
	     op != SyntaxElement.NOT.getOperator() &&
	     op != SyntaxElement.COMPOSE.getOperator() &&
	     op != SyntaxElement.TRANSITIVE.getOperator() &&
	     op != SyntaxElement.SYMMETRIC.getOperator() &&
	     op != SyntaxElement.REFLEXIVE.getOperator() &&
	     op != SyntaxElement.INVERSE.getOperator() ) {
	    throw new IllegalArgumentException( "Incorrect operator for relation : "+op );
	}
	this.operator = op;
    }

    public Constructor getOperator() {
	return operator;
    }

    public void setOperator( Constructor op ) {
	operator = op;
    }

    public Collection<PathExpression> getComponents() {
	return components;
    }

    public void addComponents( PathExpression exp ) {
	components.add( exp );
    }

    /*
    public void accept(AlignmentVisitor visitor) throws AlignmentException {
	visitor.visit(this);
    }
    */
    /*
    public Object clone() {
	return super.clone();
    }
    */
}
