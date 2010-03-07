/*
 * $Id$
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.7 - 2006 -- then AttributeExpr.java
 * Copyright (C) INRIA, 2009
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
import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Visitable;

/**
 * <p>
 * Represents a PropertyExpression.
 * </p>
 * <p>
 * $Id$
 * </p>
 * 
 * @author Francois Scharffe, Adrian Mocan
 * @author richi
 * @version $Revision: 1.7 $
 * @date $Date: 2010-03-07 20:40:05 +0100 (Sun, 07 Mar 2010) $
 */

public abstract class PropertyExpression extends PathExpression implements Cloneable, Visitable {

    /** The transformation service */
    private TransfService transf;

    /**
     * Creates a simple PropertyExpression with the given ExpressionDefinition,
     * conditions and transf.
     * 
     * @param id
     *            the ExpressionDefinition
     * @param conditions
     *            the conditions for the expression
     * @param transf
     *            the transformation service
     * @throws IllegalArgumentException
     *             if there are other ids than PropertyId
     * @throws NullPointerException
     *             if the id is {@code null}
     */
    public PropertyExpression() {
	super();
    }

    public void accept(AlignmentVisitor visitor) throws AlignmentException {
	visitor.visit(this);
    }

}
