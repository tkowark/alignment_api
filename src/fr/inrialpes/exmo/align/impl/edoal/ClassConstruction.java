/*
 * $Id$
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.5 - 2006 - was ClassExpr
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
 * Represents a ClassExpression.
 * </p>
 * 
 * @author Francois Scharffe, Adrian Mocan
 * 
 * Created on 23-Mar-2005 Committed by $Author: adrianmocan $
 * 
 * $Source:
 * /cvsroot/mediation/mappingapi/src/fr.inrialpes.exmo.align.impl.edoal/ClassExpr.java,v $,
 * @version $Revision: 1.5 $ $Date$
 */

// JE2010: Should it be abstract with ClassConjunction??

public class ClassConstruction extends ClassExpression {

    /** Holds all expressions. */
    private Collection<ClassExpression> components;
    
    /** Operator of this complex expression. */
    private Constructor operator;
    
    public ClassConstruction() {
	super();
	components = new HashSet<ClassExpression>();
    }

    public ClassConstruction( Constructor op, Collection<ClassExpression> expressions ) {
	if ((expressions == null) || (op == null)) {
	    throw new NullPointerException("The subexpressions and the operator must not be null");
	}
	if (expressions.contains(null)) {
	    throw new IllegalArgumentException("The subexpressions must not contain null");
	}
	this.components = expressions;
	if ( op != SyntaxElement.AND.getOperator() &&
	     op != SyntaxElement.OR.getOperator() &&
	     op != SyntaxElement.NOT.getOperator() ) {
	    throw new IllegalArgumentException( "Incorrect operator for class : "+op );
	}
	this.operator = op;
    }

    public Constructor getOperator() {
	return operator;
    }

    public void setOperator( Constructor op ) {
	operator = op;
    }

    public Collection<ClassExpression> getComponents() {
	return components;
    }

    public void addComponents( ClassExpression exp ) {
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
