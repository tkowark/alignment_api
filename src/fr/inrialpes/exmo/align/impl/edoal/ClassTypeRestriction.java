/*
 * $Id$
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.5 - 2006
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

/**
 * <p>
 * Represents a type typeCondition tag for PropertyExpressions.
 * </p>
 * <p>
 * $Id$
 * </p>
 * <p>
 * Created on 24-Mar-2005 Committed by $Author: poettler_ric $
 * </p>
 * 
 * @author Francois Scharffe
 * @author Adrian Mocan
 * @author Richard Pöttler
 * @version $Revision: 1.5 $ $Date: 2010-03-07 21:55:23 +0100 (Sun, 07 Mar 2010) $
 */
public class ClassTypeRestriction extends ClassRestriction implements Cloneable {

    Datatype type = null;
    boolean universal = true;

    /**
     * Constructs a ClassTypeRestriction with the given restriction.
     * 
     * @param p
     *            the restricted PathExpression
     * @param pred
     * wether the constraint is universal (true) or existential (false)
     * @param t
     *            the Datatype to which this path is restricted
     * @throws NullPointerException
     *             if the restriction is null
     * NOTE: Currently the predicate is not visible in the syntax which only
     * authorises type (so universal)
     */
    public ClassTypeRestriction(final PathExpression p,
				  final boolean pred,
				final Datatype t) {
	super(p);
	// Check that this is a property
	universal = pred;
	type = t;
    }

    public ClassTypeRestriction(final PathExpression p,
				final Datatype t) {
	super(p);
	// Check that this is a property
	type = t;
    }

    public Datatype getType() {
	return type;
    }

    public void setType( Datatype t ) {
	type = t;
    }

    public boolean isUniversal() {
	return universal;
    }
}
