/*
 * $Id$
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.4 - 2006
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

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Visitable;

/**
 * <p>
 * Represents a InstanceExpression.
 * </p>
 * 
 * @author Francois Scharffe, Adrian Mocan
 * 
 * Created on 23-Mar-2005 Committed by $Author: adrianmocan $
 * 
 * $Source:
 * /cvsroot/mediation/mappingapi/src/fr.inrialpes.exmo.align.impl.edoal/InstanceExpr.java,v $,
 * @version $Revision: 1.4 $ $Date$
 */

public class InstanceExpression extends Expression {

    /**
     * Creates a simple InstaneExpression with the given Id.
     * 
     * @param id
     *            the Id of this expression
     * @throws IllegalArgumentException
     *             if the id isn't a InstanceId
     */
    public InstanceExpression() {
	super();
    }

    public void accept(AlignmentVisitor visitor) throws AlignmentException {
	visitor.visit(this);
    }
    /*
    public Object clone() {
	return super.clone();
    }
    */
}
